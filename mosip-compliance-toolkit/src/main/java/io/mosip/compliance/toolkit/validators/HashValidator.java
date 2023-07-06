package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.HashUtil;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class HashValidator extends SBIValidator {

	private Logger log = LoggerConfiguration.logConfig(HashValidator.class);

	@Autowired
	HashUtil hashUtil;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		boolean isHashValid = false;
		ValidationResultDto validationResultDto = new ValidationResultDto();

		try {

			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);
			JsonNode previousHashArr = extraInfo.get("previousHash");

			String responseJson = inputDto.getMethodResponse();

			ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(responseJson,
					ObjectNode.class);

			JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
			int index = 0;
			if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()
					&& !previousHashArr.isNull() && previousHashArr.isArray()
					&& arrBiometricNodes.size() == previousHashArr.size()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					String hashReceived = biometricNode.get("hash").asText();
					log.info("hashReceived {}", hashReceived);
					JsonNode dataNode = biometricNode.get(DECODED_DATA);
					String biovalue = dataNode.get(BIO_VALUE).asText();
					String previousHash = previousHashArr.get(index).asText();
					log.info("previousHash {}", previousHash);
					String generatedHash = hashUtil.generateHash(previousHash, biovalue);
					log.info("generatedHash {}", generatedHash);
					if (generatedHash != null && generatedHash.equals(hashReceived)) {
						isHashValid = true;
					} else {
						isHashValid = false;
					}
					index++;
				}
			}
			if (isHashValid) {
				validationResultDto.setDescription("Hash validation is successful");
				validationResultDto.setDescriptionKey("HASH_VALIDATOR_001");
				validationResultDto.setStatus(AppConstants.SUCCESS);
			} else {
				validationResultDto.setValidatorDescription("Hash validation is unsuccessful");
				validationResultDto.setDescriptionKey("HASH_VALIDATOR_002");
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
