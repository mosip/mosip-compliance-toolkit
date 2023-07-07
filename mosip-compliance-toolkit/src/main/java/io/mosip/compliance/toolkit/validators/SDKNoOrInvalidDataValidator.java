package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidatorDefDto;

import java.util.List;

public abstract class SDKNoOrInvalidDataValidator extends ToolkitValidator{

    protected String successDescription;
    protected String successDescriptionKey;
    protected String failureDescription;
    protected String failureDescriptionKey;

    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodResponse(),
                    ObjectNode.class);
            JsonNode mainResponse = (JsonNode) methodResponse.get("response");
            int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
            if (isSuccessStatusCode(statusCode)) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription(successDescription);
                validationResultDto.setDescriptionKey(successDescriptionKey);
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription(failureDescription);
                validationResultDto.setDescriptionKey(failureDescriptionKey);
            }
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            validationResultDto.setDescriptionKey(e.getLocalizedMessage());
            return validationResultDto;
        }
        return validationResultDto;
    }

    protected abstract boolean isSuccessStatusCode(int statusCode);
}
