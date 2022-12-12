package io.mosip.compliance.toolkit.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class MainController {

	@Value("${mosip.toolkit.sbi.ports}")
	private String sbiPorts;
	
	@Value("${mosip.toolkit.documentupload.allowed.file.type}")
	private String allowedFileTypes;

	@Value("${mosip.toolkit.documentupload.allowed.file.nameLength}")
	private String allowedFileNameLegth;

	@Value("${mosip.toolkit.documentupload.allowed.file.size}")
	private String allowedFileSize;

	@Value("${mosip.toolkit.sbi.timeout}")
	private String sbiTimeout;

	@Value("${mosip.toolkit.sbi.keyrotation.iterations}")
	private String keyRotationIterations;
	
	@ResponseFilter
	@GetMapping("/configs")
	public ResponseWrapper<Map<String, String>> getConfigValues() {
		ResponseWrapper<Map<String, String>> responseWrapper = new ResponseWrapper<>();
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("sbiPorts", sbiPorts);
		configMap.put("sbiTimeout", sbiTimeout);
		configMap.put("allowedFileTypes", allowedFileTypes);
		configMap.put("allowedFileNameLegth", allowedFileNameLegth);
		configMap.put("allowedFileSize", allowedFileSize);
		configMap.put("keyRotationIterations", keyRotationIterations);
		responseWrapper.setResponse(configMap);
		return responseWrapper;
	}

}
