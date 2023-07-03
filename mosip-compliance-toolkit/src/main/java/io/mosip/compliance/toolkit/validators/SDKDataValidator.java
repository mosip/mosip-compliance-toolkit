package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidatorDefDto;

import java.util.List;

public abstract class SDKDataValidator extends ToolkitValidator{

    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodResponse(),
                    ObjectNode.class);
            JsonNode mainResponse = (JsonNode) methodResponse.get("response");
            int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
            if (isSuccessStatusCode(statusCode)) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription(getSuccessDescription(inputDto,statusCode));
                validationResultDto.setDescriptionKey(getSuccessDescriptionKey(inputDto,statusCode));
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription(getFailureDescription(inputDto,statusCode));
                validationResultDto.setDescriptionKey(getFailureDescriptionKey(inputDto,statusCode));
            }
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            validationResultDto.setDescriptionKey(e.getLocalizedMessage());
            return validationResultDto;
        }
        return validationResultDto;
    }

    private String getSuccessDescription(ValidationInputDto inputDto, int statusCode) {
        List<ValidatorDefDto> validatorDefDtoList = inputDto.getValidatorDefs();
        String validatorList = "";
        String result = "";
        for (ValidatorDefDto list: validatorDefDtoList){
            validatorList+=list.getName();
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
            if (validatorList.contains("QualityCheckNoDataValidator")) {
                result = "For no face data, expected status code received:" + statusCode;
            }
            if (validatorList.contains("QualityCheckInvalidDataValidator")) {
                result = "No data for Quality Check, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
            if (validatorList.contains("MatchNoDataValidator")) {
                result = "No data for Match, expected status code received:" + statusCode;
            }
            if (validatorList.contains("MatchInvalidDataValidator")) {
                result = "For invalid data, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().contains(MethodName.EXTRACT_TEMPLATE.getCode())) {
            if (validatorList.contains("ExtractTemplateValidator")) {
                result = "Extract Template validation is successful";
            } else {
                result = "For invalid data, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.SEGMENT.getCode())) {
            if (validatorList.contains("SegmentValidator")) {
                result = "Segment validation is successful";
            } else {
                result = "For invalid data, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CONVERT_FORMAT.getCode())) {
            result = "For invalid data, expected status code received:" + statusCode;
        }
        return result;
    }
    private String getFailureDescription(ValidationInputDto inputDto, int statusCode) {
        List<ValidatorDefDto> validatorDefDtoList = inputDto.getValidatorDefs();
        String validatorList = "";
        String result = "";
        for (ValidatorDefDto list: validatorDefDtoList){
            validatorList+=list.getName();
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
            if (validatorList.contains("QualityCheckNoDataValidator")) {
                result = "For no face data, unexpected status code received:" + statusCode;
            }
            if (validatorList.contains("QualityCheckInvalidDataValidator")) {
                result = "No data for Quality Check, unexpected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
            if (validatorList.contains("MatchNoDataValidator")) {
                result = "No data for Match, unexpected status code received:" + statusCode;
            }
            if (validatorList.contains("MatchInvalidDataValidator")) {
                result = "For invalid data, unexpected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().contains(MethodName.EXTRACT_TEMPLATE.getCode())) {
            if (validatorList.contains("ExtractTemplateValidator")) {
                result = "Extract Template status code failed, received: " + statusCode;
            } else {
                result = "For invalid data, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.SEGMENT.getCode())) {
            if (validatorList.contains("SegmentValidator")) {
                result = "Segment status code failed, received: " + statusCode;
            } else {
                result = "For invalid data, expected status code received:" + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CONVERT_FORMAT.getCode())) {
            result = "For invalid data, expected status code received:" + statusCode;
        }
        return result;
    }
    private String getSuccessDescriptionKey(ValidationInputDto inputDto,int statusCode) {
        List<ValidatorDefDto> validatorDefDtoList = inputDto.getValidatorDefs();
        String validatorList = "";
        String result = "";
        for (ValidatorDefDto list: validatorDefDtoList){
            validatorList+=list.getName();
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
            if (validatorList.contains("QualityCheckNoDataValidator")) {
                result = "QUALITY_CHECK_NO_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
            if (validatorList.contains("QualityCheckInvalidDataValidator")) {
                result = "QUALITY_CHECK_INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
            if (validatorList.contains("MatchNoDataValidator")) {
                result = "MATCH_NO_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
            if (validatorList.contains("MatchInvalidDataValidator")) {
                result = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().contains(MethodName.EXTRACT_TEMPLATE.getCode())) {
            if (validatorList.contains("ExtractTemplateValidator")) {
                result = "EXTRACT_TEMPLATE_VALIDATOR_001";
            } else {
                result = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.SEGMENT.getCode())) {
            if (validatorList.contains("SegmentValidator")) {
                result = "SEGMENT_VALIDATOR_001";
            } else {
                result = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CONVERT_FORMAT.getCode())) {
            result = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
        }
        return result;
    }
    private String getFailureDescriptionKey(ValidationInputDto inputDto, int statusCode) {
        List<ValidatorDefDto> validatorDefDtoList = inputDto.getValidatorDefs();
        String validatorList = "";
        String result = "";
        for (ValidatorDefDto list: validatorDefDtoList){
            validatorList+=list.getName();
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
            if (validatorList.contains("QualityCheckNoDataValidator")) {
                result = "QUALITY_CHECK_NO_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
            if (validatorList.contains("QualityCheckInvalidDataValidator")) {
                result = "QUALITY_CHECK_INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
            if (validatorList.contains("MatchNoDataValidator")) {
                result = "MATCH_NO_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
            if (validatorList.contains("MatchInvalidDataValidator")) {
                result = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().contains(MethodName.EXTRACT_TEMPLATE.getCode())) {
            if (validatorList.contains("ExtractTemplateValidator")) {
                result = "EXTRACT_TEMPLATE_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            } else {
                result = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.SEGMENT.getCode())) {
            if (validatorList.contains("SegmentValidator")) {
                result = "SEGMENT_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            } else {
                result = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
            }
        }
        if (inputDto.getMethodName().equalsIgnoreCase(MethodName.CONVERT_FORMAT.getCode())) {
            result = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
        }
        return result;
    }

    protected abstract boolean isSuccessStatusCode(int statusCode);
}
