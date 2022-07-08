package io.mosip.compliance.toolkit.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Instantiates a new error DTO.
 *
 * @param errorcode the error code
 * @param message   the message
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDto implements Serializable {

	private static final long serialVersionUID = 2452990684776944908L;

	/** The error code. */
	private String errorCode;

	/** The message. */
	private String errorMessage;
}
