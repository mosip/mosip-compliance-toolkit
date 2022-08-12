package io.mosip.compliance.toolkit.validators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.CertificationTypes;
import io.mosip.compliance.toolkit.constants.DeviceStatus;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.PartnerTypes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.StringUtil;

@Component
public class SignatureValidator extends SBIValidator {

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
						validationResultDto = validateDiscoverySignature(responseDto);
						break;
					case INFO:
						validationResultDto = validateDeviceSignature(responseDto);
						break;
					case CAPTURE:
						validationResultDto = validateCaptureSignature(responseDto);
						break;
					case RCAPTURE:
						validationResultDto = validateRCaptureSignature(responseDto);
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

	private ValidationResultDto validateDiscoverySignature(ValidationInputDto responseDto) {
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

	private ValidationResultDto validateDeviceSignature(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDeviceInfoResponse = (ArrayNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ArrayNode.class);

			for (int deviceIndex = 0; deviceIndex < arrDeviceInfoResponse.size(); deviceIndex++) {
				ObjectNode deviceInfoNode = (ObjectNode) arrDeviceInfoResponse.get(deviceIndex);
				if (isUnSignedDeviceInfo(deviceInfoNode)) {
					validationResultDto = validateUnSignedDeviceInfo(deviceInfoNode);
				} else {
					validationResultDto = validateSignedDeviceInfoSignature(deviceInfoNode,
							responseDto.getCertificationType());
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

	private ValidationResultDto validateUnSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode deviceInfoDto = getUnsignedDeviceInfo(objectNode.get("deviceInfo").asText());
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

	private ValidationResultDto validateSignedDeviceInfoSignature(ObjectNode objectNode, String certificationType) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String deviceInfo = objectNode.get("deviceInfo").asText();
			List<String> arrJwtInfo = new ArrayList<String>();
			validationResultDto = validateSignatureValidity(deviceInfo, arrJwtInfo);
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				validationResultDto = validateTrustRoot(arrJwtInfo.get(INFO_INDEX_CERTIFICATE),
						PartnerTypes.DEVICE.toString());
				if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
					ObjectNode deviceInfoDto = objectMapper.readValue(arrJwtInfo.get(INFO_INDEX_PAYLOAD),
							ObjectNode.class);

					if (Objects.isNull(deviceInfoDto)) {
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Device info Decoded value is null");
					} else {
						validationResultDto = validateSignedDigitalIdSignature(deviceInfoDto.get("digitalId").asText(),
								certificationType);
					}
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateCaptureSignature(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode captureInfoResponse = (ObjectNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ObjectNode.class);

			final JsonNode arrBiometricNodes = captureInfoResponse.get("biometrics");
			if (arrBiometricNodes.isArray()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					String dataInfo = biometricNode.get("data").asText();
					List<String> arrJwtInfo = new ArrayList<String>();

					validationResultDto = validateSignatureValidity(dataInfo, arrJwtInfo);
					if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
						String biometricData = arrJwtInfo.get(INFO_INDEX_PAYLOAD);
						ObjectNode biometricDataNode = (ObjectNode) objectMapper.readValue(biometricData,
								ObjectNode.class);

						validationResultDto = validateSignedDigitalIdSignature(
								biometricDataNode.get("digitalId").asText(), responseDto.getCertificationType());
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateRCaptureSignature(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode captureInfoResponse = (ObjectNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ObjectNode.class);

			final JsonNode arrBiometricNodes = captureInfoResponse.get("biometrics");
			if (arrBiometricNodes.isArray()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					String dataInfo = biometricNode.get("data").asText();
					List<String> arrJwtInfo = new ArrayList<String>();

					validationResultDto = validateSignatureValidity(dataInfo, arrJwtInfo);
					if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
						String biometricData = arrJwtInfo.get(INFO_INDEX_PAYLOAD);
						ObjectNode biometricDataNode = (ObjectNode) objectMapper.readValue(biometricData,
								ObjectNode.class);

						validationResultDto = validateSignedDigitalIdSignature(
								biometricDataNode.get("digitalId").asText(), responseDto.getCertificationType());
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateSignedDigitalIdSignature(String digitalId, String certificationType) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			CertificationTypes certification = CertificationTypes.fromCode(certificationType);
			List<String> arrJwtInfo = new ArrayList<String>();

			if (certification == CertificationTypes.L0) {
				validationResultDto = validateSignatureValidity(digitalId, arrJwtInfo);
				if (validationResultDto.getStatus().equals(AppConstants.SUCCESS))
					validationResultDto = validateTrustRoot(arrJwtInfo.get(INFO_INDEX_CERTIFICATE),
							PartnerTypes.DEVICE.toString());
			} else if (certification == CertificationTypes.L1) {
				validationResultDto = validateSignatureValidity(digitalId, arrJwtInfo);
				if (validationResultDto.getStatus().equals(AppConstants.SUCCESS))
					validationResultDto = validateTrustRoot(arrJwtInfo.get(INFO_INDEX_CERTIFICATE),
							PartnerTypes.FTM.toString());
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

	public ValidationResultDto validateTrustRoot(String certificateData, String partnerType) throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		DeviceTrustRequestDto trustRequest = new DeviceTrustRequestDto();

		trustRequest.setCertificateData(getCertificateData(certificateData));
		trustRequest.setPartnerDomain(partnerType);
		deviceValidatorDto.setRequest(trustRequest);

		io.restassured.response.Response postResponse = getPostResponse(getKeyManagerVerifyCertificateTrustUrl,
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
			validationResultDto.setDescription(
					"Exception in Trust root Validation - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}
}
