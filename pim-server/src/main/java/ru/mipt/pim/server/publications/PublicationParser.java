	package ru.mipt.pim.server.publications;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.mipt.pim.server.adapters.fs.FileAdapterController;
import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Person;
import ru.mipt.pim.server.model.Publication;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.FileRepository;
import ru.mipt.pim.server.repositories.PersonRepository;
import ru.mipt.pim.server.repositories.PublicationRepository;
import ru.mipt.pim.server.services.FileStorageService;

public class PublicationParser implements Runnable {

	private static final Log logger = LogFactory.getLog(FileAdapterController.class);
	
	private BlockingQueue<Pair<User, File>> queue = new LinkedBlockingQueue<>();
	private Thread thread;

	private PublicationRepository publicationRepository;
	
	private FileRepository fileRepository;
	
	private PersonRepository personRepository;
	
	private FileStorageService fileStorageService;

	private GrobidService grobidService;

	public PublicationParser(PublicationRepository publicationRepository, FileRepository fileRepository, PersonRepository personRepository, FileStorageService fileStorageService, GrobidService grobidService) throws Exception {
		this.publicationRepository = publicationRepository;
		this.fileRepository = fileRepository;
		this.personRepository = personRepository;
		this.fileStorageService = fileStorageService;
		this.grobidService = grobidService;
	}
	
	public void parsePublication(User currentUser, File file) {
		queue.add(Pair.of(currentUser, file)); // here we only queue parsing, parse later
	}
	
	public long getBusyness() {
		return queue.size();
	}
	
	// -- parsing
	
	public void start() {
		thread = new Thread(this, "PublicationParser");
		thread.start();
	}

	@Override
	public void run() {
		Pair<User, File> nextPair;
		try {
			while ((nextPair = queue.take()) != null) {
				realParsePublication(nextPair.getLeft(), nextPair.getRight());
			}
		} catch (InterruptedException e) {
			logger.info("PublicationParser interrupted");
			e.printStackTrace();
		}
	}
	
	private void realParsePublication(User user, File file) {
		try {
		    Article article = parseFile(file);
		    if (article != null) {
			    saveArticle(user, file, article);
		    }
		} catch (Exception e) {
			logger.error("Error while parsing publication " + file.getId(), e);
		}
	}
	
	private Article parseFile(File file) throws Exception {
		if (FilenameUtils.getExtension(file.getName()).equals("pdf")) {
			try {
				java.io.File ioFile = new java.io.File(fileStorageService.getAbsolutePath(file.getPath()));
				TEIParser teiParser = new TEIParser();
				
				String headerTei = grobidService.processHeader(ioFile);
				Article article = teiParser.parseTei(headerTei);
				
				String referenceTei = grobidService.processReferences(ioFile);
				teiParser.parseTei(referenceTei, article);
				
				return article;
			} catch (Exception e) {
				logger.error("Error while processing file " + file.getName(), e);
				return null;
			}
		} else {
			return null;
		}
	}

	private void saveArticle(User user, File file, Article article) {
		if (article.getTitle() == null) { // not a publication
			return;
		}
		
		Publication publication = articleToPublication(article, user);
		
		// replace all occurrences of file to publication
		for (Resource broaderResource : file.getBroaderResources()) {
			broaderResource.getNarrowerResources().add(publication);
		}
		file.getBroaderResources().clear();
		publication.setPublicationFile(file);
		
		fileRepository.merge(file);
		publicationRepository.merge(publication);
	}

	private Publication articleToPublication(Article article, User user) {
		List<Publication> publications = publicationRepository.findByTitle(user, article.getTitle());
		Publication publication;
		
		if (publications.isEmpty()) {
			publication = new Publication();
			
			publication.setTitle(article.getTitle());
			publicationRepository.save(publication);
			
			for (Author author : article.getAuthors()) {
				Person person = new Person();
				person.setFirstName(author.getFirstName());
				person.setLastName(author.getLastName());
				person.setMiddlename(author.getMiddleName());
				publication.getAuthors().add(person);
				
				personRepository.save(person);
			}
			
			publication.setAbstractText(article.getAbstractText());
			publication.setPageRange(article.getPages());
			publication.setPublicationDate(article.getDate());
			publication.setYear(article.getYear());
			publication.setPublicationName(article.getPublishedIn());
			publication.setPublisher(article.getPublisher());
			
			publication.getKeywords().addAll(article.getKeywors());
			
			for (Article citedArticle : article.getReferences()) {
				Publication citedPublication = articleToPublication(citedArticle, user);
				publication.getCites().add(citedPublication);
			}
			
			publicationRepository.merge(publication);
		} else {
			publication = publications.get(0);
		}
		
		return publication;
	}
}
