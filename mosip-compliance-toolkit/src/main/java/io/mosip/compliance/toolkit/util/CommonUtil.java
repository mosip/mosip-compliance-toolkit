package io.mosip.compliance.toolkit.util;

import java.util.ArrayList;
import java.util.List;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.kernel.core.exception.ServiceError;

public final class CommonUtil {

	public static List<ServiceError> getInvalidRequestBodyErr() {
		List<ServiceError> serviceErrorsList = new ArrayList<>();
		ServiceError serviceError = new ServiceError();
		serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
		serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
		serviceErrorsList.add(serviceError);
		return serviceErrorsList;
	}
	
}