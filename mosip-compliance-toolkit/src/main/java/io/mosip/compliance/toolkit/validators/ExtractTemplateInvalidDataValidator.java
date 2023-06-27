package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

public class ExtractTemplateInvalidDataValidator extends SDKValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			int statusCode = getStatusCode(inputDto);
			if (statusCode == 401 || statusCode == 403) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription(successInvalidDataDescription + statusCode);
				validationResultDto.setDescriptionKey(successInvalidDataDescriptionKey + statusCode);
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(failureInvalidDataDescription + statusCode);
				validationResultDto.setDescriptionKey(failureInvalidDataDescriptionKey + statusCode);
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
