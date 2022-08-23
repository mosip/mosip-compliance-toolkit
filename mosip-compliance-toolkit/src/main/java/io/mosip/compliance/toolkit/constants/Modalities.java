package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum Modalities {
	FINGER("finger"), FACE("face"), IRIS("iris");

	private final String code;

	private Modalities(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Modalities fromCode(String code) {
		for (Modalities paramCode : Modalities.values()) {
			if (paramCode.getCode().equals(code)) {
				return paramCode;
			}
		}
		throw new ToolkitException(ToolkitErrorCodes.INVALID_MODALITY.getErrorCode(),
				ToolkitErrorCodes.INVALID_MODALITY.getErrorMessage());
	}
}
