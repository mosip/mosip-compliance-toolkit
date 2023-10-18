package io.mosip.compliance.toolkit.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.service.SbiProjectService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class SbiProjectController {
	
	/** The Constant SBI_PROJECT_POST_ID application. */
	private static final String SBI_PROJECT_POST_ID = "sbi.project.post";

	/** The Constant SBI_PROJECT_UPDATE_ID application. */
	private static final String SBI_PROJECT_UPDATE_ID = "sbi.project.put";
	
	@Autowired
	private SbiProjectService sbiProjectService;
	
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
	
	@GetMapping(value = "/getSbiProject/{id}")
	private ResponseWrapper<SbiProjectDto> getProjectById(@PathVariable String id){
		return sbiProjectService.getSbiProject(id);
	}

	/**
	* Post Sbi Project details.
	*
	* @param SbiProjectDto
	* @return SbiProjectDto added
	 * @throws Exception 
	*/
	@ResponseFilter
	@PostMapping(value = "/addSbiProject", produces = "application/json")
	public ResponseWrapper<SbiProjectDto> addSbiProject(
			@RequestBody @Valid RequestWrapper<SbiProjectDto> value,
			Errors errors) throws Exception{
		requestValidator.validate(value, errors);
		requestValidator.validateId(SBI_PROJECT_POST_ID, value.getId(), errors);
		DataValidationUtil.validate(errors, SBI_PROJECT_POST_ID);
		return sbiProjectService.addSbiProject(value.getRequest());
	}

	/**
	 * Update Sbi Project details.
	 *
	 * @param SbiProjectDto
	 * @return SbiProjectDto added
	 * @throws Exception
	 */
	@ResponseFilter
	@PutMapping(value = "/updateSbiProject", produces = "application/json")
	public ResponseWrapper<SbiProjectDto> updateSbiProject(
			@RequestBody @Valid RequestWrapper<SbiProjectDto> value,
			Errors errors) throws Exception {

		requestValidator.validate(value, errors);
		requestValidator.validateId(SBI_PROJECT_UPDATE_ID, value.getId(), errors);
		DataValidationUtil.validate(errors, SBI_PROJECT_UPDATE_ID);
		return sbiProjectService.updateSbiProject(value.getRequest());
	}
	
	/**
	 * Returns the encryption key to be used by SBI for L1 devices. 
	 * @return
	 */
	@GetMapping(value = "/getEncryptionKey")
	private ResponseWrapper<String> getEncryptionKey(){
		return sbiProjectService.getEncryptionKey();
	}
}
