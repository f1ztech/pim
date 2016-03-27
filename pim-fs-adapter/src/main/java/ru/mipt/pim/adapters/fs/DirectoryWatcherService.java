package ru.mipt.pim.adapters.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import ru.mipt.pim.adapters.fs.common.ClientFile;
import ru.mipt.pim.adapters.fs.common.ClientFileTree;
import ru.mipt.pim.adapters.fs.common.ClientFolder;
import ru.mipt.pim.adapters.fs.common.FsAdapterUtils;
import ru.mipt.pim.adapters.fs.remote.FileSender;
import ru.mipt.pim.adapters.fs.remote.RepositoryEndpoint;
import ru.mipt.pim.adapters.fs.remote.RepositoryException;

@Service
public class DirectoryWatcherService {

	public static interface WatchedDirectoriesListener {
		public void directoryAdded(String directory);

		public void directoryRemoved(String directory);
	}

	@Resource
	private ConfigsService configService;

	@Resource 
	private FileSender fileSender;
	
	@Resource
	private RepositoryEndpoint repositoryEndpoint; 
	
	@Resource 
	private Utils utils;

	private Map<String, DirectoryWatcher> directoryWatchers = new HashMap<String, DirectoryWatcher>();
	private List<WatchedDirectoriesListener> watchedDirectoriesListener = new ArrayList<>();
	private Map<String, ClientFileTree> remoteFileTrees;
	private Map<String, ClientFileTree> localFileTrees = new HashMap<String, ClientFileTree>();

	public void startWatch() {
		try {
			populateRemoteFileTrees();
			List<String> localFolders = getWatchedDirectories().stream().map(f -> f.getName()).collect(Collectors.toList());
			remoteFileTrees.keySet().forEach(remoteFolder -> {
				if (!localFolders.contains(remoteFolder)) {
					fileSender.removeFolder("/" + remoteFolder);
				}
			});
		} catch (RepositoryException e) {
			utils.logToAll("Ошибка при получении списка файлов с сервера...", e);
		}
		
		for (File dir : getWatchedDirectories()) {
			watchDirectory(dir.toPath());
		}
		
	}

	public void watchDirectory(Path dir) {
		File rootFolder = dir.toFile();
		String rootFolderName = rootFolder.getName();
		
		if (rootFolder.exists()) {
			new Thread(new Runnable() {
				public void run() {
					DirectoryWatcher directoryWatcher = new DirectoryWatcher(dir, DirectoryWatcherService.this);
					directoryWatchers.put(rootFolderName, directoryWatcher);
					directoryWatcher.startWatch();
				};
			}, "DirectoryWatcher_" + rootFolderName).start();
		} else {
			utils.logToAll("Корневая директория " + dir + " не найдена. Удаление с сервера...");
			fileSender.removeFolder(rootFolderName);
		}
	}

	/**
	 * @return fileTree for given root folder created from user file system
	 */
	public ClientFileTree getLocalFileTree(File rootFolder) {
		if (localFileTrees.get(rootFolder) == null) {
			localFileTrees.put(rootFolder.getName(), populateFileTree(rootFolder));
		}
		return localFileTrees.get(rootFolder.getName());
	}

	private ClientFileTree populateFileTree(File folder) {
		ClientFileTree fileTree = new ClientFileTree();
		ClientFolder rootFolder = new ClientFolder();
		fileTree.setRootFolder(rootFolder);
		
		populateFolder("", folder, rootFolder);

		return fileTree;
	}
	
	private void populateFolder(String path, File folder, ClientFolder clientFolder) {
		clientFolder.setFsFile(folder);
		clientFolder.setName(folder.getName());
		clientFolder.setPath(path + "/" + folder.getName());
		clientFolder.setId(FsAdapterUtils.makeFileId(clientFolder.getPath()));
		
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				ClientFolder clientSubFolder = new ClientFolder();
				clientFolder.getSubFolders().add(clientSubFolder);
				
				populateFolder(clientFolder.getPath(), file, clientSubFolder);
			} else {
				ClientFile clientFile = new ClientFile();
				clientFile.setFsFile(file);
				clientFile.setName(file.getName());
				clientFile.setPath(clientFolder.getPath() + "/" + file.getName());				
				clientFile.setId(FsAdapterUtils.makeFileId(clientFile.getPath()));
				clientFile.setFolder(clientFolder);
				
				clientFolder.getFiles().add(clientFile);
			}
		}
	}

	/**
	 * @return fileTree for given root folder received from server
	 */
	public synchronized ClientFileTree getRemoteFileTree(File rootFolder) throws RepositoryException {
		populateRemoteFileTrees();
		return remoteFileTrees.get(rootFolder.getName());
	}

	private void populateRemoteFileTrees() throws RepositoryException {
		if (remoteFileTrees == null) {
			remoteFileTrees = repositoryEndpoint.getFileTrees();
		}
	}

	// ============================================
	// directories support methods
	// ============================================

	public List<File> getWatchedDirectories() {
		return configService.getWatchedDirectories().stream().map(path -> new File(path)).collect(Collectors.toList());
	}

	public void addWatchedDirectory(String directory) {
		List<String> directories = configService.getWatchedDirectories();
		directories.add(directory);
		configService.setWatchedDirectories(directories);

		watchedDirectoriesListener.forEach(listener -> listener.directoryAdded(directory));

		watchDirectory(Paths.get(directory));
	}

	public void addWatchedDirectoriesListener(WatchedDirectoriesListener listener) {
		watchedDirectoriesListener.add(listener);
	}

	public void removeDirectory(String dir) {
		List<String> directories = configService.getWatchedDirectories();
		directories.remove(dir);
		configService.setWatchedDirectories(directories);

		watchedDirectoriesListener.forEach(listener -> listener.directoryRemoved(dir));
		removeFromServer(dir);

		try {
			if (directoryWatchers.get(dir) != null) {
				directoryWatchers.get(dir).finishWatch();
			}
		} catch (IOException e) {
			utils.logToFile("Failed to close directory", e);
		}
	}

	private void removeFromServer(String dir) {
		fileSender.removeFolder("/" + new File(dir).getName());
	}

	public void clearWatchedDirectories() {
		getWatchedDirectories().forEach(dir -> removeDirectory(dir.getAbsolutePath()));
	}

	public FileSender getFileSender() {
		return fileSender;
	}
	
	public Utils getUtils() {
		return utils;
	}
}
