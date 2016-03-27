package ru.mipt.pim.adapters.fs;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.mipt.pim.adapters.fs.common.ClientFile;
import ru.mipt.pim.adapters.fs.common.ClientFileTree;
import ru.mipt.pim.adapters.fs.common.ClientFolder;
import ru.mipt.pim.adapters.fs.remote.FileSender;
import ru.mipt.pim.adapters.fs.remote.RepositoryException;

final class DirectoryWatcher {

	private DirectoryWatcherService directoryWatcherService;
	private File rootFolder;
	private String rootFolderName;
	private Path rootFolderPath;
	private FileSender fileSender;
	private WatchService watcher;
	private final Map<WatchKey, Path> watchKeys = new HashMap<WatchKey, Path>();
	private Utils utils;

	public DirectoryWatcher(Path dir, DirectoryWatcherService directoryWatcherService) {
		this.directoryWatcherService = directoryWatcherService;
		this.fileSender = directoryWatcherService.getFileSender();
		this.utils = directoryWatcherService.getUtils();

		this.rootFolderPath = dir;
		this.rootFolder = rootFolderPath.toFile();
		this.rootFolderName = rootFolder.getName();
	}

	public void startWatch() {
		try {
			// subscribe for filesystem changes
			registerWatchers();

			// perform full tree synchronization of folder with server
			synchronizeWithServer();

			// listen for new changes in folder
			listenForChanges();
		} catch (Exception e) {
			utils.logToAll("Ошибка при работе с папкой " + rootFolderName, e);
		}
		utils.logToAll("Окончена работа с папкой " + rootFolderName);
	}

	private void registerWatchers() throws IOException {
		watcher = rootFolderPath.getFileSystem().newWatchService();

		// register directory and sub-directories
		Files.walkFileTree(rootFolderPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				watchKeys.put(dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void synchronizeWithServer() throws RepositoryException {
		ClientFileTree localTree = directoryWatcherService.getLocalFileTree(rootFolder);
		ClientFileTree remoteTree = directoryWatcherService.getRemoteFileTree(rootFolder);
		
		if (localTree.getRootFolder() != null) {
			utils.debug(localTree.getRootFolder().formatTree());
		}
		if (remoteTree != null && remoteTree.getRootFolder() != null) {
			utils.debug(remoteTree.getRootFolder().formatTree());
		}

		synchronizeFolders(localTree.getRootFolder(), remoteTree == null ? null : remoteTree.getRootFolder());
	}

	private void synchronizeFolders(ClientFolder localFolder, ClientFolder remoteFolder) {
		Set<ClientFile> remainingRemoteFiles = remoteFolder == null ? Collections.emptySet() : new HashSet<ClientFile>(remoteFolder.getFiles());
		Set<ClientFolder> remainingRemoteFolders = remoteFolder == null ? Collections.emptySet() : new HashSet<ClientFolder>(remoteFolder.getSubFolders());
		
		if (remoteFolder == null) {
			fileSender.addFolder(localFolder.getPath());	
		}

		for (ClientFile localFile : localFolder.getFiles()) {
			fileSender.sendFile(localFile.getFsFile(), localFolder.getPath());
			remainingRemoteFiles.remove(localFile);
		}

		for (ClientFolder localSubFolder : localFolder.getSubFolders()) {
			remainingRemoteFolders.remove(localSubFolder);
			synchronizeFolders(localSubFolder, remoteFolder == null ? null :
					remoteFolder.getSubFolders().stream().filter(f -> f.getId().equals(localSubFolder.getId())).findFirst().orElse(null));
		}
		
		// remove files and folders not presented in local tree
		remainingRemoteFiles.forEach(clientFile -> {
			fileSender.removeFile(clientFile.getFsFile(), clientFile.getFolder().getPath());	
		});
		remainingRemoteFolders.forEach(clientFolder -> fileSender.removeFolder(clientFolder.getPath()));
	}

	private void listenForChanges() throws IOException, InterruptedException {
		while (true) {
			WatchKey watckKey = watcher.take();

			for (WatchEvent<?> event : watckKey.pollEvents()) {
				Path modifiedPath = watchKeys.get(watckKey).resolve((Path) event.context());
				File file = modifiedPath.toFile();
				Path folderPath = file.isDirectory() ? modifiedPath : modifiedPath.getParent();
				String relativePath = "/" + rootFolderPath.getParent().relativize(folderPath).toString().replace(File.separator, "/");

				if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
					if (file.isDirectory() && event.kind() == ENTRY_CREATE) {
						fileSender.addFolder(relativePath + "/" + file.getName());
					}
					if (!file.isDirectory()) {
						Thread.sleep(1000); // sleep to avoid file access denied 
						fileSender.sendFile(file, relativePath);
					}
				}

				if (event.kind() == ENTRY_DELETE) {
					if (!file.isDirectory()) {
						fileSender.removeFile(file, relativePath);
					} else {
						fileSender.removeFolder(relativePath + "/" + file.getName());
					}
				}
			}
			
			watckKey.reset();
		}
	}

	public void finishWatch() throws IOException {
		watcher.close();
		utils.logToAll("Окончено наблюдение за папкой " + rootFolderName);
	}
}