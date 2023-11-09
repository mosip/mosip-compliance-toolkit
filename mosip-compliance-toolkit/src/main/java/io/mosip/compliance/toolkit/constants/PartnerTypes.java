package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * PartnerTypes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum PartnerTypes {
	DEVICE("DEVICE"),
	FTM("FTM"),
	ABIS("ABIS_PARTNER");

	private final String code;

	private PartnerTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static PartnerTypes fromCode(String code) {
		 for (PartnerTypes paramCode : PartnerTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_PARTNER_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_PARTNER_TYPE.getErrorMessage());
	}
}