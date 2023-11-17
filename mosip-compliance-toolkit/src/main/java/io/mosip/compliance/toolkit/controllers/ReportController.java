package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.report.ComplianceTestRunSummaryDto;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * This controller class defines the endpoints to generate a testrun report.
 * Submit it for review. Also endpoints for partner to view, approve or reject
 * the report.
 * 
 * @author Mayura Deshmukh
 * @since 1.3.0
 *
 */
@RestController
public class ReportController {

	/** The Constant PARTNER_REPORT_ID. */
	private static final String PARTNER_REPORT_ID = "partner.report.post";

	/** The Constant ADMIN_REPORT_ID. */
	private static final String ADMIN_REPORT_ID = "admin.report.post";

	@Autowired
	private ReportService service;

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

	@PostMapping(value = "/generateDraftReport")
	public ResponseEntity<?> generateDraftReport(@RequestBody @Valid RequestWrapper<ReportRequestDto> value,
			@RequestHeader String origin, Errors errors) throws Exception {
		requestValidator.validate(value, errors);
		requestValidator.validateId(PARTNER_REPORT_ID, value.getId(), errors);
		DataValidationUtil.validate(errors, PARTNER_REPORT_ID);
		return service.generateDraftReport(value.getRequest(), origin);
	}

	@PostMapping(value = "/submitReportForReview")
	public ResponseWrapper<ComplianceTestRunSummaryDto> submitReportForReview(
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(PARTNER_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, PARTNER_REPORT_ID);
		return service.updateReportStatus(service.getPartnerId(), reportRequestWrapper,
				AppConstants.REPORT_STATUS_DRAFT, AppConstants.REPORT_STATUS_REVIEW);
	}

	@GetMapping(value = "/getSubmittedReportList")
	public ResponseWrapper<List<ComplianceTestRunSummaryDto>> getSubmittedReportList() throws Exception {
		return service.getReportList(false, null);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@GetMapping(value = "/getPartnerReportList/{reportStatus}")
	public ResponseWrapper<List<ComplianceTestRunSummaryDto>> getPartnerReportList(@PathVariable String reportStatus)
			throws Exception {
		return service.getReportList(true, reportStatus);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/getPartnerReport/{partnerId}")
	public ResponseEntity<?> getPartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(ADMIN_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ADMIN_REPORT_ID);
		return service.getPartnerReport(partnerId, reportRequestWrapper.getRequest());
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/approvePartnerReport/{partnerId}")
	public ResponseWrapper<ComplianceTestRunSummaryDto> approvePartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(ADMIN_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ADMIN_REPORT_ID);
		return service.updateReportStatus(partnerId, reportRequestWrapper, AppConstants.REPORT_STATUS_REVIEW,
				AppConstants.REPORT_STATUS_APPROVED);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/rejectPartnerReport/{partnerId}")
	public ResponseWrapper<ComplianceTestRunSummaryDto> rejectPartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(ADMIN_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ADMIN_REPORT_ID);
		return service.updateReportStatus(partnerId, reportRequestWrapper, AppConstants.REPORT_STATUS_REVIEW,
				AppConstants.REPORT_STATUS_REJECTED);
	}
}