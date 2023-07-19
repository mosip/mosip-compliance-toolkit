package io.mosip.compliance.toolkit.validators;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class ExpectedFailureReasonValidator extends ToolkitValidator {

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);
			String expectedFailureReason = extraInfo.get("expectedFailureReason").asText();

			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ObjectNode.class);

			int failureReasonRecvd = Integer.parseInt(methodResponse.get("failureReason").asText());
			int failureReasonExpected = Integer.parseInt(expectedFailureReason);
			if (failureReasonRecvd == failureReasonExpected) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Expected failure reason: " + failureReasonExpected + " successfully validated.");
				validationResultDto.setDescriptionKey("EXPECTED_FAILURE_REASON_VALIDATOR_001"
						+ AppConstants.ARGUMENTS_DELIMITER
						+ failureReasonExpected);
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("The failure reason expected was: " + failureReasonExpected
						+ ", but received: " + failureReasonRecvd);
				validationResultDto.setDescriptionKey("EXPECTED_FAILURE_REASON_VALIDATOR_002"
						+ AppConstants.ARGUMENTS_DELIMITER
						+ failureReasonExpected
						+ AppConstants.ARGUMENTS_SEPARATOR
						+ failureReasonRecvd);
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
