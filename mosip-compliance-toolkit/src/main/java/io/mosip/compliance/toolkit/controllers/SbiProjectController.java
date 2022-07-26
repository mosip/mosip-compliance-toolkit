package io.mosip.compliance.toolkit.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.sbi.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.sbi.SbiProjectResponseDto;
import io.mosip.compliance.toolkit.service.SbiProjectService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * This Controller class get/adds/save/delete from SbiProjects
 * 
 * @author Janardhan B S
 * @since 1.0.0
 *
 */
@RestController
public class SbiProjectController {

	@Autowired
	private SbiProjectService sbiProjectService;
	
	@GetMapping(value = "/getSbiProject")
	private ResponseWrapper<SbiProjectDto> getProjectById(@RequestParam(required = true) String id){
		return null;
	}

	/**
	* Add Sbi Project details.
	*
	* @param SbiProjectDto
	* @return list SbiProjectDto added
	*/
	@ResponseFilter
	@PostMapping(value = "/addSbiProject", produces = "application/json")
	public ResponseWrapper<SbiProjectResponseDto> addSbiProject(
			@RequestBody @Valid RequestWrapper<SbiProjectDto> value) throws Exception {
		return sbiProjectService.addSbiProject(value.getRequest());
	}
	
	/**
	* Save Sbi Project details.
	*
	* @param SbiProjectDto
	* @return list SbiProjectDto saved
	*/
	@ResponseFilter
	@PostMapping(value = "/saveSbiProject", produces = "application/json")
	public ResponseWrapper<SbiProjectResponseDto> saveSbiProject(
			@RequestBody @Valid RequestWrapper<SbiProjectDto> value) throws Exception {
		return sbiProjectService.saveSbiProject(value.getRequest());
	}
}