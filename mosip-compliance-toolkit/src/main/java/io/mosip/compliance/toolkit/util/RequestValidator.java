package io.mosip.compliance.toolkit.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class RequestValidator implements Validator {

	/**
	 * Logger configuration for CommonRequestValidator
	 */
	private static Logger mosipLogger = LoggerConfiguration.logConfig(RequestValidator.class);

	/** The Constant REQUEST. */
	private static final String REQUEST = "request";

	/** The Constant TIMESTAMP. */
	private static final String REQUEST_TIME = "requesttime";

	/** The Constant VER. */
	private static final String VER = "version";

	/** The Constant ID. */
	protected static final String ID = "id";

	/** The Environment. */
	@Autowired
	protected Environment env;

	/** The id. */
	@Resource
	protected Map<String, String> id;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(RequestWrapper.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(@NonNull Object target, Errors errors) {
		RequestWrapper<Object> request = (RequestWrapper<Object>) target;
		validateReqTime(request.getRequesttime(), errors);
		validateVersion(request.getVersion(), errors);
		validateRequest(request.getRequest(), errors);
	}
	
	/**
	 * Validate request time.
	 *
	 * @param reqTime the timestamp
	 * @param errors  the errors
	 */
	protected void validateReqTime(LocalDateTime reqTime, Errors errors) {
		ToolkitErrorCodes errorCode = null; 
		if (Objects.isNull(reqTime)) {
			mosipLogger.error("", "", "validateReqTime", "requesttime is null");
			errorCode = ToolkitErrorCodes.INVALID_REQUEST_DATETIME;
			errors.rejectValue(REQUEST_TIME, errorCode.getErrorCode(),
					String.format(errorCode.getErrorMessage(), REQUEST_TIME));
		} else {
			LocalDate localDate = reqTime.toLocalDate();
			LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
			if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
				errorCode = ToolkitErrorCodes.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE;
				errors.rejectValue(REQUEST_TIME, errorCode.getErrorCode(), String
						.format(errorCode.getErrorMessage(), REQUEST_TIME));
			}
		}
	}

	/**
	 * Validate version.
	 *
	 * @param ver    the ver
	 * @param errors the errors
	 */
	protected void validateVersion(String ver, Errors errors) {
		ToolkitErrorCodes errorCode = null; 
		String envVersion = env.getProperty("version");
		if (Objects.isNull(ver)) {
			mosipLogger.error("", "", "validateVersion", "version is null");
			errorCode = ToolkitErrorCodes.INVALID_REQUEST_VERSION;
			errors.rejectValue(VER, errorCode.getErrorCode(),
					String.format(errorCode.getErrorMessage(), VER));
		} else if (envVersion != null) {
			if (!envVersion.equalsIgnoreCase(ver)) {
				mosipLogger.error("", "", "validateVersion", "version is not correct");
				errorCode = ToolkitErrorCodes.INVALID_REQUEST_VERSION;
				errors.rejectValue(VER, errorCode.getErrorCode(),
						String.format(errorCode.getErrorMessage(), VER));
			}
		}
	}

	/**
	 * Validate request.
	 *
	 * @param request the request
	 * @param errors  the errors
	 */
	protected void validateRequest(Object request, Errors errors) {
		ToolkitErrorCodes errorCode = null; 
		if (Objects.isNull(request)) {
			mosipLogger.error("", "", "validateRequest", "\n" + "request is null");
			errorCode = ToolkitErrorCodes.INVALID_REQUEST_BODY;
			errors.rejectValue(REQUEST, errorCode.getErrorCode(),
					String.format(errorCode.getErrorMessage(), REQUEST));
		}
	}

	public void validateId(String operation, String requestId, Errors errors) {
		ToolkitErrorCodes errorCode = null; 
		if (Objects.nonNull(requestId)) {
			if (!requestId.equals(id.get(operation))) {
				mosipLogger.error("", "", "validateId", "\n" + "Id is not correct");
				errorCode = ToolkitErrorCodes.INVALID_REQUEST_ID;
				errors.rejectValue(ID, errorCode.getErrorCode(),
						String.format(errorCode.getErrorMessage(), ID));
			}
		} else {
			mosipLogger.error("", "", "validateId", "\n" + "Id is null");
			errorCode = ToolkitErrorCodes.INVALID_REQUEST_ID;
			errors.rejectValue(ID, errorCode.getErrorCode(),
					String.format(errorCode.getErrorMessage(), ID));
		}
	}

	public void validateProjectType(String projectType, Errors errors) {
		ToolkitErrorCodes errorCode = null; 
		if (Objects.nonNull(projectType)) {
			boolean found = false;
			if (AppConstants.SBI.equalsIgnoreCase(projectType) || AppConstants.ABIS.equalsIgnoreCase(projectType)
					|| AppConstants.SDK.equalsIgnoreCase(projectType)) {
				found = true;
			}
			if (!found) {
				mosipLogger.error("", "", "validateProjectType", "\n" + "Project Type is not correct");
				errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
				errors.rejectValue("projectType", errorCode.getErrorCode(),
						String.format(errorCode.getErrorMessage(), projectType));
			}
		} else {
			mosipLogger.error("", "", "validateProjectType", "\n" + "Project Type is null");
			errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
			errors.rejectValue(projectType, errorCode.getErrorCode(),
					String.format(errorCode.getErrorMessage(), projectType));
		}
	}
}
