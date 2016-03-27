package ru.mipt.pim.server.adapters.fs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import ru.mipt.pim.adapters.fs.common.ClientFile;
import ru.mipt.pim.adapters.fs.common.ClientFileTree;
import ru.mipt.pim.adapters.fs.common.ClientFolder;
import ru.mipt.pim.adapters.fs.common.FsAdapterUtils;
import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.Resource;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.repositories.FolderRepository;

@Service
public class FileAdapterService {
	
	@javax.annotation.Resource
	private FolderRepository folderRepository;

	public Map<String, ClientFileTree> getFileTrees(User user) {
		HashMap<String, ClientFileTree> ret = new HashMap<String, ClientFileTree>();
		for (Folder folder : folderRepository.findRootFolders(user)) {
			ClientFileTree fileTree = new ClientFileTree();
			ClientFolder rootFolder = new ClientFolder();
			fileTree.setRootFolder(rootFolder);
			
			populateFolder("", folder, rootFolder);
			
			ret.put(folder.getName(), fileTree);
		}
		return ret;
	}

	private void populateFolder(String path, Folder folder, ClientFolder clientFolder) {
		clientFolder.setName(folder.getName());
		clientFolder.setPath(path + "/" + folder.getName());	
		clientFolder.setId(FsAdapterUtils.makeFileId(clientFolder.getPath()));
		
		for (Resource resource : folder.getNarrowerResources()) {
			if (resource instanceof File) {
				ClientFile clientFile = new ClientFile();
				clientFile.setName(resource.getName());
				clientFile.setPath(clientFolder.getPath() + "/" + resource.getName());	
				clientFile.setId(FsAdapterUtils.makeFileId(clientFile.getPath()));
				clientFile.setFolder(clientFolder);
				
				clientFolder.getFiles().add(clientFile);
			} else if (resource instanceof Folder) {
				ClientFolder clientSubFolder = new ClientFolder();
				clientFolder.getSubFolders().add(clientSubFolder);
				
				populateFolder(clientFolder.getPath(), (Folder) resource, clientSubFolder);
			}
		}
	}
	
}
