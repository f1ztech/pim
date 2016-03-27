package ru.mipt.pim.server.controllers;

import ru.mipt.pim.adapters.fs.common.JsonRequestResults.CommonRequestResult;

public class InnerRequestResults {

	public static class UpdatePublicationResult extends CommonRequestResult {
		public UpdatePublicationResult() {
		}

		public UpdatePublicationResult(String error) {
			super(error);
		}
	}
	
}
