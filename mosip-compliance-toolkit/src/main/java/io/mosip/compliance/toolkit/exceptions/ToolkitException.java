package io.mosip.compliance.toolkit.exceptions;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom ToolkitException Class in case of error occurred in services
 * 
 * @see io.mosip.kernel.core.exception.BaseUncheckedException
 * @author Janardhan B S
 * @since 1.0.0
 */
public class ToolkitException extends BaseUncheckedException {
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 687991492884005033L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ToolkitException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the specified cause
	 */
	public ToolkitException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}
}