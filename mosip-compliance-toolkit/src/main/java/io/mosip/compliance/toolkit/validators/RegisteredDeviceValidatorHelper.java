package io.mosip.compliance.toolkit.validators;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class RegisteredDeviceValidatorHelper extends ToolkitValidatorHelper {
	@Value("${ida.authmanager.url}")
	private String getAuthManagerUrl;

	@Value("${ida.validation.url}")
	private String getValidationUrl;

	public ValidationResultDto registerDeviceValidation(DeviceValidatorDto deviceValidatorDto) throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());

		io.restassured.response.Response postResponse = getPostResponse(getAuthManagerUrl,
				getValidationUrl, deviceValidatorDto);

		try {
			DeviceValidatorResponseDto deviceValidatorResponseDto = objectMapper.readValue(postResponse.getBody().asString(),
					DeviceValidatorResponseDto.class);

			if (deviceValidatorResponseDto.getErrors().size() > 0) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(postResponse.getBody().asString() + "Device Registration check failed");
			} else {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Registered Device Validation Success");
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("Exception Registered Device Validation failed " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}
}
