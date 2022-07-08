package io.mosip.compliance.toolkit.exceptions;

/**
 * This Enum provides the constant variables to define Error codes. 
 * @author Mayura Deshmukh
 * 
 * @since 1.0.0
 */
public enum ErrorCodes {

	TOOLKIT_PROJECTS_001("TOOLKIT_PROJECTS_001"),
	TOOLKIT_REQ_ERR_001("TOOLKIT_REQ_ERR_001"), 
	TOOLKIT_REQ_ERR_002("TOOLKIT_REQ_ERR_002"), 
	TOOLKIT_REQ_ERR_003("TOOLKIT_REQ_ERR_003"), 
	TOOLKIT_REQ_ERR_004("TOOLKIT_REQ_ERR_004"),
	TOOLKIT_REQ_ERR_005("TOOLKIT_REQ_ERR_005"),
	TOOLKIT_REQ_ERR_006("TOOLKIT_REQ_ERR_006");

	/**
	 * @param code
	 */
	private ErrorCodes(String code) {
		this.code = code;
	}

	/**
	 * Code
	 */
	private final String code;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

}