package ru.mipt.pim.server.services;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.annotation.Resource;

import org.openrdf.repository.RepositoryException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.repositories.PersonRepository;
import ru.mipt.pim.util.Utilities;
import ru.mipt.pim.server.repositories.FileRepository;

@Service
public class FileService {

	@Resource
	private FileRepository fileRepository;
	
	@Resource
	private PersonRepository personRepository;
	
	
	private FileStorageService fileStorageService;
	
	public void remove(File file) {
		fileStorageService.removeFile(file.getPath());
		fileRepository.remove(file);
	}
	
	public File createNewFile(String fileName, Folder folder, String hash) throws RepositoryException {
		File file = new File();
		file.setTitle(fileName);
		file.setName(fileName);
		file.setHash(hash);
		file.setDateCreated(new Date());
		fileRepository.save(file);
		fileRepository.addNarrowerResource(folder, file);
		return file;
	}

	@Transactional
	public void updateFile(File destination, File source) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Utilities.copyObject(destination, source, "folder", "folderId", "id", "rdfId");
		fileRepository.merge(destination);
	}
	
}
