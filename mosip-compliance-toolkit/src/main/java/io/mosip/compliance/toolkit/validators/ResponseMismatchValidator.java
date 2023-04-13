package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import io.mosip.compliance.toolkit.constants.DeviceSubIds;

@Component
public class ResponseMismatchValidator extends ToolkitValidator {

    public static final String BIOMETRICS = "biometrics";
    public static final String BIO = "bio";
    public static final String DATA_DECODED = "dataDecoded";
    public static final String PURPOSE = "purpose";
    public static final String COUNT = "count";
    public static final String BIO_TYPE = "bioType";
    public static final String BIO_SUBTYPE = "bioSubType";
    public static final String EXCEPTION = "exception";
    public static final String DEVICE_SUBID = "deviceSubId";

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        ToolkitErrorCodes errorCode = null;
        try {
            String requestJson = inputDto.getMethodRequest();
            String responseJson = inputDto.getMethodResponse();
            ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(responseJson,
                    ObjectNode.class);
            ObjectNode captureInfoRequest = (ObjectNode) objectMapperConfig.objectMapper().readValue(requestJson,
                    ObjectNode.class);
            JsonNode respBiometricNodesArr = captureInfoResponse.get(BIOMETRICS);
            JsonNode reqBiometricNodesArr = captureInfoRequest.get(BIO);

            int reqBioCount = -1;
            List<String> reqBioSubtype = null;
            List<String> reqException = null;
            String reqPurpose = captureInfoRequest.get(PURPOSE).asText();
            String resPurpose = null;
            String reqDeviceSubId = null;

            if (!reqBiometricNodesArr.isNull() && reqBiometricNodesArr.isArray()) {
                JsonNode biometricNode = reqBiometricNodesArr.get(0);
                reqBioCount = biometricNode.get(COUNT).asInt();
                reqBioSubtype = new ArrayList<>();
                JsonNode bioSubTypeNode = biometricNode.get(BIO_SUBTYPE);
                if (bioSubTypeNode.isArray()) {
                    for (final JsonNode bioSubNode : bioSubTypeNode) {
                        reqBioSubtype.add(bioSubNode.textValue());
                    }
                }
                reqException = new ArrayList<>();
                if (Purposes.REGISTRATION.getCode().equals(reqPurpose)) {
                    JsonNode exceptionNode = biometricNode.get(EXCEPTION);
                    if (exceptionNode.isArray()) {
                        for (final JsonNode exNode : exceptionNode) {
                            reqException.add(exNode.textValue());
                        }
                    }
                }
                reqDeviceSubId = biometricNode.get(DEVICE_SUBID).textValue();
            } else {
                errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
                throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
            }

            if (!respBiometricNodesArr.isNull() && respBiometricNodesArr.isArray()) {
                int resCount = respBiometricNodesArr.size();
                // Check Biocount mismatch
                validationResultDto = isValidBioCount(reqBioCount, resCount);
                if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
                    for (final JsonNode biometricNode : respBiometricNodesArr) {
                        JsonNode dataNode = biometricNode.get(DATA_DECODED);
                        // Check Purpose mismatch
                        resPurpose = dataNode.get(PURPOSE).textValue();
                        validationResultDto = isValidPurpose(reqPurpose, resPurpose);
                        if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {

                            String resType = dataNode.get(BIO_TYPE).asText();
                            String resBioSubType = (resType.equals(DeviceTypes.FACE.getCode())) ? null
                                    : dataNode.get(BIO_SUBTYPE).asText();
                            // Check Segment mismatch
                            validationResultDto = isValidSegment(resType, reqDeviceSubId, resBioSubType);
                            if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
                                // Check Exception mismatch
                                if (Purposes.REGISTRATION.getCode().equals(reqPurpose)) {
                                    if (isValidException(reqException, resBioSubType).getStatus()
                                            .equals(AppConstants.SUCCESS)) {
                                        continue;
                                    } else {
                                        break;
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
                validationResultDto.setDescription(
                        "The bioCount, segments and exceptions in response match the request.");
                validationResultDto.setDescriptionKey("RESPONSE_MISMATCH_VALIDATOR_001");
            }
        } catch (ToolkitException e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(
                    "ResponseMismatchValidator failure, " + "with Message, " + e.getLocalizedMessage());
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(
                    "ResponseMismatchValidator failure, " + "with Message, " + e.getLocalizedMessage());
        }
        return validationResultDto;
    }

    private ValidationResultDto isValidBioCount(int reqCount, int resCount) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        if (reqCount == resCount) {
            validationResultDto.setStatus(AppConstants.SUCCESS);
        } else {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription("ResponseMismatchValidator failed due to" + " biocount mismatch."
                    + " In request, bioCount = " + reqCount + " and in response bioCount = " + resCount);
            validationResultDto.setDescriptionKey("RESPONSE_MISMATCH_VALIDATOR_002" + ":" + reqCount + ","
                    + resCount);
        }
        return validationResultDto;
    }

    private ValidationResultDto isValidPurpose(String reqPurpose, String resPurpose) throws Exception {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        if (Purposes.fromCode(reqPurpose) == Purposes.fromCode(resPurpose)) {
            validationResultDto.setStatus(AppConstants.SUCCESS);
        } else {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription("ResponseMismatchValidator failed due to " + " purpose mismatch."
                    + "In request, purpose = " + reqPurpose + " and in response, purpose = " + resPurpose);
            validationResultDto.setDescriptionKey("RESPONSE_MISMATCH_VALIDATOR_003" + ":" + reqPurpose + ","
                    + resPurpose);
        }

        return validationResultDto;
    }

    private ValidationResultDto isValidSegment(String resType, String reqDeviceSubId,
            String resBioSubType) throws Exception {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        if (isValidBioSubType(resType, reqDeviceSubId, resBioSubType)) {
            validationResultDto.setStatus(AppConstants.SUCCESS);
        } else {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(
                    "ResponseMismatchValidator failed, "
                            + "due to invalid Response BioSubType.");
            validationResultDto.setDescriptionKey("RESPONSE_MISMATCH_VALIDATOR_004");
        }
        return validationResultDto;
    }

    private ValidationResultDto isValidException(List<String> reqException, String resBioSubType) throws Exception {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        if (!reqException.isEmpty()) {
            for (String exception : reqException) {
                if (resBioSubType.equals(exception)) {
                    validationResultDto.setStatus(AppConstants.FAILURE);
                    validationResultDto.setDescription(
                            "ResponseMismatchValidator failed " + "due to exception, biosubtype = "
                                    + resBioSubType + " is not valid");
                    validationResultDto.setDescriptionKey("RESPONSE_MISMATCH_VALIDATOR_005" + ":" + resBioSubType);
                    break;
                }
            }
        }
        return validationResultDto;
    }

    private boolean isValidBioSubType(String resType, String reqDeviceSubId, String resBioSubType) throws Exception {
        ToolkitErrorCodes errorCode = null;
        switch (DeviceTypes.fromCode(resType)) {
            case FINGER:
                switch (DeviceSubIds.DeviceSubIdsFinger.fromCode(reqDeviceSubId)) {
                    case FINGER_SINGLE:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT_INDEXFINGER:
                            case LEFT_MIDDLEFINGER:
                            case LEFT_RINGFINGER:
                            case LEFT_LITTLEFINGER:
                            case RIGHT_INDEXFINGER:
                            case RIGHT_MIDDLEFINGER:
                            case RIGHT_RINGFINGER:
                            case RIGHT_LITTLEFINGER:
                            case LEFT_THUMB:
                            case RIGHT_THUMB:
                            case UNKNOWN:
                                return true;
                            default:
                                return false;
                        }
                    case FINGER_SLAP_LEFT:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT_INDEXFINGER:
                            case LEFT_MIDDLEFINGER:
                            case LEFT_RINGFINGER:
                            case LEFT_LITTLEFINGER:
                                return true;
                            default:
                                return false;
                        }
                    case FINGER_SLAP_RIGHT:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case RIGHT_INDEXFINGER:
                            case RIGHT_MIDDLEFINGER:
                            case RIGHT_RINGFINGER:
                            case RIGHT_LITTLEFINGER:
                                return true;
                            default:
                                return false;
                        }
                    case FINGER_SLAP_TWO_THUMBS:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT_THUMB:
                            case RIGHT_THUMB:
                                return true;
                            default:
                                return false;
                        }
                    default:
                        errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
                        throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
                }
            case IRIS:
                switch (DeviceSubIds.DeviceSubIdsIris.fromCode(reqDeviceSubId)) {
                    case IRIS_SINGLE:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT:
                            case RIGHT:
                            case UNKNOWN:
                                return true;
                            default:
                                return false;
                        }
                    case IRIS_DOUBLE_LEFT:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT:
                                return true;
                            default:
                                return false;
                        }
                    case IRIS_DOUBLE_RIGHT:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case RIGHT:
                                return true;
                            default:
                                return false;
                        }
                    case IRIS_DOUBLE_BOTH:
                        switch (BioSubTypes.fromCode(resBioSubType)) {
                            case LEFT:
                            case RIGHT:
                                return true;
                            default:
                                return false;
                        }
                    default:
                        errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
                        throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
                }
            case FACE:
                switch (DeviceSubIds.DeviceSubIdsFace.fromCode(reqDeviceSubId)) {
                    case FACE_SINGLE:
                        return true;
                    default:
                        errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
                        throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
                }
            default:
                errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
                throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
        }
    }
}