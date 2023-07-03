package io.mosip.compliance.toolkit.validators;

public class SegmentValidator extends SDKDataValidator {
	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}
}
