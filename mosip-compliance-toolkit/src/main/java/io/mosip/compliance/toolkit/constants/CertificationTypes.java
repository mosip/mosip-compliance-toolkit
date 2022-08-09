package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * CertificationTypes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum CertificationTypes {
	L0("L0"),
	L1("L1");

	private final String code;

	private CertificationTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static CertificationTypes fromCode(String code) {
		 for (CertificationTypes paramCode : CertificationTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_CERTIFICATION_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_CERTIFICATION_TYPE.getErrorMessage());
	}
}