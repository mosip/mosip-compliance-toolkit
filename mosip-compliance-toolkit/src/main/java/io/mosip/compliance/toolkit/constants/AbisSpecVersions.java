package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public enum AbisSpecVersions {
    SPEC_VER_0_9_0("0.9.0");

    private final String code;

    private AbisSpecVersions(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AbisSpecVersions fromCode(String code) {
        for (AbisSpecVersions paramCode : AbisSpecVersions.values()) {
            if (paramCode.getCode().equals(code)) {
                return paramCode;
            }
        }
        throw new ToolkitException(ToolkitErrorCodes.INVALID_ABIS_SPEC_VERSION.getErrorCode(), ToolkitErrorCodes.INVALID_SDK_SPEC_VERSION.getErrorMessage());
    }
}