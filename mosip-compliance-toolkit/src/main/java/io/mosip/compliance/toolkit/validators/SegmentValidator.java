package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;

public class SegmentValidator extends SDKNoOrInvalidDataValidator {
	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "Segment validation is successful";
		successDescriptionKey = "SEGMENT_VALIDATOR_001";
		failureDescription = "Segment status code failed, received: " + statusCode;
		failureDescriptionKey = "SEGMENT_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode >= 200 && statusCode <= 299;
	}
}
