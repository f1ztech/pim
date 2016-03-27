package ru.mipt.pim.server.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.User;
import ru.mipt.pim.util.Exceptions;

@Component
public class IndexFinder {

	@Autowired
	private IndexingService indexingService;
	
	@Autowired
	private LanguageDetector languageDetector;

	
	// =================================
	// Query index
	// =================================
	
	public List<Integer> findByTagAndTerm(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(IndexingService.TAG_FIELD, tagId));
		Query contentQuery = new TermQuery(new Term(IndexingService.CONTENT_FIELD, term));
		Query titleQuery = new TermQuery(new Term(IndexingService.TITLE_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		
		BooleanQuery titleOrContentQuery = new BooleanQuery();
		titleOrContentQuery.add(contentQuery, Occur.SHOULD);
		titleOrContentQuery.add(titleQuery, Occur.SHOULD);
		finalQuery.add(titleOrContentQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		IndexingService.querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<Integer> findByTagAndContent(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(IndexingService.TAG_FIELD, tagId));
		Query contentQuery = new TermQuery(new Term(IndexingService.CONTENT_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(contentQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		IndexingService.querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<Integer> findByTagAndTitle(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(IndexingService.TAG_FIELD, tagId));
		Query titleQuery = new TermQuery(new Term(IndexingService.TITLE_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(titleQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.stream(hits).map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		IndexingService.querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<String> findIdsByText(User user, String text) throws IOException, LangDetectException, ParseException {
		DirectoryReader reader = indexingService.createIndexReader(user);
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			
			MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[] {IndexingService.TITLE_FIELD, IndexingService.CONTENT_FIELD}, 
						indexingService.createAnalyzer(languageDetector.detectLang(text)));
			Query query = multiFieldQueryParser.parse(text);
			
			ScoreDoc[] hits = searcher.search(query, 40).scoreDocs;
			return Arrays.stream(hits).map(Exceptions.wrap(hit -> { return searcher.doc(hit.doc).get(IndexingService.ID_FIELD); })).collect(Collectors.toList());
		} finally {
			reader.close();
		}
	}

	public List<Integer> findByTagAndAbstract(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(IndexingService.TAG_FIELD, tagId));
		Query abstractQuery = new TermQuery(new Term(IndexingService.ABSTRACT_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(abstractQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		IndexingService.querySec += System.nanoTime() - start;
		return ret;
	}	
	
	public Integer findDocIdByResourceId(String resourceId, IndexReader reader) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		ScoreDoc[] hits = searcher.search(new TermQuery(new Term(IndexingService.ID_FIELD, resourceId)), 1).scoreDocs;
		return hits.length == 0 ? null : hits[0].doc;
	}	
	
}
