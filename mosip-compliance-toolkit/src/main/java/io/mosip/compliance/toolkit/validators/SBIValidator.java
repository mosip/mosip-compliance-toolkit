package io.mosip.compliance.toolkit.validators;

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.StringUtil;
import lombok.Data;

public abstract class SBIValidator extends ToolkitValidator {

	protected static final String TRUST_FOR_DIGITAL_ID = "Digital Id";
	protected static final String TRUST_FOR_DEVICE_INFO = "Device Info";
	protected static final String TRUST_FOR_BIOMETRIC_INFO = "Biometric Data";

	protected static final String DIGITAL_ID = "digitalId";
	protected static final String DIGITAL_ID_DECODED = "digitalIdDecoded";
	protected static final String DEVICE_SUB_TYPE = "deviceSubType";
	protected static final String DEVICE_TYPE = "type";
	protected static final String CERTIFICATION_TYPE = "certificationType";
	protected static final String BIOMETRICS = "biometrics";
	protected static final String DATA = "data";
	protected static final String DEVICE_INFO = "deviceInfo";
	protected static final String DEVICE_INFO_DECODED = "deviceInfoDecoded";
	protected static final String DEVICE_STATUS = "deviceStatus";
	protected static final String MAKE = "make";
	protected static final String MODEL = "model";
	protected static final String SERIAL_NO = "serialNo";
	private static final String ALG = "alg";
	private static final String X5C = "x5c";
	public static final String KEY_SPLITTER = "#KEY_SPLITTER#";
	public static final String ISO19794_5_2011 = "ISO19794_5_2011";
	public static final String ISO19794_6_2011 = "ISO19794_6_2011";
	public static final String ISO19794_4_2011 = "ISO19794_4_2011";

	public static final String BIO = "bio";
	public static final String DECODED_DATA = "dataDecoded";
	public static final String THUMB_PRINT = "thumbprint";
	public static final String SESSION_KEY = "sessionKey";

	public static final String BIO_VALUE = "bioValue";
	public static final String TIME_STAMP = "timestamp";
	public static final String TRANSACTION_ID = "transactionId";
	public static final String PURPOSE = "purpose";
	public static final String BIO_TYPE = "bioType";
	public static final String BIO_SUBTYPE = "bioSubType";

	private final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";
	private final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

	protected boolean validateMethodName(String methodName) throws Exception {
		MethodName.fromCode(methodName);
		return true;
	}

	protected ValidationResultDto checkIfJWTSignatureIsValid(String jwtInfo) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(jwtInfo);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			jws.setKey(publicKey);
			jws.getLeafCertificateHeaderValue().checkValidity();
			boolean verified = jws.verifySignature();
			if (verified) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("JWT Signature validation is successful");
				validationResultDto.setDescriptionKey("JWT_SIGNATURE_001");
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("JWT Signature validation failed");
				validationResultDto.setDescriptionKey("JWT_SIGNATURE_002");
			}
		} catch (CertificateExpiredException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription(" CertificateExpiredException - " + "with Message - " + e.getLocalizedMessage());
		} catch (CertificateNotYetValidException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					" CertificateNotYetValidException - " + "with Message - " + e.getLocalizedMessage());
		} catch (JoseException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(" JoseException - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected String getCertificate(String jwtInfo) throws JoseException, IOException {
		String certificate = null;
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(jwtInfo);
		String encodedHeader = jws.getHeaders().getEncodedHeader();
		String jsonHeader = StringUtil.toUtf8String(StringUtil.base64UrlDecode(encodedHeader));
		ObjectNode headerNode = (ObjectNode) objectMapperConfig.objectMapper().readValue(jsonHeader, ObjectNode.class);
		String algType = headerNode.get(ALG).asText();
		if (algType.equals(AppConstants.RS256_ALGORITHM_TYPE)) {
			ArrayNode arrCertificates = (ArrayNode) headerNode.get(X5C);
			certificate = arrCertificates.get(0).asText();
		}
		return certificate;
	}

	protected String getPayload(String jwtInfo) throws JoseException, IOException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(jwtInfo);
		String payload = jws.getEncodedPayload();
		return StringUtil.toUtf8String(StringUtil.base64UrlDecode(payload));
	}

	protected boolean isDeviceInfoUnSigned(ObjectNode objectNode) {
		try {
			ObjectNode deviceInfo = getUnsignedDeviceInfo(objectNode.get(DEVICE_INFO).asText());
			if (!Objects.isNull(deviceInfo)) {
				return true;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return false;
	}

	protected ObjectNode getUnsignedDeviceInfo(String deviceInfoResponse)
			throws JsonParseException, JsonMappingException, IOException {
		String deviceInfo = StringUtil.toUtf8String(StringUtil.base64UrlDecode(deviceInfoResponse));
		return objectMapperConfig.objectMapper().readValue(deviceInfo, ObjectNode.class);
	}

	protected String getCertificateData(String certificateInfo) {
		return BEGIN_CERTIFICATE + certificateInfo + END_CERTIFICATE;
	}

	protected String getCurrentDateAndTimeForAPI() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
		String currentTime = time.format(dateFormat);
		return currentTime;
	}

	@Data
	protected static class DeviceTrustRequestDto implements Serializable {
		private static final long serialVersionUID = -4874932813550831900L;
		String certificateData;
		String partnerDomain;
	}

	@Data
	public static class DeviceValidatorDto implements Serializable {
		private static final long serialVersionUID = 6604417847897263692L;
		String id;
		Object metadata;
		Object request;
		String requesttime;
		String version;
	}

	@Data
	public static class DecryptValidatorDto implements Serializable {
		private static final long serialVersionUID = -2112567140911169485L;
		String id;
		Object metadata;
		Object request;
		String requesttime;
		String version;
	}

	@Data
	protected static class DecryptRequestDto implements Serializable {
		private static final long serialVersionUID = 6098449354733115976L;
		String applicationId;
		String referenceId;
		String timeStamp;
		String data;
		String salt;
		String aad;
	}

	@Data
	protected static class DecryptValidatorResponseDto implements Serializable {
		private static final long serialVersionUID = 414320755574491361L;
		String id;
		Object metadata;
		DecryptValidatorResponse response;
		String responsetime;
		String version;
		List<ErrorDto> errors;
	}

	@Data
	protected static class DeviceValidatorResponseDto implements Serializable {
		private static final long serialVersionUID = -3533369757932860828L;
		String id;
		Object metadata;
		DeviceValidatorResponse response;
		String responsetime;
		String version;
		List<ErrorDto> errors;
	}

	@Data
	protected static class ErrorDto {
		String errorCode;
		String message;
	}

	@Data
	protected static class DecryptValidatorResponse implements Serializable {
		private static final long serialVersionUID = 5675428669597716934L;
		String data;
	}

	@Data
	protected static class DeviceValidatorResponse implements Serializable {
		private static final long serialVersionUID = -2714540550294089151L;
		String status;
	}

	@Data
	protected static class DeviceValidatorRequestDto implements Serializable {
		private static final long serialVersionUID = 6066767653045843567L;
		String deviceCode;
		String deviceServiceVersion;
		DeviceValidatorDigitalIdDto digitalId;
		String purpose;
		String timeStamp;
	}

	@Data
	protected static class DeviceValidatorDigitalIdDto implements Serializable {
		private static final long serialVersionUID = 5438972186954170612L;
		String dateTime;
		String deviceSubType;
		String dp;
		String dpId;
		String make;
		String model;
		String serialNo;
		String type;
	}

}
