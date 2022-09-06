package io.mosip.compliance.toolkit.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class MainController {

	@Value("${sbi.ports}")
	private String sbiPorts;
	
	@Value("${compliance.documentupload.allowed.file.type}")
	private String allowedFileTypes;

	@Value("${compliance.documentupload.allowed.file.nameLength}")
	private String allowedFileNameLegth;

	@Value("${compliance.documentupload.allowed.file.size}")
	private String allowedFileSize;

	@ResponseFilter
	@GetMapping("/configs")
	public ResponseWrapper<Map<String, String>> getConfigValues() {
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("sbiPorts", sbiPorts);
		configMap.put("allowedFileTypes", allowedFileTypes);
		configMap.put("allowedFileNameLegth", allowedFileNameLegth);
		configMap.put("allowedFileSize", allowedFileSize);
		responseWrapper.setResponse(configMap);
		return responseWrapper;
	}

}
