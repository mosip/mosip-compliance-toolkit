package io.mosip.compliance.toolkit.controllers;

import java.util.HashMap;
import java.util.Map;

import io.mosip.compliance.toolkit.constants.PartnerTypes;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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

	@Value("${mosip.toolkit.languages.rtl}")
	private String rtlLanguages;
	
	@Value("${mosip.service.datashare.incorrect.partner.id}")
	private String incorrectPartnerId;

	@Value("${mosip.service.abis.partner.type}")
	private String abisPartnerType;

	@Value("${mosip.toolkit.default.collection.name}")
	private String complianceCollectionName;
	
	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private boolean isAbisPartner() {
		boolean flag = false ;
		String authorities = authUserDetails().getAuthorities().toString();
		String partnerType = PartnerTypes.ABIS.getCode();
		if (authorities.contains(partnerType)) {
			flag = true;
		}
		return flag;
	}
	
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
		configMap.put("rtlLanguages", rtlLanguages);
		configMap.put("incorrectPartnerId", incorrectPartnerId);
		configMap.put("complianceCollectionName", complianceCollectionName);
		if (isAbisPartner()) {
			configMap.put("abisPartnerType", abisPartnerType);
		} else {
			configMap.put("abisPartnerType", "");
		}
		responseWrapper.setResponse(configMap);
		return responseWrapper;
	}

}
