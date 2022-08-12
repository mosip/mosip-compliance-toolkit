package io.mosip.compliance.toolkit.validators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.StringUtil;

@Component
public class ValidDeviceCheckValidator extends SBIValidator {

	@Value("${mosip.service.validation.url}")
	private String getValidDeviceCheckUrl;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			if (validateMethodName(responseDto.getMethodName())) {
				if (Objects.nonNull(responseDto.getMethodResponse())) {
					switch (MethodName.fromCode(responseDto.getMethodName())) {
					case DEVICE:
						validationResultDto = validateDiscoveryDeviceCheck(responseDto);
						break;
					case INFO:
						validationResultDto = validateDeviceInfoDeviceCheck(responseDto);
						break;
					case CAPTURE:
						validationResultDto = validateCaptureDeviceCheck(responseDto);
						break;
					case RCAPTURE:
						validationResultDto = validateRCaptureDeviceCheck(responseDto);
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

	private ValidationResultDto validateDiscoveryDeviceCheck(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDiscoverResponse = (ArrayNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ArrayNode.class);
			ObjectNode discoveryInfoNode = (ObjectNode) arrDiscoverResponse.get(0);

			String digitalId = StringUtil
					.toUtf8String(StringUtil.base64UrlDecode(discoveryInfoNode.get("digitalId").asText()));

			validationResultDto = validateDeviceCheck(objectMapper.readValue(digitalId, ObjectNode.class),
					discoveryInfoNode.get("deviceCode").asText(),
					discoveryInfoNode.get("serviceVersion").asText(),
					discoveryInfoNode.get("purpose").asText());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"ValidDeviceCheckValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateDeviceInfoDeviceCheck(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDeviceInfoResponse = (ArrayNode) objectMapper.readValue(responseDto.getMethodResponse(),
					ArrayNode.class);

			for (int deviceIndex = 0; deviceIndex < arrDeviceInfoResponse.size(); deviceIndex++) {
				ObjectNode deviceInfoNode = (ObjectNode) arrDeviceInfoResponse.get(deviceIndex);
				if (isUnSignedDeviceInfo(deviceInfoNode)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Registered Device Failed");
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

	private ValidationResultDto validateSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String deviceInfo = objectNode.get("deviceInfo").asText();
			List<String> arrJwtInfo = new ArrayList<String>();
			validationResultDto = validateSignatureValidity(deviceInfo, arrJwtInfo);
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				ObjectNode deviceInfoDto = objectMapper.readValue(arrJwtInfo.get(INFO_INDEX_PAYLOAD), ObjectNode.class);

				if (Objects.isNull(deviceInfoDto)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Device info Decoded value is null");
				} else {
					validationResultDto = validateSignedDigitalId(deviceInfoDto.get("digitalId").asText(),
							deviceInfoDto.get("deviceCode").asText(),
							deviceInfoDto.get("serviceVersion").asText(),
							deviceInfoDto.get("purpose").asText());
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"ValidDeviceCheckValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateCaptureDeviceCheck(ValidationInputDto responseDto) {
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

						validationResultDto = validateSignedDigitalId(biometricDataNode.get("digitalId").asText(),
								biometricDataNode.get("deviceCode").asText(),
								biometricDataNode.get("deviceServiceVersion").asText(),
								biometricDataNode.get("purpose").asText());
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"ValidDeviceCheckValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateRCaptureDeviceCheck(ValidationInputDto responseDto) {
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

						validationResultDto = validateSignedDigitalId(biometricDataNode.get("digitalId").asText(),
								biometricDataNode.get("deviceCode").asText(),
								biometricDataNode.get("deviceServiceVersion").asText(),
								biometricDataNode.get("purpose").asText());
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"ValidDeviceCheckValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateSignedDigitalId(String digitalId, String deviceCode,
			String deviceServiceVersion, String purpose) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			List<String> arrJwtInfo = new ArrayList<String>();

			validationResultDto = validateSignatureValidity(digitalId, arrJwtInfo);
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS))
				validationResultDto = validateDeviceCheck(
						objectMapper.readValue(arrJwtInfo.get(INFO_INDEX_PAYLOAD), ObjectNode.class), deviceCode,
						deviceServiceVersion, purpose);
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	public ValidationResultDto validateDeviceCheck(ObjectNode digitalIdNode, String deviceCode,
			String deviceServiceVersion, String purpose) throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();

		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		DeviceValidatorRequestDto deviceValidatorRequestDto = new DeviceValidatorRequestDto();
		DeviceValidatorDigitalIdDto digitalId = new DeviceValidatorDigitalIdDto();
		digitalId.setSerialNo(digitalIdNode.get("serialNo").asText());
		digitalId.setMake(digitalIdNode.get("make").asText());
		digitalId.setModel(digitalIdNode.get("model").asText());
		digitalId.setType(digitalIdNode.get("type").asText());
		digitalId.setDeviceSubType(digitalIdNode.get("deviceSubType").asText());
		digitalId.setDp(digitalIdNode.get("deviceProvider").asText());
		digitalId.setDpId(digitalIdNode.get("deviceProviderId").asText());
		digitalId.setDateTime(digitalIdNode.get("dateTime").asText());

		// TODO CHECK
		// need to check how to get deviceCode for Discovery, DeviceInfo, Capture,
		// RCapture in version 1.0.0 not present
		deviceValidatorRequestDto.setDeviceCode(deviceCode);
		deviceValidatorRequestDto.setDeviceServiceVersion(deviceServiceVersion);
		deviceValidatorRequestDto.setPurpose(purpose);
		deviceValidatorRequestDto.setDigitalId(digitalId);
		
		deviceValidatorDto.setRequest(deviceValidatorRequestDto);
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());

		io.restassured.response.Response postResponse = getPostResponse(getValidDeviceCheckUrl,
				deviceValidatorDto);

		try {
			DeviceValidatorResponseDto deviceValidatorResponseDto = objectMapper
					.readValue(postResponse.getBody().asString(), DeviceValidatorResponseDto.class);

			if ((deviceValidatorResponseDto.getErrors() != null && deviceValidatorResponseDto.getErrors().size() > 0)) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Registered Device Failed");
			} else {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Registered Device Success");
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"Exception in Trust root Validation - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}
}
