package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;

public class SegmentNoInputDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "For invalid data, expected status code received: " + statusCode;
		successDescriptionKey = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		failureDescription = "For invalid data, unexpected status code received: " + statusCode;
		failureDescriptionKey = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode == 404;
	}
}
