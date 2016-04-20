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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.LangDetectException;

import ru.mipt.pim.server.index.IndexingService.IndexableResource;
import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;

class FileIndexer implements Runnable {
	
	class IndexTask {
		private User user;
		private File ioFile;
		private Resource resource;
		private Folder folder;
	
		public IndexTask(User user, File ioFile, Resource resource, Folder folder) {
			this.user = user;
			this.ioFile = ioFile;
			this.resource = resource;
			this.folder = folder;
		}
	
		public User getUser() {
			return user;
		}
	
		public File getIoFile() {
			return ioFile;
		}
	
		public Resource getResource() {
			return resource;
		}
	
		public Folder getFolder() {
			return folder;
		}
	}

	private final IndexingService indexingService;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	FileIndexer(IndexingService indexingService) {
		this.indexingService = indexingService;
	}

	private BlockingQueue<IndexTask> queue = new LinkedBlockingQueue<>();

	@Override
	public void run() {
		IndexTask nextTask;
		try {
			while ((nextTask = queue.take()) != null) {
				addResourceToIndex(nextTask.getUser(), nextTask.getIoFile(), nextTask.getResource(), nextTask.getFolder());
			}
		} catch (InterruptedException e) {
			logger.info("PublicationParser interrupted");
			e.printStackTrace();
		}
	}

	public void scheduleIndexing(User user, File ioFile, Resource resource, Folder folder) {
		queue.add(new IndexTask(user, ioFile, resource, folder));
	}

	// =================================
	// Write index
	// =================================
	/**
	 * Gets content of the file, determines language and than add file to index
	 */
	private void addResourceToIndex(User user, File ioFile, Resource resource, Folder folder) {
		try {
			File contentFile = readContentToTempFile(ioFile);

			try {
				indexResource(user, contentFile, resource, StringUtils.substringBeforeLast(resource.getTitle(), "."), folder.getId());
			} finally {
				contentFile.delete();
			}
		} catch (Exception e) {
			logger.error("Error while indexing ", e);
		}
	}

	private void indexResource(User user, File contentFile, Resource resource, String title, String tagId) throws LangDetectException, IOException, FileNotFoundException {
		String content = null;
		String abstractText = null;
		
		if (contentFile != null && contentFile.length() > 0) {
			InputStreamReader contentReader = new InputStreamReader(new FileInputStream(contentFile), "UTF-8");
			Scanner contentScanner = new Scanner(contentFile, "UTF-8");
			int words = 0;
			while (contentScanner.hasNext() && words < 500) {
				words++;
				abstractText += contentScanner.next() + " ";
			}
			contentScanner.close();
			content = IOUtils.toString(contentReader);
		}

		IndexableResource indexableResource = new IndexableResource();
		indexableResource.setId(resource.getId());
		indexableResource.setLanguage(resource.getLanguage());
		indexableResource.setTitle(title);
		indexableResource.setContent(content);
		indexableResource.setAbstractText(abstractText);
		indexableResource.getTagIds().add(tagId);

		indexingService.indexResource(user, indexableResource);
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
