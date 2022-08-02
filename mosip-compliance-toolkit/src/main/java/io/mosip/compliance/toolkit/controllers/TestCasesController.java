package io.mosip.compliance.toolkit.controllers;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.config.TestCasesConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.RequestValidateDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * Controller for all testcases related end points.
 * 
 * @author Mayura D
 * @since 1.0.0
 */
@RestController
public class TestCasesController {

	@Value("${mosip.toolkit.api.id.testcase.project.get}")
	private String getTestCasesId;

	@Autowired
	private TestCasesConfig testCasesConfig;

	@Autowired
	TestCasesService service;

	@Autowired
	private ApplicationContext context;

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

//	@GetMapping(value = "/validateRequest")
//	public String doValidate() {
//		try {
//
//			File sourceJsonFile = ResourceUtils.getFile("classpath:testdata/testcase_sbi_sample.json");
//			// Read File Content
//			String sourceJson = new String(Files.readAllBytes(sourceJsonFile.toPath()));
//			// System.out.println(sourceJson);
//
//			File schemaJsonFile = ResourceUtils.getFile("classpath:schemas/testcase_schema.json");
//			// Read File Content
//			String testCaseSchemaJson = new String(Files.readAllBytes(schemaJsonFile.toPath()));
//			// System.out.println(schemaJson);
//
//			return service.validateSourceJson(sourceJson, testCaseSchemaJson);
//		} catch (Exception e) {
//			return e.getLocalizedMessage();
//		}
//	}

	@PostMapping(value = "/validateRequest")
	public ValidationResultDto validateRequest(@RequestBody(required = true) RequestValidateDto requestDto) {
		try {
			ValidationResultDto response = null;
			if (requestDto.getTestCaseType().equalsIgnoreCase(AppConstants.SBI)) {
				String sourceJson = requestDto.getMethodRequest();
				File schemaJsonFile = ResourceUtils
						.getFile("classpath:schemas/sbi/" + requestDto.getRequestSchema() + ".json");
				// Read File Content
				String testCaseSchemaJson = new String(Files.readAllBytes(schemaJsonFile.toPath()));
				// System.out.println(schemaJson);
				response = service.validateJsonWithSchema(sourceJson, testCaseSchemaJson);
			}
			return response;
		} catch (Exception e) {
			ValidationResultDto validationResponseDto = new ValidationResultDto();
			validationResponseDto.setStatus(AppConstants.FAILURE);
			validationResponseDto.setDescription(e.getLocalizedMessage());
			return validationResponseDto;
		}
	}

	@PostMapping(value = "/validateResponse")
	public ResponseWrapper<ValidationResponseDto> validateResponse(
			@RequestBody @Valid RequestWrapper<ValidationInputDto> input, Errors errors) throws Exception {
		return service.performValidations(input.getRequest()); 
	}

	@GetMapping(value = "/getTestCases")
	public String getTestCases(@RequestParam(required = true) String type) {
		try {
			File file = ResourceUtils.getFile("classpath:schemas/testcase_schema.json");
			// Read File Content
			String testCaseSchemaJson = new String(Files.readAllBytes(file.toPath()));
			// System.out.println(testCaseSchemaJson);
			List<TestCasesConfig.TestCaseConfig> testCases = new ArrayList<>();
			String testcaseType = null;
			if (AppConstants.SBI.equalsIgnoreCase(type)) {
				testCases = testCasesConfig.getSbiTestCases();
				testcaseType = AppConstants.SBI;
			}
			if (AppConstants.SDK.equalsIgnoreCase(type)) {
				testCases = testCasesConfig.getSdkTestCases();
				testcaseType = AppConstants.SDK;
			}
			if (AppConstants.ABIS.equalsIgnoreCase(type)) {
				testCases = testCasesConfig.getAbisTestCases();
				testcaseType = AppConstants.ABIS;
			}
			if (testCases.size() > 0) {
				return service.generateTestCaseFromConfig(testcaseType, testCaseSchemaJson, testCases).toString();
			} else {
				return "No test cases configured for this type!";
			}
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}

	@GetMapping(value = "/getSbiTestCases")
	public ResponseWrapper<List<TestCaseDto>> getSbiTestCases(@RequestParam(required = true) String specVersion,
			@RequestParam(required = true) String purpose, @RequestParam(required = true) String deviceType,
			@RequestParam(required = true) String deviceSubType) {
		try {
			return service.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType, getTestCaseSchemaJson());
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
	public String generateRequestForSDK(@RequestParam(required = true) String methodName,
			@RequestParam(required = true) String testcaseId, @RequestParam(required = true) List<String> modalities) {
		try {
			return service.generateRequestForSDKTestcase(methodName, testcaseId, modalities);
		} catch (Exception e) {
			return e.getLocalizedMessage();
		}
	}

	/**
	 * testcases json array.
	 *
	 * @param TestCaseRequestDto
	 * @return
	 */
	@ResponseFilter
	@PostMapping(value = "/saveTestCases", produces = "application/json")
	public ResponseWrapper<TestCaseResponseDto> saveTestCases(
			@RequestBody @Valid RequestWrapper<TestCaseRequestDto> testCaseRequestDto) throws Exception {
		return service.saveTestCases(testCaseRequestDto.getRequest().getTestCases(), getTestCaseSchemaJson());
	}

	private String getTestCaseSchemaJson() throws Exception {
		File file = ResourceUtils.getFile("classpath:schemas/testcase_schema.json");
		// Read File Content
		return new String(Files.readAllBytes(file.toPath()));
	}
}
