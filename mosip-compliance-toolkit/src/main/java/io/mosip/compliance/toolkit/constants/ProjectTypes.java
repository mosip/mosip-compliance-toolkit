package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * ProjectTypes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum ProjectTypes {
	SBI("SBI"),
	SDK("SDK"),
	ABIS("SBI");

	private final String code;

	private ProjectTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static ProjectTypes fromCode(String code) {
		 for (ProjectTypes paramCode : ProjectTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorMessage());
	}
}