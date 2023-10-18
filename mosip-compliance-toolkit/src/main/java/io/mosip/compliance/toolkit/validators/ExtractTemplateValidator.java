package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;
import org.springframework.stereotype.Component;

@Component
public class ExtractTemplateValidator extends SDKNoOrInvalidDataValidator {

	@Override
	protected boolean isSuccessStatusCode(int statusCode) {
		successDescription = "Extract Template validation is successful";
		successDescriptionKey = "EXTRACT_TEMPLATE_VALIDATOR_001";
		failureDescription = "Extract Template status code failed, received: " + statusCode;
		failureDescriptionKey = "EXTRACT_TEMPLATE_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
		return statusCode >= 200 && statusCode <= 299;
	}
}
