package ru.mipt.pim.server.index;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Resource;
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
	
	public static final String SIMILARITY_ID_FIELD = "similarityId";
	public static final String SIMILARITY_HASH_FIELD = "similarityHash";
	
	public static class IndexableResource {
		
		private String id;
		private String title;
		private String content;
		private String abstractText;
		private List<String> tagIds = new ArrayList<>();
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getAbstractText() {
			return abstractText;
		}
		public void setAbstractText(String abstractText) {
			this.abstractText = abstractText;
		}
		public List<String> getTagIds() {
			return tagIds;
		}
		public void setTagIds(List<String> tagIds) {
			this.tagIds = tagIds;
		}
	}
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private IndexFinder indexFinder;
	
	@Autowired 
	private LanguageDetector languageDetector;
	
	private ConcurrentHashMap<String, IndexReader> readersCache = new ConcurrentHashMap<>();
	private Object readersLock = new Object();
	// key is <userId, lang> pair
	private ConcurrentHashMap<Pair<String, String>, IndexWriter> writersCache = new ConcurrentHashMap<>();
	private Object writersLock = new Object();
	
	private FileIndexer fileIndexer;
	
	@PostConstruct
	private void init() throws LangDetectException, URISyntaxException {
		DetectorFactory.loadProfile(new File(getClass().getClassLoader().getResource("META-INF/langdetect").toURI()));
		fileIndexer = new FileIndexer(this);
		new Thread(fileIndexer).start();
	}
	
	// =================================
	// Support
	// =================================
	
	private FSDirectory getIndexDirectory(User user) throws IOException {
		File userIndexFolder = new File(fileStorageService.getUserRootFolder(user), "indexes" + File.separator + "filesIndex");
		userIndexFolder.mkdirs();
		return FSDirectory.open(userIndexFolder.toPath());
	}
	
	public IndexReader getReader(User user) throws IOException {
		String userId = user.getId();
		IndexReader reader = readersCache.get(userId);
		if (reader == null) {
			synchronized (readersLock) {
				reader = readersCache.get(userId);
				if (reader == null) {
					reader = DirectoryReader.open(getIndexDirectory(user));
					readersCache.put(userId, reader);
				}
			}
		}
		return reader;
	}	
	
	public IndexWriter getWriter(User user) throws IOException {
		return getWriter(user, null);
	}
	
	public IndexWriter getWriter(User user, String lang) throws IOException {
		String userId = user.getId();
		Pair<String, String> key = Pair.of(user.getId(), lang);
		IndexWriter writer = writersCache.get(key);
		if (writer == null) {
			synchronized (writersLock) {
				writer = writersCache.get(userId);
				if (writer == null) {
					FSDirectory indexDir = getIndexDirectory(user);
					IndexWriterConfig config = new IndexWriterConfig(createAnalyzer(lang));
					writer = new IndexWriter(indexDir, config);
					
					writersCache.put(key, writer);
				}
			}
		}
		return writer;
	}
	
	public Analyzer createAnalyzer(String lang) {
		return "ru".equals(lang) ? new RussianAnalyzer() : new StandardAnalyzer();
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
	public void scheduleFileIndexing(User user, File ioFile, Resource resource, Folder folder) {
		fileIndexer.scheduleIndexing(user, ioFile, resource, folder);
	}
	
	public void indexResource(User user, Resource resource) throws IOException, LangDetectException {
		indexResource(user, r -> {
			r.setId(resource.getId());
			r.setTitle(resource.getTitle());
			r.setContent(resource.getHtmlContent());
		});
	}
	
	public void indexResource(User user, Consumer<IndexableResource> consumer) throws IOException, LangDetectException {
		IndexableResource indexableResource = new IndexableResource();
		consumer.accept(indexableResource);
		indexResource(user, indexableResource);
	}
	
	public void indexResource(User user, IndexableResource resource) throws IOException, LangDetectException {
		updateDocument(user, resource.getId(), document -> {
			document.add(new StringField(ID_FIELD, resource.getId(), Store.YES));
			if (resource.getTitle() != null) {
				document.add(new Field(TITLE_FIELD, resource.getTitle(), createContentFieldType()));
			}
			if (resource.getAbstractText() != null) {
				document.add(new Field(ABSTRACT_FIELD, resource.getAbstractText(), createContentFieldType()));
			}
			if (resource.getContent() != null) {
				document.add(new Field(CONTENT_FIELD, resource.getContent(), createContentFieldType()));
			}
			for (String tagId : resource.getTagIds()) {
				document.add(new StringField(IndexingService.TAG_FIELD, tagId, Store.YES));
			}
		});
	}
	
	private FieldType createContentFieldType() {
		FieldType type = new FieldType();
		type.setStored(true);
		type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		type.setStoreTermVectors(true);
		return type;
	}

	private void updateDocument(User user, String resourceId, Consumer<Document> updater) throws IOException, LangDetectException {
		IndexReader reader = getReader(user);
		Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);

		Document document;
		if (docId != null) {
			document = cloneDocument(reader.document(docId));
			updater.accept(document);
		} else {
			document = new Document();
			updater.accept(document);
		}

		IndexWriter writer = getWriter(user, languageDetector.detectLang(StringUtils.defaultIfBlank(document.get(TITLE_FIELD), document.get(CONTENT_FIELD))));
		try {			
			if (docId != null) {
				writer.updateDocument(new Term(ID_FIELD, resourceId), document);
			} else {
				writer.addDocument(document);
			}
		} finally {
			writer.commit();
		}			
	}

	private Document cloneDocument(Document oldDocument) {
		Document document = new Document();
		document.add(new Field(CONTENT_FIELD, oldDocument.get(CONTENT_FIELD), createContentFieldType()));
		document.add(new Field(TITLE_FIELD, oldDocument.get(TITLE_FIELD), createContentFieldType()));
		document.add(new Field(ABSTRACT_FIELD, oldDocument.get(ABSTRACT_FIELD), createContentFieldType()));
		document.add(new StringField(ID_FIELD, oldDocument.get(ID_FIELD), Store.YES));
//		document.add(new IntField(WORDS_COUNT_FIELD, ((IntField) oldDocument.getField(WORDS_COUNT_FIELD)).numericValue().intValue(), Store.YES));
		for (IndexableField field : oldDocument.getFields(TAG_FIELD)) {
			document.add(new StringField(TAG_FIELD, field.stringValue(), Store.YES));
		}
		return document;
	}
	
	// =================================
	// Tags
	// =================================

	public void addTag(User user, String resourceId, String tagId) throws IOException, LangDetectException {
		updateDocument(user, resourceId, newDocument -> {
			newDocument.add(new StringField(TAG_FIELD, tagId, Store.YES));	
		});
	}
	
	public void setTags(User user, String resourceId, List<String> tagIds) throws IOException, LangDetectException {
		updateDocument(user, resourceId, newDocument -> {
			newDocument.removeFields(TAG_FIELD);
			tagIds.forEach(tagId -> newDocument.add(new StringField(TAG_FIELD, tagId, Store.YES)));	
		});
	}


	// =================================
	// Similarity Hashes
	// =================================
	
	public void storeSimilarityHashes(User user, String resourceId, long[] hashes) throws IOException {
		IndexWriter writer = getWriter(user);
		try {
			storeSimilarityHashes(resourceId, hashes, writer);
		} finally {
			writer.commit();
		}			
	}

	public void storeSimilarityHashes(String resourceId, long[] hashes, IndexWriter writer) throws IOException {
		String similarityId = "similarity_" + resourceId;
		Document document = new Document();
		document.add(new StringField(SIMILARITY_ID_FIELD, similarityId, Store.YES));
		document.add(new StringField(SIMILARITY_HASH_FIELD, Arrays.stream(hashes)
				.mapToObj(String::valueOf).collect(Collectors.joining(" ")), Store.YES));	
		
		writer.updateDocument(new Term(SIMILARITY_ID_FIELD, similarityId), document);
	}

}
