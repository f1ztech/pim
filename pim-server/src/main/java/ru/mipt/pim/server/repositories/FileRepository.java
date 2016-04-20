package ru.mipt.pim.server.repositories;

import javax.annotation.Resource;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.index.LanguageDetector;
import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.services.FileStorageService;

@Service
public class FileRepository extends CommonResourceRepository<File> {

	@Autowired
	private LanguageDetector languageDetector;

	@Resource
	private FileStorageService fileStorageService;
	
	public FileRepository() {
		super(File.class);
	}
	
	@Override
	protected String detectLanguage(File resource) {
		java.io.File ioFile = null;
		if (resource.getPath() != null) {
			ioFile = new java.io.File(fileStorageService.getAbsolutePath(resource.getPath()));
		}
		return ioFile != null && ioFile.exists() ? languageDetector.detectLang(ioFile) : super.detectLanguage(resource);
	}

	public File findByNameAndFolderAndHash(User user, String name, String folderPath, String hash) {
		Query query = prepareQuery(
				" where { " +
				"		 ?result <http://purl.org/dc/terms/title> ??fileName. " +
				"		 ?result <http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#hashValue> ??hash. " +
				"		 ?folder <http://mipt.ru/pim/narrowerResource> ?result. " +
				"		 ?folder <http://mipt.ru/pim/path> ??folderPath. " +
				" 		 ?folder <http://mipt.ru/pim/owner> ?user." +
				"		 ?user <http://xmlns.com/foaf/0.1/nick> ??login " +				
				" } ");
		query.setParameter("fileName", name);
		query.setParameter("hash", hash);
		query.setParameter("folderPath", folderPath);
		query.setParameter("login", user.getLogin());
		
		return getFirst(query);
	}
	
	public File findByNameAndFolder(User user, String name, String folderPath) {
		Query query = prepareQuery(
				" where { " +
				"		 ?result <http://purl.org/dc/terms/title> ??fileName. " +
				"		 ?folder <http://mipt.ru/pim/narrowerResource> ?result. " +
				"		 ?folder <http://mipt.ru/pim/path> ??folderPath. " +
				" 		 ?folder <http://mipt.ru/pim/owner> ?user." +
				"		 ?user <http://xmlns.com/foaf/0.1/nick> ??login " +				
				" } ");
		query.setParameter("fileName", name);
		query.setParameter("folderPath", folderPath);
		query.setParameter("login", user.getLogin());
		
		return getFirst(query);
	}
	
	public File findById(String id) {
		return em.find(File.class, "http://mipt.ru/pim/file#" + id);
	}

}
