package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum BioSubTypes {
	LEFT_INDEXFINGER("Left IndexFinger"),
	LEFT_MIDDLEFINGER("Left MiddleFinger"),
	LEFT_RINGFINGER("Left RingFinger"),
	LEFT_LITTLEFINGER("Left LittleFinger"),
	LEFT_THUMB("Left Thumb"),
	RIGHT_INDEXFINGER("Right IndexFinger"),
	RIGHT_MIDDLEFINGER("Right MiddleFinger"),
	RIGHT_RINGFINGER("Right RingFinger"),
	RIGHT_LITTLEFINGER("Right LittleFinger"),
	RIGHT_THUMB("Right Thumb"),
	RIGHT("Right"),
	LEFT("Left"),
	UNKNOWN("UNKNOWN");

	private final String code;

	private BioSubTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static BioSubTypes fromCode(String code) {
		 for (BioSubTypes paramCode : BioSubTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
	}
}