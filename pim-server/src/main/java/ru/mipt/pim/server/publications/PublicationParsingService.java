package ru.mipt.pim.server.publications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Person;
import ru.mipt.pim.server.model.Publication;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.FileRepository;
import ru.mipt.pim.server.repositories.PersonRepository;
import ru.mipt.pim.server.repositories.PublicationRepository;
import ru.mipt.pim.server.services.FileStorageService;
import ru.mipt.pim.server.services.RepositoryService;
import ru.mipt.pim.server.services.UserService;

@Service
public class PublicationParsingService {

	private static int MAX_PUBLICATION_PARSERS = 5;
	private static int MAX_PARSER_BUSYNESS = 5;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	// currently working parsers. May be up to MAX_PUBLICATION_PARSERS
	// concurrently working instances.
	private List<PublicationParser> parsers = new ArrayList<PublicationParser>();

	@Autowired
	private PublicationRepository publicationRepository;
	
	@Autowired
	private PersonRepository personRepository;
	
	@Autowired
	private FileRepository fileRepository;
	
	@Autowired
	private GrobidService grobidService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	public class PublicationParser implements Runnable {
		
		private BlockingQueue<Pair<User, File>> queue = new LinkedBlockingQueue<>();
		private Thread thread;

		public void parsePublication(User currentUser, File file) {
			queue.add(Pair.of(currentUser, file)); // here we only queue parsing, parse later
		}
		
		public long getBusyness() {
			return queue.size();
		}
		
		// -- parsing
		
		public void start() {
			thread = new Thread(this, "PublicationParser_" + parsers.size());
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

		private void saveArticle(User user, File file, Article article) throws RepositoryException {
			if (article.getTitle() == null) { // not a publication
				return;
			}
			
			Publication publication = articleToPublication(article, user);
			publication.setPublicationFile(file);
			publicationRepository.merge(publication);
			
			// replace all occurrences of file to publication
			for (Resource broaderResource : file.getBroaderResources()) {
				repositoryService.removeNarrowerResource(broaderResource, file);
				repositoryService.addNarrowerResource(broaderResource, publication);
			}
			
			file.setOwner(null);
			fileRepository.merge(file);
		}

		private Publication articleToPublication(Article article, User user) {
			List<Publication> publications = publicationRepository.findByTitle(user, article.getTitle());
			Publication publication;
			
			if (publications.isEmpty()) {
				publication = new Publication();
				
				publication.setTitle(article.getTitle());
//				publicationRepository.save(publication);
				
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
				
//				publicationRepository.merge(publication);
			} else {
				publication = publications.get(0);
			}
			
			return publication;
		}
	}

	public void scheduleParsing(User currentUser, File file) {
		PublicationParser parser = getLeastBusyParser();

		// when we has more than MAX_PARSER_BUSYNESS active publications per parser
		// create new parser
		if (parser == null || parser.getBusyness() > MAX_PARSER_BUSYNESS && parsers.size() < MAX_PUBLICATION_PARSERS) {
			try {
				parser = new PublicationParser();
				parsers.add(parser);
				parser.start();
			} catch (Exception e) {
				logger.error("Error while creating parser", e);
			}
		}

		parser.parsePublication(currentUser, file);
	}

	private PublicationParser getLeastBusyParser() {
		long min = Long.MAX_VALUE;
		PublicationParser result = null;
		for (PublicationParser parser : parsers) {
			if (min > parser.getBusyness()) {
				result = parser;
				min = parser.getBusyness();
			}
		}
		return result;
	}
}
