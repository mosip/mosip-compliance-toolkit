package io.mosip.compliance.toolkit.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
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

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@GetMapping(value = "/getListOfBiometricTestData")
	public ResponseWrapper<List<BiometricTestDataDto>> getListOfBiometricTestData() {
		return biometricTestDataService.getListOfBiometricTestData();
	}

	@PostMapping(value = "/addBiometricTestData", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	public ResponseWrapper<BiometricTestDataDto> addBiometricTestData(@RequestParam("file") MultipartFile file,
			@RequestPart("biometricMetaData") String strRequestWrapper, Errors errors) {
		try {
			RequestWrapper<BiometricTestDataDto> requestWrapper = objectMapperConfig.objectMapper()
					.readValue(strRequestWrapper, new TypeReference<RequestWrapper<BiometricTestDataDto>>() {
					});
			requestValidator.validate(requestWrapper, errors);
			requestValidator.validateId(BIOMETRIC_TESTDATA_POST_ID, requestWrapper.getId(), errors);
			DataValidationUtil.validate(errors, BIOMETRIC_TESTDATA_POST_ID);
			return biometricTestDataService.addBiometricTestdata(requestWrapper.getRequest(), file);
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

	@GetMapping(value = "/getBiometricTestDataFile/{id}")
	public ResponseEntity<Resource> getBiometricTestDataFile(@PathVariable String id) {
		return biometricTestDataService.getBiometricTestDataFile(id);
	}

	@GetMapping(value = "/getBioTestDataFileNames")
	public ResponseWrapper<List<String>> getBioTestDataFileNames(@RequestParam(required = true) String purpose) {
		return biometricTestDataService.getBioTestDataFileNames(purpose);
	}

	@GetMapping(value = "/getSampleBioTestDataFile")
	public ResponseEntity<Resource> getSampleBioTestDataFile(@RequestParam(required = true) String purpose) {
		return biometricTestDataService.getSampleBioTestDataFile(purpose);
	}

	@PostMapping(value = "/uploadSampleBioTestDataFile")
	public ResponseWrapper<Boolean> uploadSampleBioTestDataFile(@RequestParam String purpose,
			@RequestParam("file") MultipartFile file) {
		return biometricTestDataService.uploadSampleBioTestDataFile(purpose, file);
	}

}
