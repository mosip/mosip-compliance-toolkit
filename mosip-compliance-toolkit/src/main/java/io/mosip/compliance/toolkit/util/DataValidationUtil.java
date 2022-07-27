package io.mosip.compliance.toolkit.util;

import org.springframework.validation.Errors;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public final class DataValidationUtil {
	/**
	 * Instantiates a new data validation util.
	 */
	private DataValidationUtil() {
	}
	

	/**
	 * Get list of errors from error object and build and throw {@code ToolkitException}.
	 *
	 * @param errors the errors
	 * @throws ToolkitException
	 */
	public synchronized static void validate(Errors errors, String operation) throws Exception {
		if (errors.hasErrors()) {
			errors.getAllErrors().stream()
					.forEach(error ->{
						if (ToolkitErrorCodes.fromErrorCode(error.getCode()) == ToolkitErrorCodes.PROJECTS_NOT_AVAILABLE)
							throw new ToolkitException(error.getCode(), error.getDefaultMessage() + " " + operation);
						else
							throw new ToolkitException(error.getCode(), error.getDefaultMessage());
				} );
		}
	}
}