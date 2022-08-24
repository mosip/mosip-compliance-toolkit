package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

public class NoFaceDataValidator extends SDKValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode methodResponse = (ObjectNode) objectMapper.readValue(inputDto.getMethodResponse(),
					ObjectNode.class);
			JsonNode mainResponse = (JsonNode) methodResponse.get("response");
			int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
			if (statusCode == 402 || statusCode == 403) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("For no face data, expected status code received:" + statusCode);
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("For no face data, unexpected status code received:" + statusCode);
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}
}
