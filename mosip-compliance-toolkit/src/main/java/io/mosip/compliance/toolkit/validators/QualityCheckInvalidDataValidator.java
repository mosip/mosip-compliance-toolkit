package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;

public class QualityCheckInvalidDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "No data for Quality Check, expected status code received: " + statusCode;
		successDescriptionKey = "QUALITY_CHECK_INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		failureDescription = "No data for Quality Check, unexpected status code received: " + statusCode;
		failureDescriptionKey = "QUALITY_CHECK_INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode == 401 || statusCode == 403;
	}
}
