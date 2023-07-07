package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;

public class QualityCheckNoDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "For no face data, expected status code received: " + statusCode;
		successDescriptionKey = "QUALITY_CHECK_NO_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		failureDescription = "For no face data, unexpected status code received: " + statusCode;
		failureDescriptionKey = "QUALITY_CHECK_NO_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode == 404;
	}

}
