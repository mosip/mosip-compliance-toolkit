package io.mosip.compliance.toolkit.controllers;

import java.util.HashMap;
import java.util.Map;

import io.mosip.compliance.toolkit.constants.PartnerTypes;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
@Tag(name = "main-controller")
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

	@Value("${mosip.toolkit.session.idle.timeout}")
	private String sessionIdleTimeout;

	@Value("${mosip.toolkit.session.idle.ping}")
	private String sessionIdlePing;

	@Value("${mosip.toolkit.session.idle.timer}")
	private String sessionIdleTimer;

	@Value("${mosip.toolkit.sbi.keyrotation.iterations}")
	private String keyRotationIterations;

	@Value("${mosip.toolkit.languages.rtl}")
	private String rtlLanguages;
	
	@Value("${mosip.service.datashare.incorrect.partner.id}")
	private String incorrectPartnerId;

	@Value("${mosip.service.abis.partner.type}")
	private String abisPartnerType;
	
	@Value("${mosip.toolkit.roles.adminPartnerReport}")
	private String adminPartnerReportRole;

	@Value("${mosip.toolkit.biometric.consent.enabled}")
	private String isConsentEnabled;
	
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
	@Operation(summary = "Get config", description = "Get configuration values", tags = "main-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
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
		configMap.put("adminPartnerReportRole", adminPartnerReportRole);
		configMap.put("sessionIdleTimeout", sessionIdleTimeout);
		configMap.put("sessionIdlePing", sessionIdlePing);
		configMap.put("sessionIdleTimer", sessionIdleTimer);
		configMap.put("isConsentEnabled", isConsentEnabled);
		if (isAbisPartner()) {
			configMap.put("isAbisPartner", "YES");
		} else {
			configMap.put("isAbisPartner", "NO");
		}
		responseWrapper.setResponse(configMap);
		return responseWrapper;
	}

}
