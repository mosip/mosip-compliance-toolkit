package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * DeviceTypes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum DeviceTypes {
	FINGER("Finger"),
	IRIS("Iris"),
	FACE("Face");

	private final String code;

	private DeviceTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static DeviceTypes fromCode(String code) {
		 for (DeviceTypes paramCode : DeviceTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_TYPE.getErrorMessage());
	}
}