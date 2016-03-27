package ru.mipt.pim.adapters.fs.remote;

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
		private boolean publicationExists;

		public boolean isPublicationExists() {
			return publicationExists;
		}

		public void setPublicationExists(boolean publicationExists) {
			this.publicationExists = publicationExists;
		}
	}

	public static class UploadResult extends CommonRequestResult {
		public UploadResult() {
		}

		public UploadResult(String error) {
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

}
