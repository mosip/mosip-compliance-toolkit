package io.mosip.compliance.toolkit.validators;

public class MatchNoDataValidator extends SDKDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 404;
	}
}
