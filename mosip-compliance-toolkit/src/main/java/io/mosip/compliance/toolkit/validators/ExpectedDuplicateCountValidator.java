package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class ExpectedDuplicateCountValidator extends ToolkitValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);

			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ObjectNode.class);

			JsonNode candidateList = (JsonNode) methodResponse.get("candidateList");

			int count = Integer.parseInt(candidateList.get("count").asText());
			int expectedDuplicateCount = Integer.parseInt(extraInfo.get("expectedDuplicateCount").asText());

			if (count == expectedDuplicateCount) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription(
						"Identify - found the expected duplicate count as: " + count + " for given the referenceId.");
				validationResultDto.setDescriptionKey("EXPECTED_DUPLICATE_COUNT_VALIDATOR_001"
						+ AppConstants.ARGUMENTS_DELIMITER
						+ count);
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(
						"Identify - did not find the expected duplicate count. Expected duplicate count is: "
								+ expectedDuplicateCount + ". But received duplicate count as: " + count);
				validationResultDto.setDescriptionKey("EXPECTED_DUPLICATE_COUNT_VALIDATOR_002"
						+ AppConstants.ARGUMENTS_DELIMITER
						+ expectedDuplicateCount
						+ AppConstants.ARGUMENTS_SEPARATOR
						+ count);
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}

}
