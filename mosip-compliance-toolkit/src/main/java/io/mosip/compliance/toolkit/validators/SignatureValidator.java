package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class SignatureValidator implements BaseValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		try {
			//TODO: replace with code
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("signature validation failed");
			return validationResultDto;
		} catch (Exception e) {
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
	}
}
