package io.mosip.compliance.toolkit.controllers;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "resource-management-controller")
public class ResourceManagementController {

	@Autowired
	private ResourceManagementService resourceMgmtService;

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadResource())")
	@PostMapping(value = "/uploadResourceFile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "upload resource file", description = "type any of these [MOSIP_DEFAULT, SCHEMAS, SCHEMAS_SBI, SCHEMAS_SDK, SCHEMAS_ABIS] & version as per SBI/SDK/ABIS schema version", tags = "resource-management-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<Boolean> uploadResourceFile(@RequestParam(required = true) String type,
			@RequestParam(required = false) String version, @RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadResourceFile(type, version, file);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getUploadResource())")
	@PostMapping(value = "/uploadTemplate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "upload template", description = "upload template file", tags = "resource-management-controller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))
	})
	public ResponseWrapper<Boolean> uploadTemplate(
			@RequestParam String langCode,
			@RequestParam String templateName,
			@RequestParam("file") MultipartFile file) {
		return resourceMgmtService.uploadTemplate(langCode, templateName, file);
	}
}
