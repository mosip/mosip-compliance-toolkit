package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

import org.springframework.stereotype.Component;

@Component
public class TransactionIdValidator extends SBIValidator {

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodRequest = objectMapperConfig.objectMapper().readValue(responseDto.getMethodRequest(), ObjectNode.class);
            JsonNode requestTransactionId = methodRequest.get(TRANSACTION_ID);

            String methodResponseJson = responseDto.getMethodResponse();
            ObjectNode captureInfoResponse = objectMapperConfig.objectMapper().readValue(methodResponseJson, ObjectNode.class);
            JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
            boolean result = false;
            if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
                for (final JsonNode biometricNode : arrBiometricNodes) {
                    JsonNode dataNode = biometricNode.get(DECODED_DATA);
                    String transactionId = dataNode.get(TRANSACTION_ID).asText();
                    if ((requestTransactionId.asText()).equals(transactionId)) {
                        result = true;
                    } else {
                        result = false;
                        validationResultDto.setStatus(AppConstants.FAILURE);
                        validationResultDto.setDescription("Transaction Id validation failed.");
                        break;
                    }
                }
                if (result == true) {
                    validationResultDto.setStatus(AppConstants.SUCCESS);
                    validationResultDto.setDescription("Transaction Id validation is successful");
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