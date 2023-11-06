package io.mosip.compliance.toolkit.util;

import java.util.ArrayList;
import java.util.List;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;

public final class CommonErrorUtil {

	public static List<ServiceError> getServiceErr(String errorCode, String errorMessage) {
		List<ServiceError> serviceErrorsList = new ArrayList<>();
		ServiceError serviceError = new ServiceError();
		serviceError.setErrorCode(errorCode);
		serviceError.setMessage(errorMessage);
		serviceErrorsList.add(serviceError);
		return serviceErrorsList;
	}
	public static void getExceptionMessageAndSetResultStatus(ValidationResultDto validationResultDto, Exception e,
															 Logger log, String validatorClassName) {
		log.debug("sessionId", "idType", "id", e.getStackTrace());
		log.error("sessionId", "idType", "id", validatorClassName + e.getMessage());
		validationResultDto.setStatus(AppConstants.FAILURE);
		validationResultDto.setDescription(e.getLocalizedMessage());
		validationResultDto.setDescriptionKey(e.getLocalizedMessage());
	}
}