package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.service.AbisProjectService;

import javax.validation.Valid;

@RestController
public class AbisProjectController {

    /** The Constant SDK_PROJECT_POST_ID application. */
    private static final String ABIS_PROJECT_POST_ID = "abis.project.post";
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
    public ResponseWrapper<AbisProjectDto> addAbisProject(
            @RequestBody @Valid RequestWrapper<AbisProjectDto> value,
            Errors errors) throws Exception{

        requestValidator.validate(value, errors);
        requestValidator.validateId(ABIS_PROJECT_POST_ID, value.getId(), errors);
        DataValidationUtil.validate(errors, ABIS_PROJECT_POST_ID);
        return abisProjectService.addAbisProject(value.getRequest());
    }
   
}
