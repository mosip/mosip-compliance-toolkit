package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.DeviceTypes;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

@Component
public class RegistrationBioUtilValidator extends SBIValidator {
    
    private static final String ISO19794_5_2011 = "ISO19794_5_2011";
    private static final String ISO19794_6_2011 = "ISO19794_6_2011";
    private static final String ISO19794_4_2011 = "ISO19794_4_2011";
    public static final String BIO = "bio";
    public static final String DECODED_DATA = "dataDecoded";
    public static final String BIO_VALUE = "bioValue";
    public static final String PURPOSE = "purpose";
    public static final String BIO_TYPE = "bioType";
    public static final String BIO_SUBTYPE = "bioSubType";

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            String responseJson = inputDto.getMethodResponse();

            ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(responseJson,
                    ObjectNode.class);

            JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
            if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
                for (final JsonNode biometricNode : arrBiometricNodes) {
                    JsonNode dataNode = biometricNode.get(DECODED_DATA);

                    String purpose = dataNode.get(PURPOSE).asText();
                    String bioType = dataNode.get(BIO_TYPE).asText();
                    String bioValue = null;
                    switch (Purposes.fromCode(purpose)) {
                        case REGISTRATION:
                            bioValue = dataNode.get(BIO_VALUE).asText();
                            break;
                        case AUTH:
                            throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(),
                                    ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
                    }
                    validationResultDto = isValidISOTemplate(purpose, bioType, bioValue);
                    if (validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
                        break;
                    }
                }
            }
        } catch (ToolkitException e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
        }
        return validationResultDto;
    }

    private ValidationResultDto isValidISOTemplate(String purpose, String bioType, String bioValue) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        DeviceTypes deviceTypeCode = DeviceTypes.fromCode(bioType);

        if (bioValue != null) {
            switch (deviceTypeCode) {
                case FINGER:
                    validationResultDto = isValidFingerISOTemplate(purpose, bioValue);
                    break;
                case IRIS:
                    validationResultDto = isValidIrisISOTemplate(purpose, bioValue);
                    break;
                case FACE:
                    validationResultDto = isValidFaceISOTemplate(purpose, bioValue);
                    break;
                default:
                    validationResultDto.setStatus(AppConstants.FAILURE);
                    validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
                            + " invalid bioType = " + bioType);
                    break;
            }
        } else {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
                    + " isValidISOTemplate BioValue is Empty or Null");
        }
        return validationResultDto;
    }

    private ValidationResultDto isValidFingerISOTemplate(String purpose, String bioValue) {
        ToolkitErrorCodes errorCode = null;
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.FAILURE);

        ConvertRequestDto requestDto = new ConvertRequestDto();
        requestDto.setModality(DeviceTypes.FINGER.getCode());
        requestDto.setVersion(ISO19794_4_2011);

        try {
            requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        FingerBDIR bdir;
        try {
            bdir = FingerDecoder.getFingerBDIR(requestDto);
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FINGER_ISO_FORMAT_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        byte[] inImageData = bdir.getRepresentation().getRepresentationBody().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
        return validationResultDto;
    }

    private ValidationResultDto isValidIrisISOTemplate(String purpose, String bioValue) {
        ToolkitErrorCodes errorCode = null;
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.FAILURE);

        ConvertRequestDto requestDto = new ConvertRequestDto();
        requestDto.setModality(DeviceTypes.IRIS.getCode());
        requestDto.setVersion(ISO19794_6_2011);

        try {
            requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        IrisBDIR bdir;
        try {
            bdir = IrisDecoder.getIrisBDIR(requestDto);
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_IRIS_ISO_FORMAT_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
        return validationResultDto;
    }

    private ValidationResultDto isValidFaceISOTemplate(String purpose, String bioValue) {
        ToolkitErrorCodes errorCode = null;
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.FAILURE);

        ConvertRequestDto requestDto = new ConvertRequestDto();
        requestDto.setModality(DeviceTypes.FACE.getCode());
        requestDto.setVersion(ISO19794_5_2011);

        try {
            requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        FaceBDIR bdir;
        try {
            bdir = FaceDecoder.getFaceBDIR(requestDto);
        } catch (Exception e) {
            errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FACE_ISO_FORMAT_EXCEPTION;
            throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
        }

        byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
        return validationResultDto;
    }

}
