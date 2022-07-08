package io.mosip.compliance.toolkit.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.exceptions.ErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ErrorMessages;
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
		if (Objects.isNull(reqTime)) {
			mosipLogger.error("", "", "validateReqTime", "requesttime is null");
			errors.rejectValue(REQUEST_TIME, ErrorCodes.TOOLKIT_REQ_ERR_003.toString(),
					String.format(ErrorMessages.INVALID_REQUEST_DATETIME.getMessage(), REQUEST_TIME));
		} else {
			LocalDate localDate = reqTime.toLocalDate();
			LocalDate serverDate = new Date().toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
			if (localDate.isBefore(serverDate) || localDate.isAfter(serverDate)) {
				errors.rejectValue(REQUEST_TIME, ErrorCodes.TOOLKIT_REQ_ERR_005.getCode(), String
						.format(ErrorMessages.INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE.getMessage(), REQUEST_TIME));
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
		String envVersion = env.getProperty("version");
		if (Objects.isNull(ver)) {
			mosipLogger.error("", "", "validateVersion", "version is null");
			errors.rejectValue(VER, ErrorCodes.TOOLKIT_REQ_ERR_002.toString(),
					String.format(ErrorMessages.INVALID_REQUEST_VERSION.getMessage(), VER));
		} else if (envVersion != null) {
			if (!envVersion.equalsIgnoreCase(ver)) {
				mosipLogger.error("", "", "validateVersion", "version is not correct");
				errors.rejectValue(VER, ErrorCodes.TOOLKIT_REQ_ERR_002.toString(),
						String.format(ErrorMessages.INVALID_REQUEST_VERSION.getMessage(), VER));
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
		if (Objects.isNull(request)) {
			mosipLogger.error("", "", "validateRequest", "\n" + "request is null");
			errors.rejectValue(REQUEST, ErrorCodes.TOOLKIT_REQ_ERR_004.getCode(),
					String.format(ErrorMessages.INVALID_REQUEST_BODY.getMessage(), REQUEST));
		}
	}



	public void validateId(String operation, String requestId, Errors errors) {
		if (Objects.nonNull(requestId)) {
			if (!requestId.equals(id.get(operation))) {
				mosipLogger.error("", "", "validateId", "\n" + "Id is not correct");
				errors.rejectValue(ID, ErrorCodes.TOOLKIT_REQ_ERR_001.getCode(),
						String.format(ErrorMessages.INVALID_REQUEST_ID.getMessage(), ID));
			}
		} else {
			mosipLogger.error("", "", "validateId", "\n" + "Id is null");
			errors.rejectValue(ID, ErrorCodes.TOOLKIT_REQ_ERR_001.getCode(),
					String.format(ErrorMessages.INVALID_REQUEST_ID.getMessage(), ID));
		}
	}

	public void validateProjectType(String projectType, Errors errors) {
		if (Objects.nonNull(projectType)) {
			boolean found = false;
			if (AppConstants.SBI.equalsIgnoreCase(projectType) || AppConstants.ABIS.equalsIgnoreCase(projectType)
					|| AppConstants.SDK.equalsIgnoreCase(projectType)) {
				found = true;
			}
			if (!found) {
				mosipLogger.error("", "", "validateProjectType", "\n" + "Project Type is not correct");
				errors.rejectValue("projectType", ErrorCodes.TOOLKIT_REQ_ERR_006.getCode(),
						String.format(ErrorMessages.INVALID_PROJECT_TYPE.getMessage(), projectType));
			}
		} else {
			mosipLogger.error("", "", "validateProjectType", "\n" + "Project Type is null");
			errors.rejectValue(projectType, ErrorCodes.TOOLKIT_REQ_ERR_006.getCode(),
					String.format(ErrorMessages.INVALID_PROJECT_TYPE.getMessage(), projectType));
		}
	}

}
