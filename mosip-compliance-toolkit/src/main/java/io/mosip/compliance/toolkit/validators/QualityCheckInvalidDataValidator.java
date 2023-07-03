package io.mosip.compliance.toolkit.validators;

public class QualityCheckInvalidDataValidator extends SDKDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 401 || statusCode == 403;
	}
}
