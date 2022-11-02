package io.mosip.compliance.toolkit.constants;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public class DeviceSubIds {

    // Device Sub Ids for type = Finger
    public enum DeviceSubIdsFinger {
        FINGER_SINGLE("0"),
        FINGER_SLAP_LEFT("1"),
        FINGER_SLAP_RIGHT("2"),
        FINGER_SLAP_TWO_THUMBS("3");

        private final String code;

        private DeviceSubIdsFinger(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFinger fromCode(String code) {
            for (io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFinger paramCode : io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFinger.values()) {
                if (paramCode.getCode().equals(code)) {
                    return paramCode;
                }
            }
            throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
        }
    }

    // Device Sub Ids for type = Iris
    public enum DeviceSubIdsIris {

        IRIS_SINGLE("0"),
        IRIS_DOUBLE_LEFT("1"),
        IRIS_DOUBLE_RIGHT("2"),
        IRIS_DOUBLE_BOTH("3");

        private final String code;

        private DeviceSubIdsIris(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsIris fromCode(String code) {
            for (io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsIris paramCode : io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsIris.values()) {
                if (paramCode.getCode().equals(code)) {
                    return paramCode;
                }
            }
            throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
        }
    }

    // Device Sub Ids for type = Face
    public enum DeviceSubIdsFace {
        FACE_SINGLE("0");

        private final String code;

        private DeviceSubIdsFace(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFace fromCode(String code) {
            for (io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFace paramCode : io.mosip.compliance.toolkit.constants.DeviceSubIds.DeviceSubIdsFace.values()) {
                if (paramCode.getCode().equals(code)) {
                    return paramCode;
                }
            }
            throw new ToolkitException(ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorCode(), ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE.getErrorMessage());
        }
    }
}
