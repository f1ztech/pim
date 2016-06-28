package ru.mipt.pim.server.index;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.index.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TermsStatisticsService {

	public static long seekDocSes = 0;
	public static long countTermsSec = 0;
	public static long querySec = 0;
	public static long termVectorSec = 0;
	
	@Autowired
	private IndexFinder indexFinder;
	
	@Autowired
	private IndexingService indexingService;

	private ConcurrentHashMap<Pair<String, String>, TermsStatistics> statisticsCache = new ConcurrentHashMap<>();
	
	// ====================================================
	// TermsStatistics
	// ====================================================

	public static class TermsStatistics {
		
		private HashMap<String, Integer> termIndexes = new HashMap<>();
		private int totalTermsCount;
		private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> docTermsByFieldCount = new ConcurrentHashMap<>();

		public HashMap<String, Integer> getTermIndexes() {
			return termIndexes;
		}

		public void setTermIndexes(HashMap<String, Integer> termIndexes) {
			this.termIndexes = termIndexes;
		}

		public int getTotalTermsCount() {
			return totalTermsCount;
		}

		public void setTotalTermsCount(int totalTermsCount) {
			this.totalTermsCount = totalTermsCount;
		}

		// Map<fieldName, Map<docId, termsCount>>
		public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> getDocTermsByFieldCount() {
			return docTermsByFieldCount;
		}
	}
	
	private void populateTermsStatistics(TermsStatistics termsStatistics, User user, String language) throws IOException {
		IndexReader reader = indexingService.getReader(user, language);

		Terms contentTerms = SlowCompositeReaderWrapper.wrap(reader).terms(IndexingService.CONTENT_FIELD);
		TermsEnum contentTermsEnum = contentTerms.iterator();

		int termsCount = new Long(contentTerms.size()).intValue();
		HashMap<String, Integer> termIndexes = new HashMap<>(termsCount < 0 ? 1 << 10 : termsCount); // use 2^10 as start capacity of hash map if real size is unknown
		int index = 0;

		// ставим индексы для всех термов из CONTENT_FIELD
		while (contentTermsEnum.next() != null) {
			if (contentTermsEnum.term().bytes.length > 0) {
				termIndexes.put(contentTermsEnum.term().utf8ToString(), index++);
			}
		}

		// ставим индексы для всех термов из TITLE_FIELD
		Terms titleTerms = SlowCompositeReaderWrapper.wrap(reader).terms(IndexingService.TITLE_FIELD);
		TermsEnum titleTermsEnum = titleTerms.iterator();
		while (titleTermsEnum.next() != null) {
			String termStr = titleTermsEnum.term().utf8ToString();
			if (!termIndexes.containsKey(termStr)) {
				termIndexes.put(termStr, index++);
			}
		}

		termsStatistics.setTermIndexes(termIndexes);
		termsStatistics.setTotalTermsCount(termIndexes.size());
	}

	public TermsStatistics getStatistics(User user, String language) throws IOException {
		Pair<String, String> key = Pair.of(user.getId(), language); 
		
		TermsStatistics newStatistics = new TermsStatistics();
		statisticsCache.putIfAbsent(key, newStatistics);
		TermsStatistics termsStatistics = statisticsCache.get(key);

		// если ктото вызывает метод повторно нужно дождаться наполнения статистики
		synchronized (termsStatistics) {
			if (termsStatistics == newStatistics) {
				populateTermsStatistics(newStatistics, user, language);
			}
		}
		return termsStatistics;
	}
	
	
	// =================================
	// TF-IDF calculation
	// =================================
	
	public double computeTfIdf(User user, String term, Resource resource) throws IOException {
		return computeTfIdf(user, term, resource, indexingService.getReader(resource));
	}

	public double computeTfIdf(User user, String term, Resource resource, IndexReader reader) throws IOException {
		Integer docId = indexFinder.findDocIdByResourceId(resource.getId(), reader);
		
		if (docId != null) {
			return computeTfIdf(user, term, resource.getLanguage(), docId, reader);
		} else {
			return 0;
		}
	}


	public double computeTfIdf(User user, String term, String lang, Integer docId, IndexReader reader) throws IOException {
		BytesRef targetTerm = new Term(IndexingService.CONTENT_FIELD, term).bytes();
		return computeTfIdf(reader, getStatistics(user, lang), docId, IndexingService.CONTENT_FIELD, targetTerm);
	}

	private double computeTfIdf(IndexReader reader, TermsStatistics statistics, Integer docId, String field, BytesRef targetTerm) throws IOException {
		return computeTf(reader, statistics, docId, field, targetTerm) * computeIdf(reader, statistics, field, targetTerm);
	}

	private double computeTf(IndexReader reader, TermsStatistics statistics, Integer docId, String field, BytesRef targetTerm) throws IOException {
		long start = System.nanoTime();
		PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), field, targetTerm);
		if (seekToDocument(termDocsEnum, docId)) {
			seekDocSes += System.nanoTime() - start;
			double tf = termDocsEnum.freq() / (double) countTermsInDocument(reader, statistics, field, docId);
			return new DefaultSimilarity().tf((float) tf);
		}
		return 0;
	}

	private int countTermsInDocument(IndexReader reader, TermsStatistics statistics, String field, Integer docId) throws IOException {
		long start = System.nanoTime();

		statistics.getDocTermsByFieldCount().putIfAbsent(field, new ConcurrentHashMap<>());
		Map<Integer, Integer> docTermsCount = statistics.getDocTermsByFieldCount().get(field);
		if (!docTermsCount.containsKey(docId)) {
			Terms termVector = reader.getTermVector(docId, field);

			// always -1...
//			docTermsCount.put(docId, (int) termVector.getSumTotalTermFreq());

			int count = 0;
			TermsEnum termsEnum = termVector.iterator();
			Bits liveDocs = MultiFields.getLiveDocs(reader);
			while (termsEnum.next() != null) {
				PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, liveDocs, field, termsEnum.term());
				if (seekToDocument(termDocsEnum, docId)) {
					count += termDocsEnum.freq();
				}
			}
			docTermsCount.put(docId, count);
		}
		countTermsSec += System.nanoTime() - start;
		return docTermsCount.get(docId);
	}

	public void clearDocTerms(User user, String language) throws IOException {
		getStatistics(user, language).getDocTermsByFieldCount().clear();
	}
	
	private boolean seekToDocument(PostingsEnum termDocsEnum, Integer docId) throws IOException {
		if (termDocsEnum.advance(docId) == docId) {
			return true;
		}
		return false;
	}

	private double computeIdf(IndexReader reader, TermsStatistics statistics, String field, BytesRef targetTerm) throws IOException {
		TFIDFSimilarity tfidfSIM = new DefaultSimilarity();
		TermsEnum termEnum = MultiFields.getTerms(reader, field).iterator();
		if (termEnum.seekExact(targetTerm)) {
			return tfidfSIM.idf(termEnum.docFreq(), reader.numDocs());
		}
		return 0;
	}
	
	public RealVector getTfIdf(Resource resource, String field) throws IOException {
		TermsStatistics statistics = getStatistics(resource.getOwner(), resource.getLanguage());
		RealVector tfidf = new ArrayRealVector(statistics.getTotalTermsCount());
		IndexReader reader = indexingService.getReader(resource);
		HashMap<String, Integer> termIndexes = statistics.getTermIndexes();
		
		Integer docId = indexFinder.findDocIdByResourceId(resource.getId(), reader);
		if (docId != null) {
			TermsEnum termsEnum = reader.getTermVector(docId, field).iterator();
			while (termsEnum.next() != null) {
				tfidf.setEntry(termIndexes.get(termsEnum.term().utf8ToString()), computeTfIdf(reader, statistics, docId, field, termsEnum.term()));
			}
		}

		return tfidf;
	}

}
