package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import io.mosip.compliance.toolkit.service.TestRunService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
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
	public ResponseWrapper<TestRunDto> addTestrun(@RequestBody RequestWrapper<TestRunDto> requestWrapper, Errors errors)
			throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_POST_ID);
		return testRunService.addTestRun(requestWrapper.getRequest());
	}

	@PutMapping(value = "/updateTestRun")
	public ResponseWrapper<TestRunDto> updateTestrun(@RequestBody RequestWrapper<TestRunDto> requestWrapper,
			Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_PUT_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_PUT_ID);
		return testRunService.updateTestRunExecutiionTime(requestWrapper.getRequest());
	}

	@PostMapping(value = "/addTestRunDetails")
	public ResponseWrapper<TestRunDetailsDto> addTestrunDetails(
			@RequestBody RequestWrapper<TestRunDetailsDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(TEST_RUN_DETAILS_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, TEST_RUN_DETAILS_POST_ID);
		return testRunService.addTestRunDetails(requestWrapper.getRequest());
	}

	@GetMapping(value = "/getTestRunDetails/{runId}")
	public ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(@PathVariable String runId) {
		return testRunService.getTestRunDetails(runId);
	}
	
	@GetMapping(value = "/getTestRunHistory")
	public ResponseWrapper<List<TestRunHistoryDto>> getTestRunHistory(@RequestParam(required = true) String collectionId){
		return testRunService.getTestRunHistory(collectionId);
	}
}
