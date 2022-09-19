package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.compliance.toolkit.service.ResourceManagementService;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class ResourceManagementController {

	@Autowired
	private ResourceManagementService resourceMgmtService;

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadResource())")
	@PostMapping(value = "/uploadResourceFile")
	public ResponseWrapper<Boolean> uploadResourceFile(@RequestParam(required = true) String type,
			@RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadResourceFile(type, file);
	}
}
