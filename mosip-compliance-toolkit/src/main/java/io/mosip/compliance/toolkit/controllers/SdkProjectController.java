package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.service.SdkProjectService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@Tag(name = "sdk-project-controller")
public class SdkProjectController {

    /** The Constant SDK_PROJECT_POST_ID application. */
    private static final String SDK_PROJECT_POST_ID = "sdk.project.post";

    /** The Constant SDK_PROJECT_UPDATE_ID application. */
    private static final String SDK_PROJECT_UPDATE_ID = "sdk.project.put";

    @Autowired
    private SdkProjectService sdkProjectService;

    @Autowired
    private RequestValidator requestValidator;

    /**
     * Initiates the binder.
     *
     * @param binder the binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(requestValidator);
    }

    @GetMapping(value = "/getSdkProject/{id}")
    @Operation(summary = "Get SDK project", description = "Get SDK project by id", tags = "sdk-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    private ResponseWrapper<SdkProjectDto> getProjectById(@PathVariable String id){
        return sdkProjectService.getSdkProject(id);
    }

    /**
     * Post Sdk Project details.
     *
     * @param SdkProjectDto
     * @return SdkProjectDto added
     * @throws Exception
     */
    @ResponseFilter
    @PostMapping(value = "/addSdkProject", produces = "application/json")
    @Operation(summary = "Add SDK project", description = "Add new SDK project", tags = "sdk-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<SdkProjectDto> addSdkProject(
            @RequestBody @Valid RequestWrapper<SdkProjectDto> value,
            Errors errors) throws Exception{

        requestValidator.validate(value, errors);
        requestValidator.validateId(SDK_PROJECT_POST_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, SDK_PROJECT_POST_ID);
        return sdkProjectService.addSdkProject(value.getRequest());
    }

    /**
     * Update Sdk Project details.
     *
     * @param SdkProjectDto
     * @return SdkProjectDto added
     * @throws Exception
     */
    @ResponseFilter
    @PutMapping(value = "/updateSdkProject", produces = "application/json")
    @Operation(summary = "Update SDK project", description = "Update SDK project details", tags = "sdk-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<SdkProjectDto> updateSdkProject(
            @RequestBody @Valid RequestWrapper<SdkProjectDto> value,
            Errors errors) throws Exception {

        requestValidator.validate(value, errors);
        requestValidator.validateId(SDK_PROJECT_UPDATE_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, SDK_PROJECT_UPDATE_ID);
        return sdkProjectService.updateSdkProject(value.getRequest());
    }
}
