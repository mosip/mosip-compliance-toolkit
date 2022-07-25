package io.mosip.compliance.toolkit.constants;

/**
 * ToolkitErrorCode Enum for the services errors.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum ToolkitErrorCode {
	PROJECTS_NOT_AVAILABLE("TOOLKIT_PROJECTS_001", "No project found for the user"),
	INVALID_REQUEST_ID("TOOLKIT_REQ_ERR_001", "Request id is invalid"),
	INVALID_REQUEST_VERSION("TOOLKIT_REQ_ERR_002", "Request version is invalid"),
	INVALID_REQUEST_DATETIME("TOOLKIT_REQ_ERR_003", "Invalid request time"),
	INVALID_REQUEST_BODY("TOOLKIT_REQ_ERR_004", "Request body is invalid"),
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("TOOLKIT_REQ_ERR_005", "Request date should be current date"),
	INVALID_PROJECT_TYPE("TOOLKIT_REQ_ERR_006", "Project Type is invalid"),
	
	INVALID_TEST_CASE_ID("TOOLKIT_REQ_ERR_007", "Test Case Id is invalid"),
	INVALID_TEST_CASE_JSON("TOOLKIT_REQ_ERR_009", "Test Case Json is invalid"),
	TECHNICAL_ERROR_EXCEPTION("TOOLKIT_REQ_ERR_500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	private ToolkitErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static ToolkitErrorCode fromErrorCode(String errorCode) {
		 for (ToolkitErrorCode paramCode : ToolkitErrorCode.values()) {
	     	if (paramCode.getErrorCode().equalsIgnoreCase(errorCode)) {
	        	return paramCode;
	    	}
	    }
		return TECHNICAL_ERROR_EXCEPTION;
	}
}
