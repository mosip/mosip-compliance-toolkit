package io.mosip.compliance.toolkit.validators;

public class ExtractTemplateSDKInvalidDataValidator extends SDKInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 401 || statusCode == 403;
	}
}
