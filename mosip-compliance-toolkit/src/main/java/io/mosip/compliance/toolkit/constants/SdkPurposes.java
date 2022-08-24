package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * Purposes Enum.
 *
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum SdkPurposes {
    QUALITY_CHECK("Check Quality"),
    SEGMENT("Segment"),
    EXTRACT("Extract Template"),
    CONVERT_FORMAT("Convert Format"),
    MATCH("Matcher");

    private final String code;

    private SdkPurposes(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SdkPurposes fromCode(String code) {
        for (SdkPurposes paramCode : SdkPurposes.values()) {
            if (paramCode.getCode().equals(code)) {
                return paramCode;
            }
        }
        throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(), ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
    }
}