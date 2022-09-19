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

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadschema())")
	@PostMapping(value = "uploadSchema")
	public ResponseWrapper<Boolean> uploadSchema(@RequestParam(required = true) String projectType,
			@RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadSchema(projectType, file);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadsamplebiotestdata())")
	@PostMapping(value = "/uploadSampleBioTestDataFile")
	public ResponseWrapper<Boolean> uploadSampleBioTestDataFile(@RequestParam(required = true) String purpose,
			@RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadSampleBioTestDataFile(purpose, file);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploaddefaultbiotestdata())")
	@PostMapping(value = "/uploadDefaultBioTestDataFile")
	public ResponseWrapper<Boolean> uploadDefaultBioTestDataFile(@RequestParam(required = true) String purpose,
			@RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadDefaultBioTestDataFile(purpose, file);
	}
}
