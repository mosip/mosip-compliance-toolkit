package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * MethodName Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum MethodName {
	DEVICE("device"),
	INFO("info"),
	CAPTURE("capture"),
	RCAPTURE("rcapture"),
	STREAM("stream"),
	INSERT("insert"),
	IDENTITY("identify"),
	DELETE("delete"),
	INIT("init"),
	CHECK_QUALITY("check-quality"),
	MATCH("match"),
	EXTRACT_TEMPLATE("extract-template"),
	CONVERT_FORMAT("convert-format"),
	SEGMENT("segment");
	
	private final String code;

	private MethodName(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static MethodName fromCode(String code) {
		 for (MethodName paramCode : MethodName.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorCode(), ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorMessage());
	}
}