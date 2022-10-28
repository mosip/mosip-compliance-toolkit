package io.mosip.compliance.toolkit.validators;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public class TimeoutValidator extends SBIValidator {

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
            //add extra time of 10 seconds
            timeout = timeout + 10000;
            if (diff >= timeout) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Timeout validation is successful");
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription("Timeout validation failed. Timeout given: " + timeout + "ms." + " Response received in: " + diff + "ms");
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

    private LocalDateTime getLocalDate(String executionTime) {
        String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        LocalDateTime localDt = LocalDateTime.parse(executionTime, dateFormat);
        return localDt;
    }

}