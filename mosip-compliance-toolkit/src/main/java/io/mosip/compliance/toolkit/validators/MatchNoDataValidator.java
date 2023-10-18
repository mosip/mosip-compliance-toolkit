package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;

public class MatchNoDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "No data for Match, expected status code received: " + statusCode;
		successDescriptionKey = "MATCH_NO_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		failureDescription = "No data for Match, unexpected status code received: " + statusCode;
		failureDescriptionKey = "MATCH_NO_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode == 404;
	}
}
