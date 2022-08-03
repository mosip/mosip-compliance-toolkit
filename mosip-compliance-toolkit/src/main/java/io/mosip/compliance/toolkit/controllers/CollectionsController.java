package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.CollectionDto;
import io.mosip.compliance.toolkit.dto.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.CollectionTestcasesResponseDto;
import io.mosip.compliance.toolkit.dto.CollectionsResponseDto;
import io.mosip.compliance.toolkit.service.CollectionsService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class CollectionsController {

	/** The Constant SBI_PROJECT_POST_ID application. */
	private static final String COLLECTION_PROJECT_POST_ID = "collection.post";

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

	@GetMapping(value = "/getTestcasesForCollection/{id}")
	public ResponseWrapper<CollectionTestcasesResponseDto> getTestcasesForCollection(@PathVariable String id) {
		return collectionsService.getTestcasesForCollection(id);
	}

	@GetMapping(value = "/getCollection/{id}")
	public ResponseWrapper<CollectionDto> getCollection(@PathVariable String id) {
		return collectionsService.getCollectionById(id);
	}

	@PostMapping(value = "/saveCollection")
	public ResponseWrapper<CollectionDto> saveCollection(
			@RequestBody RequestWrapper<CollectionRequestDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_PROJECT_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_PROJECT_POST_ID);
		return collectionsService.saveCollection(requestWrapper.getRequest());
	}
}
