package io.mosip.compliance.toolkit.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class BiometricTestDataController {

	/** The Constant BIOMETRIC_TESTDATA_POST_ID application. */
	private static final String BIOMETRIC_TESTDATA_POST_ID = "biometric.testdata.post";

	@Autowired
	private BiometricTestDataService biometricTestDataService;

	@Autowired
	private RequestValidator requestValidator;

	@GetMapping(value = "/getBiometricTestData")
	public ResponseWrapper<List<BiometricTestDataDto>> getBiometricTestdata() {
		return biometricTestDataService.getBiometricTestdata();
	}

	@PostMapping(value = "/addBiometricTestData")
	public ResponseWrapper<BiometricTestDataDto> addBiometricTestData(
			@RequestBody RequestWrapper<BiometricTestDataDto> requestWrapper, Errors errors) {
		try {
			requestValidator.validate(requestWrapper, errors);
			requestValidator.validateId(BIOMETRIC_TESTDATA_POST_ID, requestWrapper.getId(), errors);
			DataValidationUtil.validate(errors, BIOMETRIC_TESTDATA_POST_ID);
			return biometricTestDataService.addBiometricTestdata(requestWrapper.getRequest());
		} catch (Exception ex) {
			ResponseWrapper<BiometricTestDataDto> responseWrapper = new ResponseWrapper<>();
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
			return responseWrapper;
		}
	}

}
