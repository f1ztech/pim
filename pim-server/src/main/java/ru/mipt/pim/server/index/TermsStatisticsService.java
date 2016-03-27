package ru.mipt.pim.server.index;

import java.io.IOException;

import javax.annotation.Resource;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.Bits;
import org.springframework.stereotype.Component;

@Component
public class TermsStatisticsService {

	@Resource
	private IndexFinder indexFinder;
	
	// =================================
	// TF-IDF calculation
	// =================================
	
	public float computeTfIdf(IndexReader reader, String term, String resourceId) throws IOException {
		Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);
		
		if (docId != null) {
			return computeTfIdf(reader, term, docId);
		} else {
			return 0;
		}
	}

	public float computeTfIdf(IndexReader reader, String term, Integer docId) throws IOException {
		Term targetTerm = new Term(IndexingService.CONTENT_FIELD, term);
		return computeTf(reader, docId, targetTerm) * computeIdf(reader, targetTerm);
	}

	private float computeTf(IndexReader reader, Integer docId, Term targetTerm) throws IOException {
		long start = System.nanoTime();
		PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), IndexingService.CONTENT_FIELD, targetTerm.bytes());
		if (seekToDocument(termDocsEnum, docId)) {
			IndexingService.seekDocSes += System.nanoTime() - start;
//			System.out.println("doc freq: " + (System.nanoTime() - start));
//			start = System.nanoTime();
			float tf = (float) termDocsEnum.freq() / countTermsInDocument(reader, docId);
//			System.out.println("count terms: " + (System.nanoTime() - start));
			return new DefaultSimilarity().tf(tf);
		}
		return 0;
	}

	private int countTermsInDocument(IndexReader reader, Integer docId) throws IOException {
		long start = System.nanoTime();
		Integer count = IndexingService.docTermsCount.get(docId);
		if (count == null) {
			String resourceId = reader.document(docId).get(IndexingService.ID_FIELD);
			count = IndexingService.resourceTermsCount.get(resourceId);
		
			if (count == null) {
				count = 0;
				Terms termVector = reader.getTermVector(docId, IndexingService.CONTENT_FIELD);
				TermsEnum termsEnum = termVector.iterator();
				Bits liveDocs = MultiFields.getLiveDocs(reader);
				while (termsEnum.next() != null) {
					PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, liveDocs, IndexingService.CONTENT_FIELD, termsEnum.term());
					if (seekToDocument(termDocsEnum, docId)) {
						count += termDocsEnum.freq();
					}
				}
				IndexingService.resourceTermsCount.put(resourceId, count);
			}
			
			IndexingService.docTermsCount.put(docId, count);
		}
		IndexingService.countTermsSec += System.nanoTime() - start;
		return count;
	}

	public void clearDocTerms() {
		IndexingService.docTermsCount.clear();
	}
	
	private boolean seekToDocument(PostingsEnum termDocsEnum, Integer docId) throws IOException {
		if (termDocsEnum.advance(docId) == docId ) {
			return true;
		}
		return false;
	}

	private float computeIdf(IndexReader reader, Term targetTerm) throws IOException {
		TFIDFSimilarity tfidfSIM = new DefaultSimilarity();
		TermsEnum termEnum = MultiFields.getTerms(reader, IndexingService.CONTENT_FIELD).iterator();
		if (termEnum.seekExact(targetTerm.bytes())) {
			return tfidfSIM.idf(termEnum.docFreq(), reader.numDocs());
		}
		return 0;
	}
	
}
