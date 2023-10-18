package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.compliance.toolkit.service.ResourceManagementService;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;

@RestController
public class ResourceManagementController {

	@Autowired
	private ResourceManagementService resourceMgmtService;

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadResource())")
	@PostMapping(value = "/uploadResourceFile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "upload resource file", description = "type any of these [MOSIP_DEFAULT, SCHEMAS, SCHEMAS_SBI, SCHEMAS_SDK, SCHEMAS_ABIS] & version as per SBI/SDK/ABIS schema version", tags = "ResourceManagementController")
	public ResponseWrapper<Boolean> uploadResourceFile(@RequestParam(required = true) String type,
			@RequestParam(required = false) String version, @RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadResourceFile(type, version, file);
	}
}
