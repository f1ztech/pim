package ru.mipt.pim.server.adapters.fs;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.mipt.pim.adapters.fs.common.JsonRequestResults;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.FileTreesResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.RemoveResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.SaveResult;
import ru.mipt.pim.adapters.fs.common.JsonRequestResults.UploadResult;
import ru.mipt.pim.server.index.IndexingService;
import ru.mipt.pim.server.model.File;
import ru.mipt.pim.server.model.Folder;
import ru.mipt.pim.server.model.User;
import ru.mipt.pim.server.publications.PublicationParsingService;
import ru.mipt.pim.server.repositories.FileRepository;
import ru.mipt.pim.server.repositories.FolderRepository;
import ru.mipt.pim.server.services.FileService;
import ru.mipt.pim.server.services.FileStorageService;
import ru.mipt.pim.server.services.UserService;

@Controller
public class FileAdapterController {
	@Resource
	private FileRepository fileRepository;
	
	@Resource
	private FileService fileService;
	
	@Resource
	private UserService userService;	

	@Resource
	private FolderRepository folderRepository;

	@Resource
	private FileStorageService fileStorageService;
	
	@Resource
	private PublicationParsingService publicationParsingService;
	
	@Resource
	private FileAdapterService fileAdapterService;
	
	@Resource
	private IndexingService indexingService;
	
	private HashMap<String, FileUploadInfo> uploadInfoMap = new HashMap<String, FileAdapterController.FileUploadInfo>();
	
	private long uploadInfoClearTime = 0;

	private static final Log logger = LogFactory.getLog(FileAdapterController.class);

	@RequestMapping(value = "/rest/files/load")
	public void load(@RequestParam String hash) {

	}

	private class FileUploadInfo {
		
		private Date uploadDate;
		private String storagePath;

		public FileUploadInfo(String storagePath, Date uploadDate) {
			this.storagePath = storagePath;
			this.uploadDate = uploadDate;
		}
		
		public String getStoragePath() {
			return storagePath;
		}

		public Date getUploadDate() {
			return uploadDate;
		}
	}
	
	@RequestMapping(value = "/rest/files/isExists")
	public @ResponseBody JsonRequestResults.ExistsResult isExists(@RequestParam String fileName, @RequestParam String path, @RequestParam String hash) {
		JsonRequestResults.ExistsResult ret = new JsonRequestResults.ExistsResult();
		ret.setFileExists(fileRepository.findByNameAndFolderAndHash(userService.getCurrentUser(), fileName, path, hash) != null);
		return ret;
	}

	@RequestMapping(value = "/rest/files/getFileTrees")
	public @ResponseBody FileTreesResult getFileTrees() {
		return new FileTreesResult(fileAdapterService.getFileTrees(userService.getCurrentUser()));
	}
	
	@RequestMapping(value = "/rest/files/upload", method = RequestMethod.POST)
	public @ResponseBody UploadResult upload(InputStream fileStream) {
		try {
			logger.debug("Uploading file");
			
			User currentUser = userService.getCurrentUser();
			String storagePath = fileStorageService.storeFile(currentUser, fileStream);
			
			String fileId = UUID.randomUUID().toString();
			uploadInfoMap.put(fileId, new FileUploadInfo(storagePath, new Date()));

			clearOldUploadInfos();
			
			UploadResult uploadResult = new UploadResult();
			uploadResult.setFileId(fileId);
			return uploadResult;
		} catch (Exception e) {
			logger.error("Error while saving file", e);
			return new UploadResult(e.getMessage());
		}
	}
	
