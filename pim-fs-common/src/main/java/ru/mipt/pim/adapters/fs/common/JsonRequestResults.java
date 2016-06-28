package ru.mipt.pim.adapters.fs.common;

import java.util.Map;

public class JsonRequestResults {

	public static class CommonRequestResult {
		
		private boolean success = true;
		private String error;

		public CommonRequestResult() {
		}

		public CommonRequestResult(String error) {
			this.success = false;
			this.error = error;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}
	}

	public static class AuthenticateResult extends CommonRequestResult {

		private String token;

		public AuthenticateResult() {
		}

		public AuthenticateResult(String token) {
			this.token = token;
		}

		public AuthenticateResult(boolean success) {
			setSuccess(false);
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}

	public static class ExistsResult {

		private boolean fileExists;

		public boolean isFileExists() {
			return fileExists;
		}

		public void setFileExists(boolean fileExists) {
			this.fileExists = fileExists;
		}
	}

	public static class UploadResult extends CommonRequestResult {

		private String fileId;
		
		public UploadResult() {
		}

		public UploadResult(String error) {
			super(error);
		}

		public String getFileId() {
			return fileId;
		}

		public void setFileId(String fileId) {
			this.fileId = fileId;
		}
	}
	
	public static class SaveResult extends CommonRequestResult {
		
		public SaveResult() {
		}

		public SaveResult(String error) {
			super(error);
		}
		
	}

	public static class RemoveResult extends CommonRequestResult {

		public RemoveResult() {
		}

		public RemoveResult(String error) {
			super(error);
		}
	}

	public static class FileTreesResult extends CommonRequestResult {

		private Map<String, ClientFileTree> fileTrees;
		
		public FileTreesResult() {
		}
		
		public FileTreesResult(Map<String, ClientFileTree> fileTrees) {
			this.fileTrees = fileTrees;
		}

		public FileTreesResult(String error) {
			super(error);
		}

		public Map<String, ClientFileTree> getFileTrees() {
			return fileTrees;
		}

		public void setFileTrees(Map<String, ClientFileTree> fileTrees) {
			this.fileTrees = fileTrees;
		}
	}

}
