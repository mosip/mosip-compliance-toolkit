package io.mosip.compliance.toolkit.controllers;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import io.mosip.compliance.toolkit.util.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
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
import io.mosip.compliance.toolkit.dto.GenerateSdkRequestResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.SdkRequestDto;
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
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * Controller for all testcases related end points.
 *
 * @author Mayura D
 * @since 1.0.0
 */
@RestController
@Tag(name = "test-cases-controller")
public class TestCasesController {

	/**
	 * The Constant VALIDATIONS_POST_ID application.
	 */
	private static final String VALIDATIONS_POST_ID = "validations.post";
	
	private static final String GENERATE_SDK_REQUEST_POST_ID = "generate.sdk.request.post";

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
	@Operation(summary = "Validate Request", description = "Validate methodRequest as per the specification", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ValidationResultDto> validateRequest(
			@RequestBody @Valid RequestWrapper<ValidateRequestSchemaDto> input, Errors errors) throws Exception {
		requestValidator.validateId(VALIDATIONS_POST_ID, input.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATIONS_POST_ID);
		return service.performRequestValidations(input.getRequest());
	}

	@PostMapping(value = "/validateResponse")
	@Operation(summary = "Validate Response", description = "Validate methodResponse as per the specification", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ValidationResponseDto> validateResponse(
			@RequestBody @Valid RequestWrapper<ValidationInputDto> input, Errors errors) throws Exception {
		requestValidator.validateId(VALIDATIONS_POST_ID, input.getId(), errors);
		DataValidationUtil.validate(errors, VALIDATIONS_POST_ID);
		return service.performValidations(input.getRequest());
	}

	@GetMapping(value = "/getSbiTestCases")
	@Operation(summary = "Get SBI testcases", description = "Get SBI testcases by using the required fields given below.", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<TestCaseDto>> getSbiTestCases(@RequestParam(required = true) String specVersion,
			@RequestParam(required = true) String purpose, @RequestParam(required = true) String deviceType,
			@RequestParam(required = true) String deviceSubType,
			@RequestParam(required = true) boolean isAndroid) {
		try {
			String isAndroidSbi = "no";
			if (isAndroid) {
				isAndroidSbi = "yes";
			}
			return service.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType, isAndroidSbi);
		} catch (Exception ex) {
			return handleFailureForGetTestcases(ex);
		}
	}

	@GetMapping(value = "/getSdkTestCases")
	@Operation(summary = "Get SDK testcases", description = "Get SDK testcases by using the required fields given below.", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<TestCaseDto>> getSdkTestCases(@RequestParam(required = true) String specVersion,
			@RequestParam(required = true) String sdkPurpose) {
		try {
			return service.getSdkTestCases(specVersion, sdkPurpose);
		} catch (Exception ex) {
			return handleFailureForGetTestcases(ex);
		}
	}

	private ResponseWrapper<List<TestCaseDto>> handleFailureForGetTestcases(Exception ex) {
		ResponseWrapper<List<TestCaseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setId(getTestCasesId);
		responseWrapper.setResponse(null);
		String errorCode = ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorCode();
		String errorMessage = ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorMessage() + " " + ex.getLocalizedMessage();
		responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
	
	@GetMapping(value = "/getAbisTestCases")
	@Operation(summary = "Get ABIS testcases", description = "Get ABIS testcases based on the spec version", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<TestCaseDto>> getAbisTestCases(@RequestParam(required = true) String abisSpecVersion) {
		try {
			return service.getAbisTestCases(abisSpecVersion);
		} catch (Exception ex) {
			return handleFailureForGetTestcases(ex);
		}
	}

	@PostMapping(value = "/generateRequestForSDK")
	@Operation(summary = "Generate request for SDK", description = "Generate request for SDK", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<GenerateSdkRequestResponseDto> generateRequestForSDK(
			@RequestBody @Valid RequestWrapper<SdkRequestDto> request, Errors errors) throws Exception {
		requestValidator.validateId(GENERATE_SDK_REQUEST_POST_ID, request.getId(), errors);
		DataValidationUtil.validate(errors, GENERATE_SDK_REQUEST_POST_ID);
		return service.generateRequestForSDKTestcase(request.getRequest());
	}
	
	@PostMapping(value = "/generateRequestForSDKFrmBirs")
	@Operation(summary = "Generate request for SDK From BIR's", description = "Generate request for SDK From BIR's", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<GenerateSdkRequestResponseDto> generateRequestForSDKFrmBirs(
			@RequestBody @Valid RequestWrapper<SdkRequestDto> request, Errors errors) throws Exception {
		requestValidator.validateId(GENERATE_SDK_REQUEST_POST_ID, request.getId(), errors);
		DataValidationUtil.validate(errors, GENERATE_SDK_REQUEST_POST_ID);
		return service.generateRequestForSDKFrmBirs(request.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getSaveTestCases())")
	@PostMapping(value = "/saveTestCases", produces = "application/json")
	@Operation(summary = "Save testcases", description = "The user must have the CTK_ADMIN role in order to save test cases.", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestCaseResponseDto> saveTestCases(
			@RequestBody @Valid RequestWrapper<TestCaseRequestDto> testCaseRequestDto) throws Exception {
		return service.saveTestCases(testCaseRequestDto.getRequest().getTestCases());
	}

	@GetMapping(value = "/getTestCase/{testId}")
	@Operation(summary = "Get testcase", description = "Get testcase by testId", tags = "test-cases-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestCaseDto> getTestCaseById(@PathVariable String testId) {
		return service.getTestCaseById(testId);
	}
}
