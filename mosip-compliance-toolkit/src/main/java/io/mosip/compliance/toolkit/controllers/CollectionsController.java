package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.CollectionTestRunResponseDto;
import io.mosip.compliance.toolkit.dto.TestcasesCollectionResponseDto;
import io.mosip.compliance.toolkit.service.CollectionsService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class CollectionsController {

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

	@GetMapping(value = "/getProjectCollections")
	public ResponseWrapper<CollectionTestRunResponseDto> getSbiCollections(
			@RequestParam(required = true) String projectId, @RequestParam(required = true) String type) {
		return collectionsService.getProjectCollectionTestrun(type, projectId);
	}

	@GetMapping(value = "/getTestcasesByCollectionId/{id}")
	public ResponseWrapper<TestcasesCollectionResponseDto> getTestcasesByCollection(@PathVariable String id) {
		return collectionsService.getTestcasesByCollectionId(id);
	}
}
