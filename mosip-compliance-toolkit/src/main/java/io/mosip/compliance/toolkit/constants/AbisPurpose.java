package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum AbisPurpose {

	INSERT("Insert"), IDENTIFY("Identify");

	private final String code;

	private AbisPurpose(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static AbisPurpose fromCode(String code) {
		for (AbisPurpose paramCode : AbisPurpose.values()) {
			if (paramCode.getCode().equals(code)) {
				return paramCode;
			}
		}
		throw new ToolkitException(ToolkitErrorCodes.INVALID_ABIS_PURPOSE.getErrorCode(),
				ToolkitErrorCodes.INVALID_ABIS_PURPOSE.getErrorMessage());
	}
}