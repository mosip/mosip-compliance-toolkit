package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.abis.DataShareRequestDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareResponseWrapperDto;
import io.mosip.compliance.toolkit.service.ABISDataShareService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

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

	/**
	 * Initiates the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(requestValidator);
	}

	@PostMapping(value = "/getDataShareUrl")
	public ResponseWrapper<DataShareResponseWrapperDto> getDataShareUrl(
			@RequestBody RequestWrapper<DataShareRequestDto> requestWrapper, Errors errors) {
		requestValidator.validate(requestWrapper, errors);
		return abisDataShareService.getDataShareUrl(requestWrapper.getRequest());
	}

}
