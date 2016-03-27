package ru.mipt.pim.server.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.util.Exceptions;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

@Service
public class IndexingService {

	private class IndexTask {
		private User user;
		private File ioFile;
		private ru.mipt.pim.server.model.File file;
		private Folder folder;

		public IndexTask(User user, File ioFile, ru.mipt.pim.server.model.File file, Folder folder) {
			this.user = user;
			this.ioFile = ioFile;
			this.file = file;
			this.folder = folder;
		}

		public User getUser() {
			return user;
		}

		public File getIoFile() {
			return ioFile;
		}

		public ru.mipt.pim.server.model.File getFile() {
			return file;
		}

		public Folder getFolder() {
			return folder;
		}
	}

	private class Indexer implements Runnable {
		
		private BlockingQueue<IndexTask> queue = new LinkedBlockingQueue<>();

		@Override
		public void run() {
			IndexTask nextTask;
			try {
				while ((nextTask = queue.take()) != null) {
					addFileToIndex(nextTask.getUser(), nextTask.getIoFile(), nextTask.getFile(), nextTask.getFolder());
				}
			} catch (InterruptedException e) {
				logger.info("PublicationParser interrupted");
				e.printStackTrace();
			}
		}
		
		public void scheduleIndexing(User user, File ioFile, ru.mipt.pim.server.model.File file, Folder folder) {
			queue.add(new IndexTask(user, ioFile, file, folder));
		}
		
		// =================================
		// Write index
		// =================================
		/**
		 * Gets content of the file, determines language and than add file to index
		 */
		private void addFileToIndex(User user, File ioFile, ru.mipt.pim.server.model.File file, Folder folder) {
			try {
				File contentFile = readContentToTempFile(ioFile);
				
				try {
					indexFile(user, contentFile, file.getId(), StringUtils.substringBeforeLast(file.getTitle(), "."), folder.getId());
				} finally {
					contentFile.delete();
				}
			} catch (Exception e) {
				logger.error("Error while indexing ", e);
			}
		}

		private void indexFile(User user, File contentFile, String fileId, String title, String tagId) throws LangDetectException, IOException, FileNotFoundException {
			if (contentFile.length() > 0) {
				Detector langDetector = DetectorFactory.create();
				langDetector.append(new FileReader(contentFile));
				String lang = "en";
				try {
					lang = langDetector.detect();
				} catch (LangDetectException e) {
					logger.error("Error while detecting language", e);
				}
		
				Analyzer analyzer = createAnalyzer(lang);
				try {
					IndexWriter indexWriter = createIndexWriter(user, analyzer);
					InputStreamReader contentReader = new InputStreamReader(new FileInputStream(contentFile), "UTF-8");
					String abstractText = "";
					Scanner contentScanner = new Scanner(contentFile, "UTF-8");
					int words = 0;
					while (contentScanner.hasNext() && words < 500) {
						words++;
						abstractText += contentScanner.next() + " ";
					}
					contentScanner.close();
					
					try {
						Document document = new Document();
						document.add(new Field(CONTENT_FIELD, IOUtils.toString(contentReader), createContentFieldType()));
						document.add(new Field(TITLE_FIELD, title, createContentFieldType()));
						document.add(new Field(ABSTRACT_FIELD, abstractText, createContentFieldType()));
//						document.add(new IntField(WORDS_COUNT_FIELD, countWords(contentFile), Store.YES));
						document.add(new StringField(ID_FIELD, fileId, Store.YES));
						if (tagId != null) {
							document.add(new StringField(TAG_FIELD, tagId, Store.YES));
						}
						indexWriter.addDocument(document);
					} finally {
						contentReader.close();
						indexWriter.close();
					}
				} finally {
					analyzer.close();
				}
			}
		}
		
//		private int countWords(File contentFile) throws FileNotFoundException {
//		    Scanner input = new Scanner(contentFile); 
//		    int countWords = 0;
	//
//		    while (input.hasNextLine()) {
//		        while(input.hasNext()) {
//		            input.next();
//		            countWords++;
//		        }
//		    }
//		    
//		    input.close();
//		    return countWords;
//		}

