package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

@Component
public class ExtractTemplateValidator extends SDKDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		return statusCode >= 200 && statusCode <= 299;
	}
}
