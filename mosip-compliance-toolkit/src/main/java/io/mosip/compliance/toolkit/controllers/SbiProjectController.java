package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.SbiProjectDto;
import io.mosip.compliance.toolkit.service.SbiProjectService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class SbiProjectController {
	
	@Autowired
	private SbiProjectService sbiProjectService;
	
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
	
	@GetMapping(value = "/getSbiProject/{id}")
	private ResponseWrapper<SbiProjectDto> getProjectById(@PathVariable String id){
		return sbiProjectService.getSbiProject(id);
	}

}
