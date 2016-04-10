package ru.mipt.pim.server.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Component;

import ru.mipt.pim.server.model.User;

@Component
public class TermsStatisticsService {

	public static long seekDocSes = 0;
	public static long countTermsSec = 0;
	public static long querySec = 0;
	public static long termVectorSec = 0;
	
	@Resource
	private IndexFinder indexFinder;
	
	@Resource
	private IndexingService indexingService;

	private ConcurrentHashMap<User, TermsStatistics> statisticsCache = new ConcurrentHashMap<>();
	
	// ====================================================
	// TermsStatistics
	// ====================================================

	private class TermsStatistics {
		
		private HashMap<BytesRef, Integer> termIndexes = new HashMap<>();
		private int totalTermsCount;
		private Map<Integer, Integer> docTermsCount = new HashMap<>();

		public HashMap<BytesRef, Integer> getTermIndexes() {
			return termIndexes;
		}

		public void setTermIndexes(HashMap<BytesRef, Integer> termIndexes) {
			this.termIndexes = termIndexes;
		}

		public int getTotalTermsCount() {
			return totalTermsCount;
		}

		public void setTotalTermsCount(int totalTermsCount) {
			this.totalTermsCount = totalTermsCount;
		}

		public Map<Integer, Integer> getDocTermsCount() {
			return docTermsCount;
		}
	}
	
	public void populateTermsStatistics(User user) throws IOException {
		IndexReader reader = indexingService.getReader(user);
		
		Terms contentTerms = SlowCompositeReaderWrapper.wrap(reader).terms(IndexingService.CONTENT_FIELD);
		TermsEnum contentTermsEnum = contentTerms.iterator();
		
		int termsCount = new Long(contentTerms.size()).intValue();
		HashMap<BytesRef, Integer> termIndexes = new HashMap<BytesRef, Integer>(termsCount < 0 ? 1 << 10 : termsCount); // use 2^10 as start capacity of hash map if real size is unknown
		
		int index = 0;
		while (contentTermsEnum != null) {
			termIndexes.put(contentTermsEnum.term(), index++);
		}
		
		Terms titleTerms = SlowCompositeReaderWrapper.wrap(reader).terms(IndexingService.TITLE_FIELD);
		TermsEnum titleTermsEnum = titleTerms.iterator();
		while (titleTermsEnum != null) {
			if (!termIndexes.containsKey(titleTermsEnum.term())) {
				termIndexes.put(titleTermsEnum.term(), index++);
			}
		}

		TermsStatistics termsStatistics = getStatistics(user);
		termsStatistics.setTermIndexes(termIndexes);
		termsStatistics.setTotalTermsCount(termIndexes.size());
	}

	public TermsStatistics getStatistics(User user) throws IOException {
		TermsStatistics newStatistics = new TermsStatistics();
		statisticsCache.putIfAbsent(user, newStatistics);
		TermsStatistics termsStatistics = statisticsCache.get(user);
		if (termsStatistics == newStatistics) {
			populateTermsStatistics(user);
		}
		return termsStatistics;
	}
	
	
	// =================================
	// TF-IDF calculation
	// =================================
	
	public double computeTfIdf(User user, String term, String resourceId) throws IOException {
		return computeTfIdf(user, term, resourceId, indexingService.getReader(user));
	}

	public double computeTfIdf(User user, String term, String resourceId, IndexReader reader) throws IOException {
		Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);
		
		if (docId != null) {
			return computeTfIdf(user, term, docId);
		} else {
			return 0;
		}
	}

	public double computeTfIdf(User user, String term, Integer docId) throws IOException {
		return computeTfIdf(user, term, docId, indexingService.getReader(user));
	}

	public double computeTfIdf(User user, String term, Integer docId, IndexReader reader) throws IOException {
		BytesRef targetTerm = new Term(IndexingService.CONTENT_FIELD, term).bytes();
		return computeTfIdf(reader, getStatistics(user), docId, IndexingService.CONTENT_FIELD, targetTerm);
	}

	private double computeTfIdf(IndexReader reader, TermsStatistics statistics, Integer docId, String field, BytesRef targetTerm) throws IOException {
		return computeTf(reader, statistics, docId, field, targetTerm) * computeIdf(reader, statistics, field, targetTerm);
	}

	private double computeTf(IndexReader reader, TermsStatistics statistics, Integer docId, String field, BytesRef targetTerm) throws IOException {
		long start = System.nanoTime();
		PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), field, targetTerm);
		if (seekToDocument(termDocsEnum, docId)) {
			seekDocSes += System.nanoTime() - start;
			double tf = termDocsEnum.freq() / countTermsInDocument(reader, statistics, field, docId);
			return new DefaultSimilarity().tf((float) tf);
		}
		return 0;
	}

	private int countTermsInDocument(IndexReader reader, TermsStatistics statistics, String field, Integer docId) throws IOException {
		long start = System.nanoTime();
		Integer count = statistics.getDocTermsCount().get(docId);
		if (count == null) {
			count = 0;
			Terms termVector = reader.getTermVector(docId, field);
			statistics.getDocTermsCount().put(docId, (int) termVector.getSumTotalTermFreq());
			
			// TODO correct?
			
//			TermsEnum termsEnum = termVector.iterator();
//			Bits liveDocs = MultiFields.getLiveDocs(reader);
//			while (termsEnum.next() != null) {
//				PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, liveDocs, field, termsEnum.term());
//				if (seekToDocument(termDocsEnum, docId)) {
//					count += termDocsEnum.freq();
//				}
//			}
//			statistics.getDocTermsCount().put(docId, count);
		}
		countTermsSec += System.nanoTime() - start;
		return count;
	}

	public void clearDocTerms(User user) throws IOException {
		getStatistics(user).getDocTermsCount().clear();
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
	
	public RealVector getTfIdf(User user, Integer docId, String field) throws IOException {
		TermsStatistics statistics = getStatistics(user);
		RealVector tfidf = new ArrayRealVector(statistics.getTotalTermsCount());
		IndexReader reader = indexingService.getReader(user);
		HashMap<BytesRef, Integer> termIndexes = statistics.getTermIndexes();
		
		TermsEnum termsEnum = reader.getTermVector(docId, field).iterator();
		while (termsEnum.next() != null) {
			tfidf.setEntry(termIndexes.get(termsEnum.term()), computeTfIdf(reader, statistics, docId, field, termsEnum.term()));
		}
		
		return tfidf;
	}

}
