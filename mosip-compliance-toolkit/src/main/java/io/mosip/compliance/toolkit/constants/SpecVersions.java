package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * SpecVersions Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum SpecVersions {
	SPEC_VER_0_9_5("0.9.5"),
	SPEC_VER_1_0_0("1.0.0");

	private final String code;

	private SpecVersions(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
	
	public static SpecVersions fromCode(String code) {
		 for (SpecVersions paramCode : SpecVersions.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_SPEC_VERSION_.getErrorCode(), ToolkitErrorCodes.INVALID_SPEC_VERSION_.getErrorMessage());
	}
}
