package io.mosip.compliance.toolkit.validators;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class KeyRotationValidator extends SignatureValidator {

	private Logger log = LoggerConfiguration.logConfig(KeyRotationValidator.class);
	
	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {

			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);
			ArrayNode beforeKeyRotationRespArr = (ArrayNode) extraInfo.get("beforeKeyRotationResp");
			ObjectNode beforeKeyRotationResp = null;
			if (beforeKeyRotationRespArr != null && beforeKeyRotationRespArr.size() > 0) {
				beforeKeyRotationResp = (ObjectNode) beforeKeyRotationRespArr.get(0);
			}
			if (beforeKeyRotationResp == null) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Device Info before key rotation is not available");
				return validationResultDto;
			}
			ArrayNode afterKeyRotationRespArr = (ArrayNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ArrayNode.class);
			ObjectNode afterKeyRotationResp = null;
			if (afterKeyRotationRespArr != null && afterKeyRotationRespArr.size() > 0) {
				afterKeyRotationResp = (ObjectNode) afterKeyRotationRespArr.get(0);
			}
			if (afterKeyRotationResp == null) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Device Info after key rotation is not available");
				return validationResultDto;
			}
			validationResultDto = performSignatureAndTrustValidations(afterKeyRotationResp);
			if (validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
				String err = validationResultDto.getDescription();
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto
						.setDescription("Signature validation failed for the Device Info after key rotation. " + err);
				return validationResultDto;
			}
			validationResultDto = compareCerificates(beforeKeyRotationResp, afterKeyRotationResp);
			if (validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
				return validationResultDto;
			}

			validationResultDto = compareMakeAndModel(beforeKeyRotationResp, afterKeyRotationResp);
			if (validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
				return validationResultDto;
			}
			validationResultDto.setStatus(AppConstants.SUCCESS);
			validationResultDto.setDescription("Key Rotation validations are successful.");
		} catch (ToolkitException e) {
			log.error("sessionId", "idType", "id", "In KeyRotationValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("sessionId", "idType", "id", "In KeyRotationValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto compareMakeAndModel(ObjectNode beforeKeyRotationResp, ObjectNode afterKeyRotationResp)
			throws JsonProcessingException, JsonMappingException {

		ValidationResultDto validationResultDto = new ValidationResultDto();
		ObjectNode deviceInfoDecoded = (ObjectNode)beforeKeyRotationResp.get(DEVICE_INFO_DECODED);
		ObjectNode digitalIdDecoded = (ObjectNode) deviceInfoDecoded.get(DIGITAL_ID_DECODED);
		String make = digitalIdDecoded.get(MAKE).asText();
		String model = digitalIdDecoded.get(MODEL).asText();
		String serialNo = digitalIdDecoded.get(SERIAL_NO).asText();
		
		ObjectNode deviceInfoDecoded1 = (ObjectNode)afterKeyRotationResp.get(DEVICE_INFO_DECODED);
		ObjectNode digitalIdDecoded1 = (ObjectNode) deviceInfoDecoded1.get(DIGITAL_ID_DECODED);
		String make1 = digitalIdDecoded1.get(MAKE).asText();
		String model1 = digitalIdDecoded1.get(MODEL).asText();
		String serialNo1 = digitalIdDecoded1.get(SERIAL_NO).asText();

		if (!(make.equals(make1) || model.equals(model1) || serialNo.equals(serialNo1))) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("Make, model or serialNo of the device info is not matching. ");
			return validationResultDto;
		}
		validationResultDto.setStatus(AppConstants.SUCCESS);
		return validationResultDto;
	}

	private ValidationResultDto performSignatureAndTrustValidations(ObjectNode afterKeyRotationResp) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		if (isDeviceInfoUnSigned(afterKeyRotationResp)) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("Device is not registered.");
		} else {
			validationResultDto = validateSignedDeviceInfo(afterKeyRotationResp);
		}
		return validationResultDto;
	}

	private ValidationResultDto compareCerificates(ObjectNode beforeKeyRotationResp, ObjectNode afterKeyRotationResp) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String beforeKRDeviceInfo = beforeKeyRotationResp.get(DEVICE_INFO).asText();
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(beforeKRDeviceInfo);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);

			String afterKRDeviceInfo = afterKeyRotationResp.get(DEVICE_INFO).asText();
			JsonWebSignature jws1 = new JsonWebSignature();
			jws1.setCompactSerialization(afterKRDeviceInfo);
			List<X509Certificate> certificateChainHeaderValue1 = jws1.getCertificateChainHeaderValue();
			X509Certificate certificate1 = certificateChainHeaderValue1.get(0);
			if (certificate.equals(certificate1)) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Both device keys are same. Rotate the device key and try again!");
				return validationResultDto;
			}
			Date certificateStartDt = certificate.getNotBefore();
			Date certificate1StartDt = certificate1.getNotBefore();

			if (certificate1StartDt.before(certificateStartDt)) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Second device key must be created after the first device key");
				return validationResultDto;
			}

			validationResultDto.setStatus(AppConstants.SUCCESS);
		} catch (JoseException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(" JoseException - " + "with Message - " + e.getLocalizedMessage());
		}

		return validationResultDto;
	}
}
