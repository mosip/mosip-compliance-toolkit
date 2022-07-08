package io.mosip.compliance.toolkit.exceptions;

/**
 * This Enum provides the constant variables to define Error Messages.
 * 
 * @author Mayura Deshmukh
 * 
 * @since 1.0.0
 * 
 */
public enum ErrorMessages {

	PROJECTS_NOT_AVAILABLE("No project found for the user"),//TOOLKIT_PROJECTS_001
	INVALID_REQUEST_ID("Request id is invalid"), //TOOLKIT_REQ_ERR_001
	INVALID_REQUEST_VERSION("Request version is invalid"),//TOOLKIT_REQ_ERR_002 
	INVALID_REQUEST_DATETIME("Invalid request time"), //TOOLKIT_REQ_ERR_003
	INVALID_REQUEST_BODY("Request body is invalid"),//TOOLKIT_REQ_ERR_004
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("Request date should be current date"),//TOOLKIT_REQ_ERR_005
	INVALID_PROJECT_TYPE("Project Type is invalid"); //TOOLKIT_REQ_ERR_006
	/**
	 * @param code
	 */
	private ErrorMessages(String message) {
		this.message = message;
	}

	private final String message;

	/**
	 * @return message
	 */
	public String getMessage() {
		return message;
	}

}
