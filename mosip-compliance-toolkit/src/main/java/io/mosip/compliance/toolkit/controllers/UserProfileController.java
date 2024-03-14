package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.service.UserProfileService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Tag(name = "user-profile-controller")
public class UserProfileController {

    /**
     * The Constant BIOMETRIC_CONSENT_POST_ID application.
     */
    private static final String BIOMETRIC_CONSENT_POST_ID = "biometric.consent.post";

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    private RequestValidator requestValidator;

    @GetMapping(value = "/getBiometricsConsentTemplate")
    @Operation(summary = "Get biometric consent template", description = "Fetch biometric consent template", tags = "user-profile-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<String> getBiometricsConsentTemplate()  throws Exception {
        return userProfileService.getConsentTemplate();
    }

    @PostMapping(value = "/savePartnerBiometricConsent")
    @Operation(summary = "save partner biometric consent", description = "Store the partner's biometric consent in the database.", tags = "user-profile-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<PartnerConsentDto> savePartnerConsent(
            @RequestBody @Valid RequestWrapper<PartnerConsentDto> requestWrapper, Errors errors) throws Exception {
        requestValidator.validate(requestWrapper, errors);
        requestValidator.validateId(BIOMETRIC_CONSENT_POST_ID, requestWrapper.getId(), errors);
        DataValidationUtil.validate(errors, BIOMETRIC_CONSENT_POST_ID);
        return userProfileService.savePartnerConsent(requestWrapper.getRequest());
    }

    @GetMapping(value = "/getPartnerConsent")
    @Operation(summary = "Retrieve the partner's biometric consent status.", description = "Retrieve the partner's biometric consent status.", tags = "user-profile-controller")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<PartnerConsentDto> getPartnerConsent() {
        return userProfileService.getPartnerConsent();
    }
}