package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * DeviceStatus Enum.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum DeviceStatus {
	READY("Ready"),
	BUSY("Busy"),
	NOT_READY("Not Ready"),
	NOT_REGISTERED("Not Registered");

	private final String code;

	private DeviceStatus(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static DeviceStatus fromCode(String code) {
		 for (DeviceStatus paramCode : DeviceStatus.values()) {
	     	if (paramCode.getCode().equals(code)) {
	        	return paramCode;
	    	}
		 }
		 throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_STATUS.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_STATUS.getErrorMessage());
	}
}