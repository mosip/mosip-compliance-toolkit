package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;
import org.springframework.stereotype.Component;

@Component
public class ExceptionValidator extends SDKNoOrInvalidDataValidator {

    @Override
    protected boolean isSuccessStatusCode(int statusCode) {
        successDescription = "Exception validation is successful, received status code: " + statusCode;
        successDescriptionKey = "EXCEPTION_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
        failureDescription = "Exception validation failed, received status code: " + statusCode;
        failureDescriptionKey = "EXCEPTION_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + statusCode;
        return statusCode == 404 || statusCode == 402 || statusCode == 401;
    }
}

