package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.CollectionTestcaseDto;
import io.mosip.compliance.toolkit.service.CollectionTestcaseService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class CollectionTestcaseController {

	/** The Constant COLLECTION_TESTCASE_POST_ID application. */
	private static final String COLLECTION_TESTCASE_POST_ID = "collection.testcase.post";

	@Autowired
	private CollectionTestcaseService collectionTestcaseService;

	@Autowired
	private RequestValidator requestValidator;

	@PostMapping(value = "/saveCollectionTestcase")
	public ResponseWrapper<CollectionTestcaseDto> saveCollectionTestcase(
			@RequestBody RequestWrapper<CollectionTestcaseDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_TESTCASE_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_TESTCASE_POST_ID);
		return collectionTestcaseService.saveCollectionTestcaseMapping(requestWrapper.getRequest());
	}

}
