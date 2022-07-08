package io.mosip.compliance.toolkit.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class MainController {

	@ResponseFilter
	@GetMapping("/configs")
	public ResponseWrapper<Map<String,String>> getConfigValues() {
		ResponseWrapper<Map<String,String>> responseWrapper = new ResponseWrapper<>();
		//responseWrapper.setResponse(applicationService.getConfigValues());
		return responseWrapper;
	}
	

}
