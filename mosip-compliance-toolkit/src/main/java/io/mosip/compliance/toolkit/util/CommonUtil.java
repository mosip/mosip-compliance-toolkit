package io.mosip.compliance.toolkit.util;

import java.util.ArrayList;
import java.util.List;

import io.mosip.kernel.core.exception.ServiceError;

public final class CommonUtil {

	public static List<ServiceError> getServiceErr(String errorCode, String errorMessage) {
		List<ServiceError> serviceErrorsList = new ArrayList<>();
		ServiceError serviceError = new ServiceError();
		serviceError.setErrorCode(errorCode);
		serviceError.setMessage(errorMessage);
		serviceErrorsList.add(serviceError);
		return serviceErrorsList;
	}
	
}