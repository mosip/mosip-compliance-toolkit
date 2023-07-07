package io.mosip.compliance.toolkit.controllers;

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
	public ResponseWrapper<DataShareResponseWrapperDto> createDataShareUrl(
			@RequestBody RequestWrapper<DataShareRequestDto> requestWrapper, Errors errors) {
		requestValidator.validate(requestWrapper, errors);
		return abisDataShareService.createDataShareUrl(requestWrapper.getRequest());
	}

	@PostMapping(value = "/expireDataShareUrl")
	public ResponseWrapper<Boolean> expireDataShareUrl(
			@RequestBody RequestWrapper<DataShareExpireRequest> requestWrapper, Errors errors) {
		return abisDataShareService.expireDataShareUrl(requestWrapper.getRequest());
	}

	@PostMapping(value = "/saveDataShareToken")
	public ResponseWrapper<String> saveDataShareToken(
			@RequestBody RequestWrapper<DataShareSaveTokenRequest> requestWrapper, Errors errors) throws Exception {
		log.info("sessionId", "idType", "id", "In saveDataShareToken method of ABISDataShareController.");
		log.info("Recvd request {}", requestWrapper);
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(ABIS_DATASHARE_TOKEN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ABIS_DATASHARE_TOKEN_POST_ID);
		return abisDataShareService.saveDataShareToken(requestWrapper);
	}

	@PostMapping(value = "/invalidateDataShareToken")
	public ResponseWrapper<String> invalidateDataShareToken(
			@RequestBody RequestWrapper<DataShareSaveTokenRequest> requestWrapper, Errors errors) throws Exception {
		log.info("sessionId", "idType", "id", "In invalidateDataShareToken method of ABISDataShareController.");
		log.info("Recvd request {}", requestWrapper);
		requestValidator.validate(requestWrapper, errors);
		requestValidator.validateId(ABIS_DATASHARE_TOKEN_POST_ID, requestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ABIS_DATASHARE_TOKEN_POST_ID);
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse("OK");
		return responseWrapper;
	}

}
