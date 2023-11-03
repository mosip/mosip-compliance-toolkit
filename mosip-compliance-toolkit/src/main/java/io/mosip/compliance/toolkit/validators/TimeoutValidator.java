package io.mosip.compliance.toolkit.validators;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.logger.spi.Logger;

public class TimeoutValidator extends SBIValidator {

    private Logger log = LoggerConfiguration.logConfig(TimeoutValidator.class);

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper()
                    .readValue(inputDto.getExtraInfoJson(), ObjectNode.class);
            Long timeout = extraInfo.get("timeout").asLong();
            String startExecutionTime = extraInfo.get("startExecutionTime").asText();
            String endExecutionTime = extraInfo.get("endExecutionTime").asText();
            LocalDateTime startLocalDateTime = getLocalDate(startExecutionTime);
            LocalDateTime endLocalDateTime = getLocalDate(endExecutionTime);
            long diff = Duration.between(startLocalDateTime, endLocalDateTime).toMillis();
            if (diff >= timeout) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Timeout validation is successful");
                validationResultDto.setDescriptionKey("TIMEOUT_VALIDATOR_001");
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription("Timeout validation failed. Timeout given: " + timeout + "ms."
                        + " Response received in: " + diff + "ms");
                validationResultDto.setDescriptionKey("TIMEOUT_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + timeout + AppConstants.ARGUMENTS_SEPARATOR + diff);
            }
        } catch (ToolkitException e) {
            handleExceptionAndSetResultStatus(validationResultDto,e);
        } catch (Exception e) {
            handleExceptionAndSetResultStatus(validationResultDto,e);
        }
        return validationResultDto;
    }
    public void handleExceptionAndSetResultStatus(ValidationResultDto validationResultDto, Exception e) {
        log.debug("sessionId", "idType", "id", e.getStackTrace());
        log.error("sessionId", "idType", "id", "In TimeoutValidator - " + e.getMessage());
        validationResultDto.setStatus(AppConstants.FAILURE);
        validationResultDto.setDescription(e.getLocalizedMessage());
        validationResultDto.setDescriptionKey(e.getLocalizedMessage());
    }


    private LocalDateTime getLocalDate(String executionTime) {
        String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        LocalDateTime localDt = LocalDateTime.parse(executionTime, dateFormat);
        return localDt;
    }

}
