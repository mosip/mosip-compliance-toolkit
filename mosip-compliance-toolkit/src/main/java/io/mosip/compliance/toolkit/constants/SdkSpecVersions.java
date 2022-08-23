package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

/**
 * SpecVersions Enum.
 *
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum SdkSpecVersions {
    SPEC_VER_0_9_0("0.9.0");

    private final String code;

    private SdkSpecVersions(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static SdkSpecVersions fromCode(String code) {
        for (SdkSpecVersions paramCode : SdkSpecVersions.values()) {
            if (paramCode.getCode().equals(code)) {
                return paramCode;
            }
        }
        throw new ToolkitException(ToolkitErrorCodes.INVALID_SDK_SPEC_VERSION.getErrorCode(), ToolkitErrorCodes.INVALID_SDK_SPEC_VERSION.getErrorMessage());
    }
}