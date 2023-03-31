package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.abis.QueueRequest;
import io.mosip.compliance.toolkit.service.ABISQueueService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * This controller class defines the endpoints for all ABIS queue.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@RestController
public class ABISQueueController {

	@Autowired
	private ABISQueueService abisQueueService;

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

	@PostMapping(value = "/sendToQueue")
	public ResponseWrapper<Boolean> sendToQueue(@RequestBody RequestWrapper<QueueRequest> requestWrapper, Errors errors) {
		requestValidator.validate(requestWrapper, errors);
		return abisQueueService.sendToQueue(requestWrapper.getRequest());
	}
	
	@GetMapping(value = "/readFromQueue")
	public ResponseWrapper<List<String>> readFromQueue() {
		return abisQueueService.readFromQueue();
	}

}
