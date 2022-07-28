package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * Purposes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum Purposes {
	AUTH("Auth"),
	REGISTRATION("Registration");

	private final String code;

	private Purposes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Purposes fromCode(String code) {
		 for (Purposes paramCode : Purposes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(), ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
	}
}