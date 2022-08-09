package io.mosip.compliance.toolkit.validators;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;

@Component
public class TrustValidatorHelper extends ToolkitValidatorHelper {
	@Value("${ida.authmanager.url}")
	private String getAuthManagerUrl;

	@Value("${keymanager.verifyCertificateTrust.url}")
	private String getKeyManagerVerifyCertificateTrustUrl;

	public ValidationResultDto trustRootValidation(String certificateData, String certification) throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		DeviceTrustRequestDto trustRequest = new DeviceTrustRequestDto();

		trustRequest.setCertificateData(getCertificateData(certificateData));
		trustRequest.setPartnerDomain(certification);
		deviceValidatorDto.setRequest(trustRequest);

		io.restassured.response.Response postResponse = getPostResponse(getAuthManagerUrl,
				getKeyManagerVerifyCertificateTrustUrl, deviceValidatorDto);

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
