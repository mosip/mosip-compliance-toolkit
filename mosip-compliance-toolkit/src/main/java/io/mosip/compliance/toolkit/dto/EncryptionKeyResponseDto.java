package io.mosip.compliance.toolkit.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EncryptionKeyResponseDto {

	String id;
	Object metadata;
	EncryptionKeyResponse response;
	String responsetime;
	String version;
	List<ErrorDto> errors;

	@Data
	public static class ErrorDto {
		String errorCode;
		String message;
	}

	@Data
	public static class EncryptionKeyResponse {
		String certificate;
		String issuedAt;
		String expiryAt;
		String timestamp;
	}

}
