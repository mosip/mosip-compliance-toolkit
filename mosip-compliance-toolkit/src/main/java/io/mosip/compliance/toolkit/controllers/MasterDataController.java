package io.mosip.compliance.toolkit.controllers;

import java.util.Base64;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.MasterDataDto;
import io.mosip.compliance.toolkit.service.MasterDataService;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class MasterDataController {
	
	@Autowired
	private MasterDataService masterDataService;

	@GetMapping(value = "/masterdata") 
	public ResponseWrapper<MasterDataDto> getMasterDataByName(@RequestParam String name){
		return masterDataService.getDataByName(name); 
	}

	@PostMapping(value = "/masterdata")
	public ResponseWrapper<MasterDataDto> addMasterData(@RequestHeader Map<String, String> headers,@RequestBody MasterDataDto masterDataDto) {
		//String token = headers.get("cookie");
		//return masterDataService.addMasterData(getEmail(token.split("Authorization=")[1]), masterDataDto);
		return masterDataService.addMasterData("admin", masterDataDto);
	}
	
//	private String getEmail(String token) {
//		String[] jwtPart = token.split("\\.");
//		JSONObject payloadJsonObj = new JSONObject(new String(Base64.getUrlDecoder().decode(jwtPart[1])));
//		return payloadJsonObj.getString("email");
//	}
}
