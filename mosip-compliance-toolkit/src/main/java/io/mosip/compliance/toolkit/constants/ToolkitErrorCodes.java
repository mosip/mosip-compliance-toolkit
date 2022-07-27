package io.mosip.compliance.toolkit.constants;

/**
 * ToolkitErrorCode Enum for the services errors.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum ToolkitErrorCodes {
	PROJECTS_NOT_AVAILABLE("TOOLKIT_PROJECTS_001", "No project found for the user"),
	SBI_PROJECT_NOT_AVAILABLE("TOOLKIT_PROJECTS_002", "No matching SBI project available for the given id"),
	COLLECTION_NOT_AVAILABLE("TOOLKIT_COLLECTIONS_001", "No matching Collection available for the given project"),
	INVALID_REQUEST_ID("TOOLKIT_REQ_ERR_001", "Request id is invalid"),
	INVALID_REQUEST_VERSION("TOOLKIT_REQ_ERR_002", "Request version is invalid"),
	INVALID_REQUEST_DATETIME("TOOLKIT_REQ_ERR_003", "Invalid request time"),
	INVALID_REQUEST_BODY("TOOLKIT_REQ_ERR_004", "Request body is invalid"),
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("TOOLKIT_REQ_ERR_005", "Request date should be current date"),
	INVALID_PROJECT_TYPE("TOOLKIT_REQ_ERR_006", "Project Type is invalid"),	
	INVALID_TEST_CASE_ID("TOOLKIT_REQ_ERR_007", "Test Case Id is invalid"),
	INVALID_TEST_CASE_JSON("TOOLKIT_REQ_ERR_008", "Test Case Json is invalid"),
	SAVE_TEST_CASE_JSON_ERROR("TOOLKIT_REQ_ERR_009", "Save Test Case Json error"),
	TECHNICAL_ERROR_EXCEPTION("TOOLKIT_REQ_ERR_500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	private ToolkitErrorCodes(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static ToolkitErrorCodes fromErrorCode(String errorCode) {
		 for (ToolkitErrorCodes paramCode : ToolkitErrorCodes.values()) {
	     	if (paramCode.getErrorCode().equalsIgnoreCase(errorCode)) {
	        	return paramCode;
	    	}
	    }
		return TECHNICAL_ERROR_EXCEPTION;
	}
}