	private void clearOldUploadInfos() {
		if (uploadInfoClearTime + 60 * 60 * 1000 < System.currentTimeMillis()) {
			Iterator<Entry<String, FileUploadInfo>> iterator = uploadInfoMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, FileUploadInfo> entry = iterator.next();
				if (entry.getValue().getUploadDate().getTime() < System.currentTimeMillis() - 20 * 60 * 1000) {
					iterator.remove();
				}
			}
		}
	}

	@RequestMapping(value = "/rest/files/save")
	public @ResponseBody SaveResult save(@RequestParam String fileId, @RequestParam String fileName, @RequestParam String path, @RequestParam String hash) {
		try {
			logger.debug("Saving file " + path + "/" + fileName + " hash: " + hash);
			
			User currentUser = userService.getCurrentUser();
			
			FileUploadInfo uploadInfo = uploadInfoMap.remove(fileId);

			Folder folder = folderRepository.findByPath(currentUser, path);
			if (folder == null) {
				return new SaveResult("Directory " + path + " not found for file " + fileName);
			}

			File file = fileRepository.findByNameAndFolder(currentUser, fileName, path);
			if (file == null) {
				file = fileService.createNewFile(fileName, folder, hash);
			} else {
				fileStorageService.removeFile(file.getPath());
			}
			file.setOwner(currentUser);

			file.setDateModified(new Date());
			java.io.File ioFile = new java.io.File(fileStorageService.getAbsolutePath(uploadInfo.getStoragePath()));
			file.setSize(ioFile.length());
			file.setPath(uploadInfo.getStoragePath());

			fileRepository.merge(file);
			
			publicationParsingService.scheduleParsing(currentUser, file);
			try {
				indexingService.addFileToIndex(currentUser, ioFile, file, folder);
			} catch (Exception e) {
				logger.error("Error while indexing file", e);				
			}

			return new SaveResult();
		} catch (Exception e) {
			logger.error("Error while saving file", e);
			return new SaveResult(e.getMessage());
		}
	}
	

	@RequestMapping(value = "/rest/files/remove")
	public @ResponseBody RemoveResult remove(@RequestParam String fileName, @RequestParam String path) {
		try {
			logger.debug("Removing file " + path + "/" + fileName);
			
			File file = fileRepository.findByNameAndFolder(userService.getCurrentUser(), fileName, path);
			fileService.remove(file);
			return new RemoveResult();
		} catch (Exception e) {
			logger.error("Error while removing file", e);
			return new RemoveResult(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/rest/files/addFolder")
	public @ResponseBody UploadResult addFolder(@RequestParam String path) {
		try {
			logger.debug("Adding folder " + path);
			
			User currentUser = userService.getCurrentUser();
			Folder folder = folderRepository.findByPath(currentUser, path);
			
			if (folder == null) {
				folder = new Folder();
				folder.setPath(path);
				folder.setName(StringUtils.substringAfterLast(path, "/"));
				folder.setTitle(folder.getName());
				folder.setOwner(userService.getCurrentUser());

				Folder parentFolder = null;
				if (path.lastIndexOf("/") > 0) {
					parentFolder = folderRepository.findByPath(currentUser, path.substring(0, path.lastIndexOf("/")));
					if (parentFolder == null) {
						return new UploadResult("Parent directory for " + path + " not found");
					}
				}
				
				folderRepository.save(folder);
				if (parentFolder != null) {
					folderRepository.addNarrowerResource(parentFolder, folder);					
				}
				
				indexingService.indexResource(currentUser, folder);
			}
			
			return new UploadResult();
		} catch (Exception e) {
			logger.error("Error while adding folder", e);
			return new UploadResult(e.getMessage());
		}
	}

	@RequestMapping(value = "/rest/files/removeFolder")
	public @ResponseBody RemoveResult removeFolder(@RequestParam String path) {
		try {
			logger.debug("Removing folder " + path);
			
			Folder folder = folderRepository.findByPath(userService.getCurrentUser(), path);
			if (folder == null) {
				return new RemoveResult("Directory " + path + " not found");
			}
			
			removeFolder(folder);
			return new RemoveResult();
		} catch (Exception e) {
			logger.error("Error while removing folder", e);
			return new RemoveResult(e.getMessage());
		}
	}

	private void removeFolder(Folder folder) {
		folder.getNarrowerResources().forEach(resource -> {
			if (resource instanceof File) {
				fileRepository.remove((File) resource);
			}
		});
		folderRepository.remove(folder);
	}
	
}
