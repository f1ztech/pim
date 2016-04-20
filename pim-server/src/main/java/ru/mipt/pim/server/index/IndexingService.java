package ru.mipt.pim.server.index;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;
import ru.mipt.pim.server.services.RepositoryService;

@Service
public class IndexingService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String ID_FIELD = "id";
	public static final String CONTENT_FIELD = "content";
	public static final String TITLE_FIELD = "title";
	public static final String ABSTRACT_FIELD = "abstract";
//	private static final String WORDS_COUNT_FIELD = "words";
	public static final String TAG_FIELD = "tagId";

	public static final String SIMILARITY_HASH_SEPARATOR = " ";
	public static final String SIMILARITY_ID_FIELD = "similarityId";
	public static final String SIMILARITY_HASH_FIELD = "similarityHash";
	
	public static class IndexableResource {
		
		private String id;
		private String title;
		private String content;
		private String abstractText;
		private String language;
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
		public String getLanguage() {
			return language;
		}
		public void setLanguage(String language) {
			this.language = language;
		}
	}
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private IndexFinder indexFinder;
	
	private ConcurrentHashMap<Pair<String, String>, IndexReader> readersCache = new ConcurrentHashMap<>();
	private Object readersLock = new Object();
	// key is <userId, lang> pair
	private ConcurrentHashMap<Pair<String, String>, IndexWriter> writersCache = new ConcurrentHashMap<>();
	private Object writersLock = new Object();
	
	private FileIndexer fileIndexer;
	
	@PostConstruct
	private void init() throws LangDetectException, URISyntaxException {
		fileIndexer = new FileIndexer(this);
		new Thread(fileIndexer).start();
	}
	
	// =================================
	// Support
	// =================================
	
	private FSDirectory getIndexDirectory(User user, String language) throws IOException {
		File userIndexFolder = new File(fileStorageService.getUserRootFolder(user), "indexes" + File.separator + "filesIndex_" + language);
		userIndexFolder.mkdirs();
		return FSDirectory.open(userIndexFolder.toPath());
	}

	public IndexReader getReader(Resource resource) throws IOException {
		return getReader(resource.getOwner(), resource.getLanguage());
	}
	
	public IndexReader getReader(User user, String language) throws IOException {
		String userId = user.getId();
		Pair<String, String> key = Pair.of(user.getId(), language);
		IndexReader reader = readersCache.get(key);
		if (reader == null) {
			synchronized (readersLock) {
				reader = readersCache.get(userId);
				if (reader == null) {
					if (!DirectoryReader.indexExists(getIndexDirectory(user, language))) {
						getWriter(user, language).commit();
					}
					reader = DirectoryReader.open(getIndexDirectory(user, language));
					readersCache.put(key, reader);
				}
			}
		}
		return reader;
	}	
	
	public IndexWriter getWriter(User user, String language) throws IOException {
		String userId = user.getId();
		Pair<String, String> key = Pair.of(user.getId(), language);
		IndexWriter writer = writersCache.get(key);
		if (writer == null) {
			synchronized (writersLock) {
				writer = writersCache.get(userId);
				if (writer == null) {
					FSDirectory indexDir = getIndexDirectory(user, language);
					IndexWriterConfig config = new IndexWriterConfig(createAnalyzer(language));
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
	
	@Async
	public <T extends Resource> void scheduleIndexing(T resource, Function<T, String> languageProvider) {
		String language = languageProvider.apply(resource);
		resource.setLanguage(language);
		repositoryService.setProperty(resource, "purl:language", f -> f.createLiteral(language));
		try {
			indexResource(resource);
		} catch (Exception e) {
			logger.error("indexing error", e);
		}
	}

	
	public void indexResource(Resource resource) throws IOException, LangDetectException {
		indexResource(resource, null);
	}
	
	public void indexResource(Resource resource, Consumer<Document> updater) throws IOException, LangDetectException {
		if (resource.getOwner() != null) {
			indexResource(resource.getOwner(), r -> {
				r.setId(resource.getId());
				r.setTitle(resource.getTitle());
				r.setContent(resource.getHtmlContent());
				r.setLanguage(resource.getLanguage());
			}, updater);
		}
	}
	
	public void indexResource(User user, Consumer<IndexableResource> consumer) throws IOException, LangDetectException {
		indexResource(user, consumer, null);
	}
	
	public void indexResource(User user, Consumer<IndexableResource> consumer, Consumer<Document> updater) throws IOException, LangDetectException {
		IndexableResource indexableResource = new IndexableResource();
		consumer.accept(indexableResource);
		indexResource(user, indexableResource);
	}
	
	public void indexResource(User user, IndexableResource resource) throws IOException, LangDetectException {
		updateDocument(user, resource.getId(), resource.getLanguage(), document -> {
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

	private void updateDocument(User user, String resourceId, String language, Consumer<Document> updater) throws IOException, LangDetectException {
		IndexReader reader = getReader(user, language);
		Integer docId = indexFinder.findDocIdByResourceId(resourceId, reader);

		Document document;
		if (docId != null) {
			document = cloneDocument(reader.document(docId));
			updater.accept(document);
		} else {
			document = new Document();
			updater.accept(document);
		}

		IndexWriter writer = getWriter(user, language);
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

	public void addTag(Resource resource, String tagId) throws IOException, LangDetectException {
		indexResource(resource, newDocument -> {
			newDocument.add(new StringField(TAG_FIELD, tagId, Store.YES));	
		});
	}
	
	public void setTags(Resource resource, List<String> tagIds) throws IOException, LangDetectException {
		indexResource(resource, newDocument -> {
			newDocument.removeFields(TAG_FIELD);
			tagIds.forEach(tagId -> newDocument.add(new StringField(TAG_FIELD, tagId, Store.YES)));	
		});
	}

	// =================================
	// Similarity Hashes
	// =================================
	
	public void storeSimilarityHashes(Resource resource, long[] hashes) throws IOException {
		IndexWriter writer = getWriter(resource.getOwner(), resource.getLanguage());
		try {
			storeSimilarityHashes(resource.getId(), hashes, writer);
		} finally {
			writer.commit();
		}			
	}

	public void storeSimilarityHashes(String resourceId, long[] hashes, IndexWriter writer) throws IOException {
		Document document = new Document();
		document.add(new StringField(SIMILARITY_ID_FIELD, resourceId, Store.YES));
		document.add(new StringField(SIMILARITY_HASH_FIELD, Arrays.stream(hashes)
				.mapToObj(String::valueOf).collect(Collectors.joining(SIMILARITY_HASH_SEPARATOR)), Store.YES));	
		
		writer.updateDocument(new Term(SIMILARITY_ID_FIELD, resourceId), document);
	}

}
