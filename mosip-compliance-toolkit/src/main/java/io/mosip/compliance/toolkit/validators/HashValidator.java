package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.HashUtil;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class HashValidator extends ISOStandardsValidator {

	private Logger log = LoggerConfiguration.logConfig(HashValidator.class);

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		boolean isHashValid = false;
		ValidationResultDto validationResultDto = new ValidationResultDto();

		try {

			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);
			String previousHash = extraInfo.get("previousHash").asText();

			String responseJson = inputDto.getMethodResponse();

			ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(responseJson,
					ObjectNode.class);

			JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);

			String errHashReceived = null;
			String errGeneratedHashValue = null;
			if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					log.info("previousHash {}", previousHash);
					String hashReceivedInResponse = biometricNode.get("hash").asText();
					String bioValue = extractBioValue(biometricNode);
					byte[] decodedBioValue = CommonUtil.decodeURLSafeBase64(bioValue);
					String generatedHash = HashUtil.generateHash(previousHash, decodedBioValue);
					log.info("generatedHash {}", generatedHash);
					if (generatedHash != null && generatedHash.equals(hashReceivedInResponse)) {
						previousHash = generatedHash;
						isHashValid = true;
					} else {
						isHashValid = false;
						errHashReceived = hashReceivedInResponse;
						errGeneratedHashValue = generatedHash;
						break;
					}
				}
			}
			if (isHashValid) {
				validationResultDto.setDescription("Validation of hash chain is successful across multiple captures");
				validationResultDto.setDescriptionKey("HASH_VALIDATOR_001");
				validationResultDto.setStatus(AppConstants.SUCCESS);
			} else {
				validationResultDto.setDescription("Validation of hash chain failed across multiple captures."
						+ " Previous Hash for last request was {},"
						+ " hash generated  by validator is {} and hash received is {}");
				if ("".equals(previousHash)) {
					previousHash = "''";
				}
				validationResultDto.setDescriptionKey(
						"HASH_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + previousHash + AppConstants.ARGUMENTS_SEPARATOR + errGeneratedHashValue
										+ AppConstants.ARGUMENTS_SEPARATOR + errHashReceived);
				validationResultDto.setStatus(AppConstants.FAILURE);
			}
		} catch (Exception ex) {
			log.error("sessionId", "idType", "id", "In HashValidator - " + ex.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescriptionKey(
					"HASH_VALIDATOR_003" + AppConstants.ARGUMENTS_DELIMITER + ex.getLocalizedMessage());
			validationResultDto.setDescription("Hash validation failure due to: " + ex.getLocalizedMessage());
		}
		return validationResultDto;
	}

}
