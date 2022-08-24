package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum SdkPurpose {

	CHECK_QUALITY("Check Quality"), MATCHER("Matcher"), EXTRACT_TEMPLATE("Extract Template"),
	CONVERT_FORMAT("Convert Format"), SEGMENT("Segment");

	private final String code;

	private SdkPurpose(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static SdkPurpose fromCode(String code) {
		for (SdkPurpose paramCode : SdkPurpose.values()) {
			if (paramCode.getCode().equals(code)) {
				return paramCode;
			}
		}
		throw new ToolkitException(ToolkitErrorCodes.INVALID_SDK_PURPOSE.getErrorCode(),
				ToolkitErrorCodes.INVALID_SDK_PURPOSE.getErrorMessage());
	}
}