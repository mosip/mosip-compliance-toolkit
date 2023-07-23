package io.mosip.compliance.toolkit.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.service.ReportGeneratorService;

/**
 * This controller class defines the endpoints to generate a testrun report.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.0
 *
 */
@RestController
public class ReportGeneratorController {

	@Autowired
	private ReportGeneratorService service;
	
	@GetMapping(value = "/createReport/{testRunId}")
	public ResponseEntity<Resource> createReport(@PathVariable String testRunId, @RequestHeader String origin) {
		return service.createReport(testRunId, origin);
	}
}