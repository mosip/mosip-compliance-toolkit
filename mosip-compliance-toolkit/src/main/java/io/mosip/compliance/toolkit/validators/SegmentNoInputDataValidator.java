package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

public class SegmentNoInputDataValidator extends SDKValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodResponse(),
					ObjectNode.class);
			JsonNode mainResponse = (JsonNode) methodResponse.get("response");
			int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
			if (statusCode == 404) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("For invalid data, expected status code received:" + statusCode);
				validationResultDto.setDescriptionKey("SEGMENT_NO_INPUT_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode);
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("For invalid data, unexpected status code received:" + statusCode);
				validationResultDto.setDescriptionKey("SEGMENT_NO_INPUT_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode);
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}

}
