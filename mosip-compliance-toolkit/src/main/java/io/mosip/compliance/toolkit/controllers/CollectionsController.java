package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "collections-controller")
public class CollectionsController {

	/** The Constant SBI_PROJECT_POST_ID application. */
	private static final String COLLECTION_POST_ID = "collection.post";

	/** The Constant COLLECTION_TESTCASE_POST_ID application. */
	private static final String COLLECTION_TESTCASE_POST_ID = "collection.testcase.post";

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
	@Operation(summary = "Get collections", description = "Get collections based on the project id and project type", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<CollectionsResponseDto> getProjectCollections(
			@RequestParam(required = true) String projectId, @RequestParam(required = true) String type) {
		return collectionsService.getCollections(type, projectId);
	}

	@GetMapping(value = "/getTestCasesForCollection/{id}")
	@Operation(summary = "Get testcases for collection", description = "Get testcases for collection by id", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<CollectionTestCasesResponseDto> getTestCasesForCollection(@PathVariable String id) {
		return collectionsService.getTestCasesForCollection(collectionsService.getPartnerId(), id);
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@GetMapping(value = "/getPartnerTestCasesForCollection/{partnerId}/{id}")
	@Operation(summary = "Get partner testcases for collection", description = "Get partner testcases for collection by id and partner id", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<CollectionTestCasesResponseDto> getPartnerTestCasesForCollection(@PathVariable String partnerId, 
			@PathVariable String id) {
		return collectionsService.getTestCasesForCollection(partnerId, id);
	}

	@GetMapping(value = "/getCollection/{id}")
	@Operation(summary = "Get collection", description = "Get collection by id", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<CollectionDto> getCollection(@PathVariable String id) {
		return collectionsService.getCollectionById(id, collectionsService.getPartnerId());
	}
	
	@PostMapping(value = "/addCollection")
	@Operation(summary = "Add collection", description = "Add new collection", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<CollectionDto> addCollection(
			@RequestBody RequestWrapper<CollectionRequestDto> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_POST_ID);
		return collectionsService.addCollection(requestWrapper.getRequest());
	}

	@PostMapping(value = "/addTestCasesForCollection")
	@Operation(summary = "Add testcases for collection", description = "Add selected testcases for collection", tags = "collections-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<CollectionTestCaseDto>> addTestCasesForCollection(
			@RequestBody RequestWrapper<List<CollectionTestCaseDto>> requestWrapper, Errors errors) throws Exception {
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(COLLECTION_TESTCASE_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, COLLECTION_TESTCASE_POST_ID);
		return collectionsService.addTestCasesForCollection(requestWrapper.getRequest());
	}
}
