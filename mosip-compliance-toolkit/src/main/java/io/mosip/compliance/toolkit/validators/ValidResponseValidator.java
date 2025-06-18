package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValidResponseValidator extends SDKValidator {

    private static final String RESPONSE = "response";
    private static final String STATUS_CODE = "statusCode";

    @Autowired
    private ObjectMapperConfig objectMapperConfig;

    private final Logger log = LoggerConfiguration.logConfig(ValidResponseValidator.class);

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
                    .readValue(inputDto.getMethodResponse(), ObjectNode.class);
            JsonNode mainResponse = methodResponse.get(RESPONSE);
            int statusCode = Integer.parseInt(mainResponse.get(STATUS_CODE).asText());

            if (statusCode >= 200 && statusCode <= 299) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Validation is successful. Received status code: " + statusCode);
                validationResultDto.setDescriptionKey("VALID_RESPONSE_VALIDATOR_001");
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription("Validation failed. Received status code: " + statusCode);
                validationResultDto.setDescriptionKey("VALID_RESPONSE_VALIDATOR_002"
                        + AppConstants.ARGUMENTS_DELIMITER + statusCode);
            }
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In ExceptionResponseValidator - " + e.getMessage());
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            validationResultDto.setDescriptionKey(e.getLocalizedMessage());
        }
        return validationResultDto;
    }
}


