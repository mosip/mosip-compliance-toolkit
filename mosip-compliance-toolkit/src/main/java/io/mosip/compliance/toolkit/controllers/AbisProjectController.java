package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.service.AbisProjectService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class AbisProjectController {

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

   
}