		private File readContentToTempFile(File file) throws IOException {
			Tika tika = new Tika();
			Reader contentReader = tika.parse(file);
			
			File contentFile = File.createTempFile("text_content", ".tmp");
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(contentFile), "UTF-8");
			
			try {
				char[] buffer = new char[8 * 1024];
				int charsRead;
			    while ((charsRead = contentReader.read(buffer)) != -1) {
			    	writer.write(buffer, 0, charsRead);
			    }
			} finally {
				writer.close();
				contentReader.close();
			}
			
			return contentFile;
		}
	}
	
	
	private static final String ID_FIELD = "id";
	private static final String CONTENT_FIELD = "content";
	private static final String TITLE_FIELD = "title";
	private static final String ABSTRACT_FIELD = "abstract";
//	private static final String WORDS_COUNT_FIELD = "words";
	private static final String TAG_FIELD = "tagId";
	@Resource
	private FileStorageService fileStorageService;
	
	private Map<String, Integer> resourceTermsCount = new HashMap<String, Integer>();
	private Map<Integer, Integer> docTermsCount = new HashMap<Integer, Integer>();
	
	public static long seekDocSes = 0;
	public static long countTermsSec = 0;
	public static long querySec = 0;
	public static long termVectorSec = 0;
	
	private Log logger = LogFactory.getLog(getClass());
	private Indexer indexer;
	
	@PostConstruct
	private void init() throws LangDetectException, URISyntaxException {
		DetectorFactory.loadProfile(new File(getClass().getClassLoader().getResource("META-INF/langdetect").toURI()));
		indexer = new Indexer();
		new Thread(indexer).start();
	}
	
	// =================================
	// Support
	// =================================
	
	private FSDirectory getIndexDirectory(User user) throws IOException {
		File userIndexFolder = new File(fileStorageService.getUserRootFolder(user), "indexes" + File.separator + "filesIndex");
		userIndexFolder.mkdirs();
		return FSDirectory.open(userIndexFolder.toPath());
	}
	
	public DirectoryReader createIndexReader(User user) throws IOException {
		return DirectoryReader.open(getIndexDirectory(user));
	}	
	
	private IndexWriter createIndexWriter(User user, Analyzer analyzer) throws IOException {
		FSDirectory indexDir = getIndexDirectory(user);
		
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		return new IndexWriter(indexDir, config);
	}
	
	@SuppressWarnings("resource")
	private Analyzer createAnalyzer(String lang) {
		return lang.equals("ru") ? new RussianAnalyzer() : new StandardAnalyzer();
	}
	
	public Terms getContentTerms(IndexReader reader, int docId) throws IOException {
		return reader.getTermVector(docId, CONTENT_FIELD);
	}
	
	public Terms getTitleTerms(IndexReader reader, int docId) throws IOException {
		return reader.getTermVector(docId, TITLE_FIELD);
	}

	// =================================
	// Indexing
	// =================================
	public void addFileToIndex(User user, File ioFile, ru.mipt.pim.server.model.File file, Folder folder) {
		indexer.scheduleIndexing(user, ioFile, file, folder);
	}
	
	public void indexResource(User user, ru.mipt.pim.server.model.Resource resource) throws LangDetectException, IOException {
		Analyzer analyzer = createAnalyzer(detectLang(resource.getTitle()));
		IndexWriter writer = createIndexWriter(user, analyzer);
		try {
			Document document = new Document();
			document.add(new Field(CONTENT_FIELD, resource.getHtmlContent(), createContentFieldType()));
			document.add(new Field(TITLE_FIELD, resource.getTitle(), createContentFieldType()));
			document.add(new StringField(ID_FIELD, resource.getId(), Store.YES));
			writer.addDocument(document);
		} finally {
			writer.close();
		}
	}
	
	private FieldType createContentFieldType() {
		FieldType type = new FieldType();
		type.setStored(true);
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		type.setStoreTermVectors(true);
		return type;
	}

	// =================================
	// Query index
	// =================================
	
	public List<Integer> findByTagAndTerm(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(TAG_FIELD, tagId));
		Query contentQuery = new TermQuery(new Term(CONTENT_FIELD, term));
		Query titleQuery = new TermQuery(new Term(TITLE_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		
		BooleanQuery titleOrContentQuery = new BooleanQuery();
		titleOrContentQuery.add(contentQuery, Occur.SHOULD);
		titleOrContentQuery.add(titleQuery, Occur.SHOULD);
		finalQuery.add(titleOrContentQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<Integer> findByTagAndContent(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(TAG_FIELD, tagId));
		Query contentQuery = new TermQuery(new Term(CONTENT_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(contentQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<Integer> findByTagAndTitle(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(TAG_FIELD, tagId));
		Query titleQuery = new TermQuery(new Term(TITLE_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(titleQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.stream(hits).map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		querySec += System.nanoTime() - start;
		return ret;
	}
	
	public List<String> findIdsByText(User user, String text) throws IOException, LangDetectException, ParseException {
		DirectoryReader reader = createIndexReader(user);
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
			
			MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[] {TITLE_FIELD, CONTENT_FIELD}, createAnalyzer(detectLang(text)));
			Query query = multiFieldQueryParser.parse(text);
			
			ScoreDoc[] hits = searcher.search(query, 40).scoreDocs;
			return Arrays.stream(hits).map(Exceptions.wrap(hit -> { return searcher.doc(hit.doc).get(ID_FIELD); })).collect(Collectors.toList());
		} finally {
			reader.close();
		}
	}

	private String detectLang(String text) throws LangDetectException {
		Detector langDetector = DetectorFactory.create();
		langDetector.append(text);
		String lang = langDetector.detect();
		return lang;
	}
	
	
	public List<Integer> findByTagAndAbstract(IndexReader reader, String tagId, String term) throws IOException {
		long start = System.nanoTime();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		Query tagQuery = new TermQuery(new Term(TAG_FIELD, tagId));
		Query abstractQuery = new TermQuery(new Term(ABSTRACT_FIELD, term));
		
		BooleanQuery finalQuery = new BooleanQuery();
		finalQuery.add(tagQuery, Occur.MUST);
		finalQuery.add(abstractQuery, Occur.MUST);
		
		ScoreDoc[] hits = searcher.search(finalQuery, reader.numDocs()).scoreDocs;
		
		List<Integer> ret = Arrays.asList(hits).stream().map(Exceptions.wrap(hit -> hit.doc)).collect(Collectors.toList());
		querySec += System.nanoTime() - start;
		return ret;
	}	
	
	public Integer findDocIdByResourceId(String resourceId, IndexReader reader) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		ScoreDoc[] hits = searcher.search(new TermQuery(new Term(ID_FIELD, resourceId)), 1).scoreDocs;
		return hits.length == 0 ? null : hits[0].doc;
	}	
	
	// =================================
	// Tags
	// =================================

	public void addTag(User user, String resourceId, String tagId) throws IOException {
		DirectoryReader reader = createIndexReader(user);
		IndexWriter writer = createIndexWriter(user, new StandardAnalyzer());
		try {
			Integer docId = findDocIdByResourceId(resourceId, reader);
			if (docId != null) {
				Document oldDocument = reader.document(docId);
				
				Document document = new Document();
				document.add(new Field(CONTENT_FIELD, oldDocument.get(CONTENT_FIELD), createContentFieldType()));
				document.add(new Field(TITLE_FIELD, oldDocument.get(TITLE_FIELD), createContentFieldType()));
				document.add(new Field(ABSTRACT_FIELD, oldDocument.get(ABSTRACT_FIELD), createContentFieldType()));
				document.add(new StringField(ID_FIELD, oldDocument.get(ID_FIELD), Store.YES));
//				document.add(new IntField(WORDS_COUNT_FIELD, ((IntField) oldDocument.getField(WORDS_COUNT_FIELD)).numericValue().intValue(), Store.YES));
				for (IndexableField field : oldDocument.getFields(TAG_FIELD)) {
					document.add(new StringField(TAG_FIELD, field.stringValue(), Store.YES));
				}
				document.add(new StringField(TAG_FIELD, tagId, Store.YES));
				writer.addDocument(document);
				
				writer.tryDeleteDocument(reader, docId);
			}
		} finally {
			reader.close();
			writer.close();
		}			
	}
	
	public void removeTag(User user, String resourceId, String tagId) throws IOException {
		DirectoryReader reader = createIndexReader(user);
		IndexWriter writer = createIndexWriter(user, new StandardAnalyzer());
		try {
			Integer docId = findDocIdByResourceId(resourceId, reader);
			if (docId != null) {
				writer.tryDeleteDocument(reader, docId);
			}
		} finally {
			writer.close();
			reader.close();
		}			
	}

	// =================================
	// TF-IDF calculation
	// =================================
	
	public float computeTfIdf(IndexReader reader, String term, String resourceId) throws IOException {
		Integer docId = findDocIdByResourceId(resourceId, reader);
		
		if (docId != null) {
			return computeTfIdf(reader, term, docId);
		} else {
			return 0;
		}
	}

	public float computeTfIdf(IndexReader reader, String term, Integer docId) throws IOException {
		Term targetTerm = new Term(CONTENT_FIELD, term);
		return computeTf(reader, docId, targetTerm) * computeIdf(reader, targetTerm);
	}

	private float computeTf(IndexReader reader, Integer docId, Term targetTerm) throws IOException {
		long start = System.nanoTime();
		PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), CONTENT_FIELD, targetTerm.bytes());
		if (seekToDocument(termDocsEnum, docId)) {
			seekDocSes += System.nanoTime() - start;
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
		Integer count = docTermsCount.get(docId);
		if (count == null) {
			String resourceId = reader.document(docId).get(ID_FIELD);
			count = resourceTermsCount.get(resourceId);
		
			if (count == null) {
				count = 0;
				Terms termVector = reader.getTermVector(docId, CONTENT_FIELD);
				TermsEnum termsEnum = termVector.iterator();
				Bits liveDocs = MultiFields.getLiveDocs(reader);
				while (termsEnum.next() != null) {
					PostingsEnum termDocsEnum = MultiFields.getTermDocsEnum(reader, liveDocs, CONTENT_FIELD, termsEnum.term());
					if (seekToDocument(termDocsEnum, docId)) {
						count += termDocsEnum.freq();
					}
				}
				resourceTermsCount.put(resourceId, count);
			}
			
			docTermsCount.put(docId, count);
		}
		countTermsSec += System.nanoTime() - start;
		return count;
	}

	public void clearDocTerms() {
		docTermsCount.clear();
	}
	
	private boolean seekToDocument(PostingsEnum termDocsEnum, Integer docId) throws IOException {
		if (termDocsEnum.advance(docId) == docId ) {
			return true;
		}
		return false;
	}

	private float computeIdf(IndexReader reader, Term targetTerm) throws IOException {
		TFIDFSimilarity tfidfSIM = new DefaultSimilarity();
		TermsEnum termEnum = MultiFields.getTerms(reader, CONTENT_FIELD).iterator();
		if (termEnum.seekExact(targetTerm.bytes())) {
			return tfidfSIM.idf(termEnum.docFreq(), reader.numDocs());
		}
		return 0;
	}

}
