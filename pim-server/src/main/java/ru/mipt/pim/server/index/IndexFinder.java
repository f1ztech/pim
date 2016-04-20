package ru.mipt.pim.server.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.util.Exceptions;
import ru.mipt.pim.util.Exceptions.FunctionWithExceptions;

@Component
public class IndexFinder {

	@Autowired
	@Lazy
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
		
		List<Integer> ret = getDocIds(hits);
		TermsStatisticsService.querySec += System.nanoTime() - start;
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
		
		List<Integer> ret = getDocIds(hits);
		TermsStatisticsService.querySec += System.nanoTime() - start;
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
		
		List<Integer> ret = getDocIds(hits);
		TermsStatisticsService.querySec += System.nanoTime() - start;
		return ret;
	}

	public List<String> findIdsByText(User user, String text) throws IOException, LangDetectException, ParseException {
		String language = languageDetector.detectLang(text);
		
		IndexReader reader = indexingService.getReader(user, language);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(
				new String[] {IndexingService.TITLE_FIELD, IndexingService.CONTENT_FIELD}, 
				indexingService.createAnalyzer(language)
		);
		Query query = multiFieldQueryParser.parse(text);
		
		ScoreDoc[] hits = searcher.search(query, 40).scoreDocs;
		return getResourceIds(searcher, hits);
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
		TermsStatisticsService.querySec += System.nanoTime() - start;
		return ret;
	}	
	
	public Integer findDocIdByResourceId(String resourceId, IndexReader reader) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		ScoreDoc[] hits = searcher.search(new TermQuery(new Term(IndexingService.ID_FIELD, resourceId)), 1).scoreDocs;
		return hits.length == 0 ? null : hits[0].doc;
	}
	
	public List<String> findAllIndexedResourceIds(User user) throws IOException {
		List<String> ret = findIndexedResourcesIds(user, "ru");
		ret.addAll(findIndexedResourcesIds(user, "en"));
		return ret;
	}

	private List<String> findIndexedResourcesIds(User user, String lang) throws IOException {
		IndexSearcher searcher = new IndexSearcher(indexingService.getReader(user, lang));
		ScoreDoc[] hits = searcher.search(new MatchAllDocsQuery(), indexingService.getReader(user, lang).numDocs()).scoreDocs;
		return getResourceIds(searcher, hits);
	}

	// =========================================
	// Similarity
	// =========================================
	
	public long[] getSimilarityHashes(Resource resource) throws IOException {
		IndexReader reader = indexingService.getReader(resource);
		Integer docId = getSimilarityDocId(reader, resource.getId());
		if (docId != null) {
			String[] hashStrings = reader.document(docId).get(IndexingService.SIMILARITY_HASH_FIELD).split(IndexingService.SIMILARITY_HASH_SEPARATOR);
			return Arrays.stream(hashStrings).mapToLong(Long::parseLong).toArray();
		} else {
			return null;
		}
	}

	public List<String> findSimilarHashResources(Resource resource, long[] hashes, int count) throws IOException {
		List<String> hashStrings = Arrays.stream(hashes).mapToObj(String::valueOf).collect(Collectors.toList());
		return findBySimilarityHashes(indexingService.getReader(resource), hashStrings, count);
	}
	
	public Integer getSimilarityDocId(IndexReader reader, String similarityId) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		ScoreDoc[] hits = searcher.search(new TermQuery(new Term(IndexingService.SIMILARITY_ID_FIELD, similarityId)), 1).scoreDocs;
		return hits.length == 0 ? null : hits[0].doc;
	}

	public List<String> findBySimilarityHashes(IndexReader reader, List<String> hashes, int count) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		
		BooleanQuery finalQuery = new BooleanQuery();
		
		for (String hash : hashes) {
			Query query = new TermQuery(new Term(IndexingService.SIMILARITY_HASH_FIELD, hash));
			finalQuery.add(query, Occur.SHOULD);
		}
		
		ScoreDoc[] hits = searcher.search(finalQuery, count).scoreDocs;
		return mapSearchResults(hits, hit -> searcher.doc(hit.doc).get(IndexingService.SIMILARITY_ID_FIELD));
	}
	
	// =========================================
	// Support
	// =========================================

	private List<String> getResourceIds(IndexSearcher searcher, ScoreDoc[] hits) {
		return mapSearchResults(hits, hit -> searcher.doc(hit.doc).get(IndexingService.ID_FIELD));
	}

	private List<Integer> getDocIds(ScoreDoc[] hits) {
		return mapSearchResults(hits, hit -> hit.doc);
	}
	
	private <R> List<R> mapSearchResults(ScoreDoc[] hits, FunctionWithExceptions<ScoreDoc, R> runnable) {
		return Arrays.stream(hits).map(Exceptions.wrap(runnable)).collect(Collectors.toList());
	}

}
