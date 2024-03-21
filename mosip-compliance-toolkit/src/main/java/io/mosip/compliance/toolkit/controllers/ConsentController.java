package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.CustomTemplateDto;
import io.mosip.compliance.toolkit.dto.PartnerProfileDto;
import io.mosip.compliance.toolkit.service.ConsentService;
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
@Tag(name = "consent-controller")
public class ConsentController {

    /**
     * The Constant BIOMETRIC_CONSENT_POST_ID application.
     */
    private static final String BIOMETRIC_CONSENT_POST_ID = "biometric.consent.post";

    @Autowired
    ConsentService consentService;

    @GetMapping(value = "/getConsentTemplate")
    @Operation(summary = "Get biometric consent template", description = "Fetch biometric consent template", tags = "consent-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<CustomTemplateDto> getConsentTemplate(@RequestParam String langCode, @RequestParam String templateName)  throws Exception {
        return consentService.getConsentTemplate(langCode, templateName);
    }

    @PostMapping(value = "/savePartnerConsent")
    @Operation(summary = "save partner consent", description = "Store the partner's consent in the database.", tags = "consent-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<PartnerProfileDto> savePartnerConsent() throws Exception {
        return consentService.setConsent();
    }

    @GetMapping(value = "/getPartnerConsent")
    @Operation(summary = "Retrieve the partner's biometric consent status.", description = "Retrieve the partner's biometric consent status.", tags = "consent-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<Boolean> getPartnerConsent(@RequestParam String templateName) {
        return consentService.isConsentGiven(templateName);
    }
}