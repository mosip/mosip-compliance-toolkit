package io.mosip.compliance.toolkit.validators;

public class SegmentInvalidDataValidator extends SDKInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 401 || statusCode == 405;
	}
}
