package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

@Component
public class RegistrationBioUtilValidator extends BioUtilValidator {
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
}
