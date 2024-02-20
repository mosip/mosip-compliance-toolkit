package io.mosip.compliance.toolkit.controllers;

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

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.service.AbisProjectService;

import javax.validation.Valid;

@RestController
@Tag(name = "abis-project-controller")
public class AbisProjectController {

    /** The Constant ABIS_PROJECT_POST_ID application. */
    private static final String ABIS_PROJECT_POST_ID = "abis.project.post";

    /** The Constant ABIS_PROJECT_UPDATE_ID application. */
    private static final String ABIS_PROJECT_UPDATE_ID = "abis.project.put";
    @Autowired
    private AbisProjectService abisProjectService;

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

    @GetMapping(value = "/getAbisProject/{id}")
    @Operation(summary = "Get ABIS project", description = "Get ABIS project by id", tags = "abis-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    private ResponseWrapper<AbisProjectDto> getProjectById(@PathVariable String id){
        return abisProjectService.getAbisProject(id);
    }

    /**
     * Post Abis Project details.
     *
     * @param AbisProjectDto
     * @return AbisProjectDto added
     * @throws Exception
     */
    @ResponseFilter
    @PostMapping(value = "/addAbisProject", produces = "application/json")
    @Operation(summary = "Add ABIS project", description = "Add new ABIS project", tags = "abis-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<AbisProjectDto> addAbisProject(
            @RequestBody @Valid RequestWrapper<AbisProjectDto> value,
            Errors errors) throws Exception{

        requestValidator.validate(value, errors);
        requestValidator.validateId(ABIS_PROJECT_POST_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, ABIS_PROJECT_POST_ID);
        return abisProjectService.addAbisProject(value.getRequest());
    }

    /**
     * Update Abis Project details.
     *
     * @param AbisProjectDto
     * @return AbisProjectDto added
     * @throws Exception
     */
    @ResponseFilter
    @PutMapping(value = "/updateAbisProject", produces = "application/json")
    @Operation(summary = "Update ABIS project", description = "Update ABIS project details", tags = "abis-project-controller")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
    public ResponseWrapper<AbisProjectDto> updateAbisProject(
            @RequestBody @Valid RequestWrapper<AbisProjectDto> value,
            Errors errors) throws Exception {

        requestValidator.validate(value, errors);
        requestValidator.validateId(ABIS_PROJECT_UPDATE_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, ABIS_PROJECT_UPDATE_ID);
        return abisProjectService.updateAbisProject(value.getRequest());
    }
}
