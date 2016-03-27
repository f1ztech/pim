package ru.mipt.pim.server.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.Tika;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.User;

class Indexer implements Runnable {
	
	class IndexTask {
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

	/**
	 * 
	 */
	private final IndexingService indexingService;

	/**
	 * @param indexingService
	 */
	Indexer(IndexingService indexingService) {
		this.indexingService = indexingService;
	}

	private BlockingQueue<IndexTask> queue = new LinkedBlockingQueue<>();

	@Override
	public void run() {
		IndexTask nextTask;
		try {
			while ((nextTask = queue.take()) != null) {
				addFileToIndex(nextTask.getUser(), nextTask.getIoFile(), nextTask.getFile(), nextTask.getFolder());
			}
		} catch (InterruptedException e) {
			this.indexingService.logger.info("PublicationParser interrupted");
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
			this.indexingService.logger.error("Error while indexing ", e);
		}
	}

	private void indexFile(User user, File contentFile, String fileId, String title, String tagId) throws LangDetectException, IOException, FileNotFoundException {
		if (contentFile.length() > 0) {
			String lang = this.indexingService.languageDetector.detectLang(contentFile);

			Analyzer analyzer = this.indexingService.createAnalyzer(lang);
			try {
				IndexWriter indexWriter = this.indexingService.createIndexWriter(user, analyzer);
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
					document.add(new Field(IndexingService.CONTENT_FIELD, IOUtils.toString(contentReader), this.indexingService.createContentFieldType()));
					document.add(new Field(IndexingService.TITLE_FIELD, title, this.indexingService.createContentFieldType()));
					document.add(new Field(IndexingService.ABSTRACT_FIELD, abstractText, this.indexingService.createContentFieldType()));
					// document.add(new IntField(WORDS_COUNT_FIELD,
					// countWords(contentFile), Store.YES));
					document.add(new StringField(IndexingService.ID_FIELD, fileId, Store.YES));
					if (tagId != null) {
						document.add(new StringField(IndexingService.TAG_FIELD, tagId, Store.YES));
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

	// private int countWords(File contentFile) throws FileNotFoundException {
	// Scanner input = new Scanner(contentFile);
	// int countWords = 0;
	//
	// while (input.hasNextLine()) {
	// while(input.hasNext()) {
	// input.next();
	// countWords++;
	// }
	// }
	//
	// input.close();
	// return countWords;
	// }

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
