package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class BiometricTestDataController {

	@Autowired
	private BiometricTestDataService biometricTestDataService;

	@GetMapping(value = "/getBiometricTestData")
	public ResponseWrapper<List<BiometricTestDataDto>> getBiometricTestdata() {
		return biometricTestDataService.getBiometricTestdata();
	}

}
