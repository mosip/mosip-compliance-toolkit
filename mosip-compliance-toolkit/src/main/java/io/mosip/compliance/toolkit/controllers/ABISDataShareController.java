package io.mosip.compliance.toolkit.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.dto.abis.DataShareExpireRequest;
import io.mosip.compliance.toolkit.dto.abis.DataShareRequestDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareResponseWrapperDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareSaveTokenRequest;
import io.mosip.compliance.toolkit.service.ABISDataShareService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * This controller class defines the endpoints for all ABIS datashare.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@RestController
@Tag(name = "abis-data-share-controller")
public class ABISDataShareController {

	@Autowired
	private ABISDataShareService abisDataShareService;

	@Autowired
	private RequestValidator requestValidator;

	/** The Constant ABIS_DATASHARE_TOKEN_POST_ID */
	private static final String ABIS_DATASHARE_TOKEN_POST_ID = "abis.datashare.token.post";

	private Logger log = LoggerConfiguration.logConfig(ABISDataShareController.class);

	/**
	 * Initiates the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@PostMapping(value = "/createDataShareUrl")
	@Operation(summary = "Create data share Url", description = "Create data share Url", tags = "abis-data-share-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<DataShareResponseWrapperDto> createDataShareUrl(
			@RequestBody RequestWrapper<DataShareRequestDto> requestWrapper, Errors errors) {
		requestValidator.validate(requestWrapper, errors);
		return abisDataShareService.createDataShareUrl(requestWrapper.getRequest());
	}

	@PostMapping(value = "/expireDataShareUrl")
	@Operation(summary = "Expire data share Url", description = "Expire data share Url", tags = "abis-data-share-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<Boolean> expireDataShareUrl(
			@RequestBody RequestWrapper<DataShareExpireRequest> requestWrapper, Errors errors) {
		return abisDataShareService.expireDataShareUrl(requestWrapper.getRequest());
	}

	@PostMapping(value = "/saveDataShareToken")
	@Operation(summary = "Save data share token", description = "Save data share token", tags = "abis-data-share-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<String> saveDataShareToken(
			@RequestBody RequestWrapper<DataShareSaveTokenRequest> requestWrapper, Errors errors) throws Exception {
		log.info("sessionId", "idType", "id", "In saveDataShareToken method of ABISDataShareController.");
		log.info("sessionId", "idType", "id", "Recvd request {}", requestWrapper);
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(ABIS_DATASHARE_TOKEN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ABIS_DATASHARE_TOKEN_POST_ID);
		return abisDataShareService.saveDataShareToken(requestWrapper);
	}

	@PostMapping(value = "/invalidateDataShareToken")
	@Operation(summary = "Invalidate data share token", description = "Invalidate data share token", tags = "abis-data-share-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<String> invalidateDataShareToken(
			@RequestBody RequestWrapper<DataShareSaveTokenRequest> requestWrapper, Errors errors) throws Exception {
		log.info("sessionId", "idType", "id", "In invalidateDataShareToken method of ABISDataShareController.");
		log.info("sessionId", "idType", "id", "Recvd request {}", requestWrapper);
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(ABIS_DATASHARE_TOKEN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ABIS_DATASHARE_TOKEN_POST_ID);
		return abisDataShareService.invalidateDataShareToken(requestWrapper);
	}

}
