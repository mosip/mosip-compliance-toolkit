package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.dto.testcases.ResponseValidateDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;

public interface BaseValidator {
	
	ValidationResponseDto validateResponse(ResponseValidateDto responseDto);
}
