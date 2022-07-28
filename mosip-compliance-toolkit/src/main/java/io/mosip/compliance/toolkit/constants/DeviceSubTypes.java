package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * DeviceSubTypes Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum DeviceSubTypes {
	SLAP("Slap"),
	SINGLE("Single"),
	TOUCHLESS("Touchless"),
	DOUBLE("Double"),
	FULL_FACE("Full face");

	private final String code;

	private DeviceSubTypes(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static DeviceSubTypes fromCode(String code) {
		 for (DeviceSubTypes paramCode : DeviceSubTypes.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
	}
}