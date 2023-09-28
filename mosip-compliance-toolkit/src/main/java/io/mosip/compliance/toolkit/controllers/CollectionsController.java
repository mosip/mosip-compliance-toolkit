package io.mosip.compliance.toolkit.controllers;

import java.util.List;

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
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionsResponseDto;
import io.mosip.compliance.toolkit.service.CollectionsService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class CollectionsController {

	/** The Constant SBI_PROJECT_POST_ID application. */
	private static final String COLLECTION_POST_ID = "collection.post";

	/** The Constant COLLECTION_TESTCASE_POST_ID application. */
	private static final String COLLECTION_TESTCASE_POST_ID = "collection.testcase.post";

	@Value("${mosip.toolkit.quality.assessment.collection}")
	private String qualityAssessmentCollection;

	@Autowired
	private CollectionsService collectionsService;

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

	@GetMapping(value = "/getCollections")
	public ResponseWrapper<CollectionsResponseDto> getProjectCollections(
			@RequestParam(required = true) String projectId, @RequestParam(required = true) String type) {
		return collectionsService.getCollections(type, projectId);
	}

	@GetMapping(value = "/getTestCasesForCollection/{id}")
	public ResponseWrapper<CollectionTestCasesResponseDto> getTestCasesForCollection(@PathVariable String id) {
		return collectionsService.getTestCasesForCollection(id);
	}

	@GetMapping(value = "/getCollection/{id}")
	public ResponseWrapper<CollectionDto> getCollection(@PathVariable String id) {
		return collectionsService.getCollectionById(id);
	}

	@PostMapping(value = "/addCollection")
	public ResponseWrapper<CollectionDto> addCollection(
			@RequestBody RequestWrapper<CollectionRequestDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_POST_ID);
		return collectionsService.addCollection(requestWrapper.getRequest(), AppConstants.BLANK);
	}

	@PostMapping(value = "/addQualityAssessmentCollection")
	public ResponseWrapper<List<CollectionDto>> addQualityAssessmentCollection(
			@RequestBody RequestWrapper<CollectionRequestDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_POST_ID);
		return collectionsService.addQualityAssessmentCollection(requestWrapper.getRequest(), qualityAssessmentCollection);
	}

	@PostMapping(value = "/addTestCasesForCollection")
	public ResponseWrapper<List<CollectionTestCaseDto>> addTestCasesForCollection(
			@RequestBody RequestWrapper<List<CollectionTestCaseDto>> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_TESTCASE_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_TESTCASE_POST_ID);
		return collectionsService.addTestCasesForCollection(requestWrapper.getRequest());
	}
}
