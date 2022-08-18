package io.mosip.compliance.toolkit.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidateRequestSchemaDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * Controller for all testcases related end points.
 * 
 * @author Mayura D
 * @since 1.0.0
 */
@RestController
public class TestCasesController {

	/** The Constant VALIDATIONS_POST_ID application. */
	private static final String VALIDATIONS_POST_ID = "validations.post";

	@Value("${mosip.toolkit.api.id.testcase.project.get}")
	private String getTestCasesId;

	@Autowired
	TestCasesService service;

	@Autowired
	private RequestValidator requestValidator;

	/**
	 * Initiates the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@PostMapping(value = "/validateRequest")
	public ResponseWrapper<ValidationResultDto> validateRequest(
			@RequestBody @Valid RequestWrapper<ValidateRequestSchemaDto> input, Errors errors) throws Exception {
		requestValidator.validateId(VALIDATIONS_POST_ID, input.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATIONS_POST_ID);
		return service.performRequestValidations(input.getRequest());
	}

	@PostMapping(value = "/validateResponse")
	public ResponseWrapper<ValidationResponseDto> validateResponse(
			@RequestBody @Valid RequestWrapper<ValidationInputDto> input, Errors errors) throws Exception {
		requestValidator.validateId(VALIDATIONS_POST_ID, input.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATIONS_POST_ID);
		return service.performValidations(input.getRequest());
	}

	@GetMapping(value = "/getSbiTestCases")
	public ResponseWrapper<List<TestCaseDto>> getSbiTestCases(@RequestParam(required = true) String specVersion,
			@RequestParam(required = true) String purpose, @RequestParam(required = true) String deviceType,
			@RequestParam(required = true) String deviceSubType) {
		try {
			return service.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType);
		} catch (Exception ex) {
			ResponseWrapper<List<TestCaseDto>> responseWrapper = new ResponseWrapper<>();
			responseWrapper.setId(getTestCasesId);
			responseWrapper.setResponse(null);
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorMessage() + " " + ex.getLocalizedMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
			responseWrapper.setVersion(AppConstants.VERSION);
			responseWrapper.setResponsetime(LocalDateTime.now());
			return responseWrapper;
		}
	}

	@GetMapping(value = "/generateRequestForSDK")
	public ResponseWrapper<String> generateRequestForSDK(@RequestParam(required = true) String methodName,
			@RequestParam(required = true) String testcaseId, @RequestParam(required = true) List<String> modalities)
			throws Exception {
		return service.generateRequestForSDKTestcase(methodName, testcaseId, modalities);
	}

	@PostMapping(value = "/saveTestCases", produces = "application/json")
	public ResponseWrapper<TestCaseResponseDto> saveTestCases(
			@RequestBody @Valid RequestWrapper<TestCaseRequestDto> testCaseRequestDto) throws Exception {
		return service.saveTestCases(testCaseRequestDto.getRequest().getTestCases());
	}

	@GetMapping(value = "/getTestCase/{testId}")
	public ResponseWrapper<TestCaseDto> getTestCaseById(@PathVariable String testId) {
		return service.getTestCaseById(testId);
	}
}
