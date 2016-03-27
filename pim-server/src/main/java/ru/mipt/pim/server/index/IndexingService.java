package ru.mipt.pim.server.index;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

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
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

@Service
public class IndexingService {

	public static final String ID_FIELD = "id";
	public static final String CONTENT_FIELD = "content";
	public static final String TITLE_FIELD = "title";
	public static final String ABSTRACT_FIELD = "abstract";
//	private static final String WORDS_COUNT_FIELD = "words";
	public static final String TAG_FIELD = "tagId";
	
	@Resource
	private FileStorageService fileStorageService;
	
	@Resource
	private IndexFinder indexFinder;
	
	@Autowired LanguageDetector languageDetector;
	
	public static Map<String, Integer> resourceTermsCount = new HashMap<String, Integer>();
	public static Map<Integer, Integer> docTermsCount = new HashMap<Integer, Integer>();
	
	public static long seekDocSes = 0;
	public static long countTermsSec = 0;
	public static long querySec = 0;
	public static long termVectorSec = 0;
	
	Log logger = LogFactory.getLog(getClass());
	
	private Indexer indexer;
	
	@PostConstruct
	private void init() throws LangDetectException, URISyntaxException {
		DetectorFactory.loadProfile(new File(getClass().getClassLoader().getResource("META-INF/langdetect").toURI()));
		indexer = new Indexer(this);
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
	
	public IndexWriter createIndexWriter(User user, Analyzer analyzer) throws IOException {
		FSDirectory indexDir = getIndexDirectory(user);
		
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		return new IndexWriter(indexDir, config);
	}
	
	public Analyzer createAnalyzer(String lang) {
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
		Analyzer analyzer = createAnalyzer(languageDetector.detectLang(resource.getTitle()));
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
	
	FieldType createContentFieldType() {
		FieldType type = new FieldType();
		type.setStored(true);
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		type.setStoreTermVectors(true);
		return type;
	}

	// =================================
	// Tags
	// =================================

	public void addTag(User user, String resourceId, String tagId) throws IOException {
		DirectoryReader reader = createIndexReader(user);
		IndexWriter writer = createIndexWriter(user, new StandardAnalyzer());
		try {
			Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);
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
			Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);
			if (docId != null) {
				writer.tryDeleteDocument(reader, docId);
			}
		} finally {
			writer.close();
			reader.close();
		}			
	}

}
