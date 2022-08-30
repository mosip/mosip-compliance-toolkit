package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.service.SdkProjectService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
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
    public ResponseWrapper<SdkProjectDto> updateSdkProject(
            @RequestBody @Valid RequestWrapper<SdkProjectDto> value,
            Errors errors) throws Exception {

        requestValidator.validate(value, errors);
        requestValidator.validateId(SDK_PROJECT_UPDATE_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, SDK_PROJECT_UPDATE_ID);
        return sdkProjectService.updateSdkProject(value.getRequest());
    }
}
