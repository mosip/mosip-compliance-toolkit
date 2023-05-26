package io.mosip.compliance.toolkit.validators;

import com.amazonaws.services.opsworks.model.App;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class IdentifyDuplicateFoundValidator extends ToolkitValidator {

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(
                    inputDto.getMethodResponse(),
                    ObjectNode.class);

            JsonNode candidateList = (JsonNode) methodResponse.get("candidateList");

            int count = Integer.parseInt(candidateList.get("count").asText());
            
            if (count > 0) {
            	validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Identify - found " + count + " duplicate for given the referenceId.");
                validationResultDto.setDescriptionKey("IDENTIFY_DUPLICATE_FOUND_VALIDATOR_001"
                + AppConstants.ARGUMENTS_DELIMITER
                + count);
            } else {
            	validationResultDto.setStatus(AppConstants.FAILURE);
            	  validationResultDto.setDescription("Identify - no duplicate found for given the referenceId.");
                  validationResultDto.setDescriptionKey("IDENTIFY_DUPLICATE_FOUND_VALIDATOR_002");
            }
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            return validationResultDto;
        }
        return validationResultDto;
    }

}
