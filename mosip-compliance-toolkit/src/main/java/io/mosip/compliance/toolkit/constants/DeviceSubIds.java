package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum DeviceSubIds {
	FINGER_SLAP_LEFT("1"),
	FINGER_SLAP_RIGHT("2"),
	FINGER_SLAP_TWO_THUMBS("3"),
	FINGER_SINGLE("0"),
	IRIS_DOUBLE_LEFT("1"),
	IRIS_DOUBLE_RIGHT("2"),
	IRIS_DOUBLE_BOTH("3"),
	IRIS_SINGLE("0"),
	FACE_SINGLE("0");

	private final String code;

	private DeviceSubIds(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static DeviceSubIds fromCode(String code) {
		 for (DeviceSubIds paramCode : DeviceSubIds.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
	}
}