package io.mosip.compliance.toolkit.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.service.ReportGeneratorService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;

/**
 * This controller class defines the endpoints to generate a testrun report.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.0
 *
 */
@RestController
public class ReportGeneratorController {

	/** The Constant CREATE_REPORT_ID. */
	private static final String CREATE_REPORT_ID = "create.report.post";
	
	@Autowired
	private ReportGeneratorService service;

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
	
	@PostMapping(value = "/createDraftReport")
	public ResponseEntity<?> createDraftReport(@RequestBody @Valid RequestWrapper<ReportRequestDto> value,
			@RequestHeader String origin, Errors errors) throws Exception {
		requestValidator.validate(value, errors);
		requestValidator.validateId(CREATE_REPORT_ID, value.getId(), errors);
		DataValidationUtil.validate(errors, CREATE_REPORT_ID);
		return service.createDraftReport(value.getRequest(), origin);
	}
	
	@PreAuthorize("hasAnyRole(@authorizedRoles.getCreatePartnerReport())")
	@PostMapping(value = "/createPartnerReport/{partnerId}")
	public ResponseEntity<?> createPartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestDto,
			@RequestHeader String origin, Errors errors) throws Exception {
		requestValidator.validate(reportRequestDto, errors);
		requestValidator.validateId(CREATE_REPORT_ID, reportRequestDto.getId(), errors);
		DataValidationUtil.validate(errors, CREATE_REPORT_ID);
		return service.createPartnerReport(partnerId, reportRequestDto.getRequest(), origin);
	}
}