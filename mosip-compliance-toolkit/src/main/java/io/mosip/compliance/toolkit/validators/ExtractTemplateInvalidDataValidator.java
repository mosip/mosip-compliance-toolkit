package io.mosip.compliance.toolkit.validators;

public class ExtractTemplateInvalidDataValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode == 401 || statusCode == 403;
	}
}
