package ru.mipt.pim.server.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import ru.mipt.pim.server.model.User;

@Service
public class FileStorageService {

	@Resource(name = "appProperties")
	private Properties properties;

	private String rootFolder;

	@PostConstruct
	private void init() {
		rootFolder = properties.getProperty("file.root");
	}

	public String storeFile(User user, InputStream fileStream) throws IOException {
		File storedFile = new File(getUserRootFolder(user), UUID.randomUUID().toString());
		storedFile.createNewFile();
		FileOutputStream output = new FileOutputStream(storedFile);
		IOUtils.copy(fileStream, output);
		IOUtils.closeQuietly(fileStream);

		return user.getId() + File.separator + storedFile.getName();
	}

	public File getUserRootFolder(User user) {
		File rootFolderFile = new File(rootFolder + File.separator + user.getId());
		rootFolderFile.mkdirs();
		return rootFolderFile;
	}
	
	public boolean removeFile(String path) {
		File file = new File(getAbsolutePath(path));
		return file.delete();
	}

	public String getAbsolutePath(String path) {
		return rootFolder + File.separator + path;
	}
	
}
