package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimeCheckValidator extends SBIValidator{

    private Logger log = LoggerConfiguration.logConfig(TimeCheckValidator.class);

    @Value("${mosip.toolkit.sbi.timestamp-interval}")
    String interval;

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper()
                    .readValue(inputDto.getExtraInfoJson(), ObjectNode.class);
            String startTime=extraInfo.get("startExecutionTime").asText();
            LocalDateTime localStartTime=getLocalDate(startTime);

            long time_interval=Long.parseLong(interval);

            String responseJson = inputDto.getMethodResponse();
            ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
                    .readValue(responseJson, ObjectNode.class);
            JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
            if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
                boolean result=false;
                long diff=0;
                for (final JsonNode biometricNode : arrBiometricNodes) {
                    JsonNode dataNode = biometricNode.get(DECODED_DATA);

                    String timestamp = dataNode.get(TIME_STAMP).asText();
                    LocalDateTime localtimestamp = getLocalDate1(timestamp);
                    diff = Duration.between(localStartTime, localtimestamp).toMinutes();
                    if (diff <=time_interval) {
                        result = true;
                    } else {
                        result = false;
                        break;
                    }
                }
                if(result==false){
                    validationResultDto.setStatus(AppConstants.FAILURE);
                    validationResultDto.setDescription("Time Check validation failed. Timestamp interval given: "
                            + interval + "m." + " Response received in: " + diff + "m");
                    validationResultDto.setDescriptionKey("TIMECHECK_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + interval + AppConstants.ARGUMENTS_SEPARATOR + diff);
                }else{
                    validationResultDto.setStatus(AppConstants.SUCCESS);
                    validationResultDto.setDescription("Time Check validation is Successful");
                    validationResultDto.setDescriptionKey("TIMECHECK_VALIDATOR_001");
                }

            }
        }catch (ToolkitException e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id", "In TimeCheckValidator - " + e.getMessage());
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            validationResultDto.setDescriptionKey(e.getLocalizedMessage());
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id", "In TimeCheckValidator - " + e.getMessage());
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            validationResultDto.setDescriptionKey(e.getLocalizedMessage());
        }
        return validationResultDto;
    }

    private LocalDateTime getLocalDate(String time) {
        String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        LocalDateTime localDt = LocalDateTime.parse(time, dateFormat);
        return localDt;
    }
    private LocalDateTime getLocalDate1(String time) {
        String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        LocalDateTime localDt = LocalDateTime.parse(time, dateFormat);
        return localDt;
    }

}
