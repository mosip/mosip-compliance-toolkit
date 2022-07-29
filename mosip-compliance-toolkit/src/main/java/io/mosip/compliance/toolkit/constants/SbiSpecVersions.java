package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * SpecVersions Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum SbiSpecVersions {
	SPEC_VER_0_9_5("0.9.5"),
	SPEC_VER_1_0_0("1.0.0");

	private final String code;

	private SbiSpecVersions(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static SbiSpecVersions fromCode(String code) {
		 for (SbiSpecVersions paramCode : SbiSpecVersions.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_SBI_SPEC_VERSION.getErrorCode(), ToolkitErrorCodes.INVALID_SBI_SPEC_VERSION.getErrorMessage());
	}
}