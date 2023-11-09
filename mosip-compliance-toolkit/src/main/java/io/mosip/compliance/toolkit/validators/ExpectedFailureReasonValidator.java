package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class ExpectedFailureReasonValidator extends ToolkitValidator {

	private Logger log = LoggerConfiguration.logConfig(ExpectedFailureReasonValidator.class);

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
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In ExpectedFailureReasonValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}

}
