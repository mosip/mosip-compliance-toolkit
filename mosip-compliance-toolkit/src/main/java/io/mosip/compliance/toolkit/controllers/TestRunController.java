package io.mosip.compliance.toolkit.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunStatusDto;
import io.mosip.compliance.toolkit.service.TestRunService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
@Tag(name = "test-run-controller")
public class TestRunController {

	/** The Constant TEST_RUN_POST_ID application. */
	private static final String TEST_RUN_POST_ID = "testrun.post";

	/** The Constant TEST_RUN_PUT_ID application. */
	private static final String TEST_RUN_PUT_ID = "testrun.put";

	/** The Constant TEST_RUN_DETAILS_POST_ID application. */
	private static final String TEST_RUN_DETAILS_POST_ID = "testrun.details.post";

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private TestRunService testRunService;

	@PostMapping(value = "/addTestRun")
	@Operation(summary = "Add test run", description = "Add test run", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDto> addTestrun(@RequestBody RequestWrapper<TestRunDto> requestWrapper, Errors errors)
			throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_POST_ID);
		return testRunService.addTestRun(requestWrapper.getRequest());
	}

	@PutMapping(value = "/updateTestRun")
	@Operation(summary = "Update test run", description = "Update test run", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDto> updateTestrun(@RequestBody RequestWrapper<TestRunDto> requestWrapper,
			Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_PUT_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_PUT_ID);
		return testRunService.updateTestRunExecutionTime(requestWrapper.getRequest());
	}

	@PostMapping(value = "/addTestRunDetails")
	@Operation(summary = "Add test run details", description = "Add test run details", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDetailsDto> addTestrunDetails(
			@RequestBody RequestWrapper<TestRunDetailsDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_DETAILS_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_DETAILS_POST_ID);
		return testRunService.addTestRunDetails(requestWrapper.getRequest());
	}

	@GetMapping(value = "/getTestRunDetails/{runId}")
	@Operation(summary = "Get test run details", description = "Get test run details by runId", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(@PathVariable String runId) {
		return testRunService.getTestRunDetails(testRunService.getPartnerId(), runId, false);
	}

	@GetMapping(value = "/getMethodDetails/{runId}/{testcaseId}/{methodId}")
	@Operation(summary = "Get method details", description = "Get method details based on the runId, testcaseId and methodId", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDetailsDto> getMethodDetails(@PathVariable String runId,
			@PathVariable String testcaseId, @PathVariable String methodId) {
		return testRunService.getMethodDetails(testRunService.getPartnerId(), runId, testcaseId, methodId);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@GetMapping(value = "/getPartnerTestRunDetails/{partnerId}/{runId}")
	@Operation(summary = "Get partner test run details", description = "Get partner test run details by partnerId and runId", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDetailsResponseDto> getPartnerTestRunDetails(@PathVariable String partnerId,
			@PathVariable String runId) {
		return testRunService.getTestRunDetails(partnerId, runId, false);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@GetMapping(value = "/getPartnerMethodDetails/{partnerId}/{runId}/{testcaseId}/{methodId}")
	@Operation(summary = "Get partner method details", description = "Get partner method details", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunDetailsDto> getPartnerMethodDetails(@PathVariable String partnerId,
			@PathVariable String runId, @PathVariable String testcaseId, @PathVariable String methodId) {
		return testRunService.getMethodDetails(partnerId, runId, testcaseId, methodId);
	}

	@GetMapping(value = "/getTestRunHistory")
	@Operation(summary = "Get test run history", description = "Get test run history for a collection", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<PageDto<TestRunHistoryDto>> getTestRunHistory(
			@RequestParam(required = true) String collectionId, @RequestParam(defaultValue = "0") int pageNo,
			@RequestParam(defaultValue = "10") int pageSize) {
		return testRunService.getTestRunHistory(collectionId, pageNo, pageSize);
	}

	@GetMapping(value = "/getTestRunStatus/{runId}")
	@Operation(summary = "Get test run status", description = "Get test run status by runId", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<TestRunStatusDto> getTestRunStatus(@PathVariable String runId) {
		return testRunService.getTestRunStatus(runId);
	}

	@DeleteMapping(value = "/deleteTestRun/{runId}")
	@Operation(summary = "Delete test run", description = "Delete testrun by runId", tags = "test-run-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<Boolean> deleteTestRun(@PathVariable String runId) {
		return testRunService.deleteTestRun(runId);
	}
}
