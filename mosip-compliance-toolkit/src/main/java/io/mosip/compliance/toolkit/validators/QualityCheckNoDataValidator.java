package io.mosip.compliance.toolkit.validators;

public class QualityCheckNoDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 404;
	}
}
