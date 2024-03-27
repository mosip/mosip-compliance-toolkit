package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.service.TemplateService;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "template-controller")
public class TemplateController {

    @Autowired
    TemplateService templateService;

    @GetMapping(value = "/getTemplate")
    @Operation(summary = "Get template", description = "Fetch template", tags = "template-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<MasterTemplatesDto> getTemplate(@RequestParam String langCode, @RequestParam String templateName,
                                                           @RequestParam String version) {
        return templateService.getTemplate(langCode, templateName, version);
    }

    @GetMapping(value = "/getLatestTemplateVersion")
    @Operation(summary = "Get template", description = "Fetch template", tags = "template-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<String> getLatestTemplateVersion(@RequestParam String templateName) {
        return templateService.getLatestTemplateVersion(templateName);
    }

}