package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.dto.ResponseValidateDto;
import io.mosip.compliance.toolkit.dto.ValidationResponseDto;

public interface BaseValidator {
	
	ValidationResponseDto validateResponse(ResponseValidateDto responseDto);
}
