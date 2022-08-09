package io.mosip.compliance.toolkit.validators;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.CertificationTypes;
import io.mosip.compliance.toolkit.constants.DeviceStatus;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.StringUtil;

@Component
public class SignatureValidator extends ToolkitValidator {

	@Value("${mosip.service.auth.appid}")
	private String getAuthAppId;

	@Value("${mosip.service.auth.clientid}")
	private String getAuthClientId;

	@Value("${mosip.service.auth.secretkey}")
	private String getAuthSecretKey;

	@Value("${mosip.service.authmanager.url}")
	private String getAuthManagerUrl;

	@Value("${mosip.service.keymanager.verifyCertificateTrust.url}")
	private String getKeyManagerVerifyCertificateTrustUrl;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			if (validateMethodName(responseDto.getMethodName())) {
				if (Objects.nonNull(responseDto.getMethodResponse())) {
					switch (MethodName.fromCode(responseDto.getMethodName())) {
					case DEVICE:
						validationResultDto = validateDiscoveryInfo(responseDto);
						break;
					case INFO:
						validationResultDto = validateDeviceInfo(responseDto);
						break;
					case CAPTURE:
						validationResultDto = validateCaptureInfo(responseDto);
						break;
					case RCAPTURE:
						validationResultDto = validateRCaptureInfo(responseDto);
						break;
					default:
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Method not supported");
						break;
					}
				} else {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Response is empty");
				}
			}
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateDiscoveryInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDiscoverResponse = (ArrayNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ArrayNode.class);
			ObjectNode discoveryInfoNode = (ObjectNode) arrDiscoverResponse.get(0);
			
			String digitalId = StringUtil
					.toUtf8String(StringUtil.base64UrlDecode(discoveryInfoNode.get("digitalId").asText()));
			validationResultDto = validateUnsignedDigitalID(digitalId);
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateUnsignedDigitalID(String digitalId) throws Exception {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		ObjectNode digitalIdDto = objectMapper.readValue(digitalId, ObjectNode.class);
		if (Objects.isNull(digitalIdDto) || digitalIdDto.get("type").isNull()
				|| digitalIdDto.get("deviceSubType").isNull()) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("UnsignedDigitalID value is null");
		} else {
			validationResultDto.setStatus(AppConstants.SUCCESS);
			validationResultDto.setDescription("UnsignedDigitalID validation success");
		}
		return validationResultDto;
	}

	private ValidationResultDto validateDeviceInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDeviceInfoResponse = (ArrayNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ArrayNode.class);

			for (int deviceIndex = 0; deviceIndex < arrDeviceInfoResponse.size(); deviceIndex++) {
				ObjectNode deviceInfoNode = (ObjectNode) arrDeviceInfoResponse.get(deviceIndex);
				if (isUnSignedDeviceInfo(deviceInfoNode)) {
					validationResultDto = validateUnSignedDeviceInfo(deviceInfoNode);
				} else {
					validationResultDto = validateSignedDeviceInfo(deviceInfoNode);
				}
				if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
					break;
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private boolean isUnSignedDeviceInfo(ObjectNode objectNode) {
		try {
			ObjectNode deviceInfoDto = getUnsignedDeviceInfoDto(objectNode.get("deviceInfo").asText());
			if (!Objects.isNull(deviceInfoDto)) {
				return true;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
		}
		return false;
	}

	private ObjectNode getUnsignedDeviceInfoDto(String deviceInfoResponse)
			throws JsonParseException, JsonMappingException, IOException {
		String deviceInfo = StringUtil.toUtf8String(StringUtil.base64UrlDecode(deviceInfoResponse));
		return objectMapper.readValue(deviceInfo, ObjectNode.class);
	}

	private ValidationResultDto validateUnSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode deviceInfoDto = getUnsignedDeviceInfoDto(objectNode.get("deviceInfo").asText());
			DeviceStatus deviceStatus = DeviceStatus.fromCode(deviceInfoDto.get("deviceStatus").asText());

			if (deviceStatus == DeviceStatus.NOT_REGISTERED) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Device is not registered");
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Device is registered, so can not be unsigned");
			}
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			validationResultDto = validateSignatureValidity(objectNode.get("deviceInfo").asText());
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				ObjectNode deviceInfoDto = objectMapper.readValue(getJWTPayload(objectNode.get("deviceInfo").asText()),
						ObjectNode.class);
				if (Objects.isNull(deviceInfoDto)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Device info Decoded value is null");
				} else {
					validationResultDto = validateSignedDigitalID(deviceInfoDto.get("digitalId").asText(),
							deviceInfoDto.get("certification").asText());
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateSignedDigitalID(String digitalId, String certification) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			CertificationTypes certificationType = CertificationTypes.fromCode(certification);
			if (certificationType == CertificationTypes.L0)
				validationResultDto = validateSignatureValidity(digitalId);
			else if (certificationType == CertificationTypes.L1)
				validationResultDto = validateSignatureValidityL1(digitalId);
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateCaptureInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {

		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateRCaptureInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {

		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private boolean validateMethodName(String methodName) throws Exception {
		MethodName.fromCode(methodName);
		return true;
	}

	private ValidationResultDto validateSignatureValidity(String info) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(info);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			jws.setKey(publicKey);
			jws.getLeafCertificateHeaderValue().checkValidity();
			validationResultDto.setStatus(AppConstants.SUCCESS);
			validationResultDto.setDescription("Signature Validation Success");
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

	private String getJWTPayload(String info) throws JoseException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(info);
		List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
		X509Certificate certificate = certificateChainHeaderValue.get(0);
		PublicKey publicKey = certificate.getPublicKey();
		jws.setKey(publicKey);
		boolean verified = jws.verifySignature();
		if (verified) {
			String payload = jws.getEncodedPayload();
			return StringUtil.toUtf8String(StringUtil.base64UrlDecode((payload)));
		}
		return null;
	}

	private ValidationResultDto validateSignatureValidityL1(String info) {
		return validateSignatureValidity(info);
	}

	public ValidationResultDto trustRootValidation(String certificateData, String certification) throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		DeviceTrustRequestDto trustRequest = new DeviceTrustRequestDto();

		trustRequest.setCertificateData(getCertificateData(certificateData));
		trustRequest.setPartnerDomain(certification);
		deviceValidatorDto.setRequest(trustRequest);

		io.restassured.response.Response postResponse = getPostResponse(getAuthManagerUrl,
				getKeyManagerVerifyCertificateTrustUrl, getAuthAppId, getAuthClientId, getAuthSecretKey,
				deviceValidatorDto);

		try {
			DeviceValidatorResponseDto deviceValidatorResponseDto = objectMapper
					.readValue(postResponse.getBody().asString(), DeviceValidatorResponseDto.class);

			if ((deviceValidatorResponseDto.getErrors() != null && deviceValidatorResponseDto.getErrors().size() > 0)
					|| (deviceValidatorResponseDto.getResponse().getStatus().equals("false"))) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Trust Validation Failed");
			} else {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Trust Validation Success");
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("Exception in Trust Validation " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}
}
