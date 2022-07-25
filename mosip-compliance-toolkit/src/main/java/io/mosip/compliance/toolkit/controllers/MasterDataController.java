package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.MasterDataDto;
import io.mosip.compliance.toolkit.service.MasterDataService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class MasterDataController {
	
	@Autowired
	private MasterDataService masterDataService;
	
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

	@GetMapping(value = "/getMasterData") 
	public ResponseWrapper<MasterDataDto> getMasterDataByName(@RequestParam(required = true) String name){
		return masterDataService.getDataByName(name); 
	}

	@PostMapping(value = "/saveMasterData")
	public ResponseWrapper<MasterDataDto> addMasterData(
			@RequestBody(required = true) RequestWrapper<MasterDataDto> masterDataDtoRequest) {
		MasterDataDto masterDataDto = masterDataDtoRequest.getRequest();
		return masterDataService.addMasterData(masterDataDto);
	}
	
}
