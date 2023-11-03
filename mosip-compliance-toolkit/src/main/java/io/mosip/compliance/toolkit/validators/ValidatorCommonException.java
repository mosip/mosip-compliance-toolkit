package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.kernel.core.logger.spi.Logger;

public class ValidatorCommonException {
    public static void getExceptionMessageAndSetResultStatus(ValidationResultDto validationResultDto, Exception e,
            Logger log, String validatorClassName) {
        log.debug("sessionId", "idType", "id", e.getStackTrace());
        log.error("sessionId", "idType", "id", validatorClassName + e.getMessage());
        validationResultDto.setStatus(AppConstants.FAILURE);
        validationResultDto.setDescription(e.getLocalizedMessage());
        validationResultDto.setDescriptionKey(e.getLocalizedMessage());
    }
}