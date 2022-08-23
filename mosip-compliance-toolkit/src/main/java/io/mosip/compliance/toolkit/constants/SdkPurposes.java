package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * Purposes Enum.
 *
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum SdkPurposes {
    QUALITY_CHECK("QualityCheck"),
    SEGMENT("Segment"),
    EXTRACT("Extract"),
    CONVERT_FORMAT("ConvertFormat"),
    MATCH("Match");

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