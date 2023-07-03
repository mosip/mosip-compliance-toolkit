package io.mosip.compliance.toolkit.validators;

public class SegmentInvalidDataValidator extends SDKDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 401 || statusCode == 405;
	}
}
