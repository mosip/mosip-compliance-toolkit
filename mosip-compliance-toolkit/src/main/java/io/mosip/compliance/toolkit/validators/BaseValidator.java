package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

public interface BaseValidator {
	
	ValidationResultDto validateResponse(ValidationInputDto responseDto);
}
