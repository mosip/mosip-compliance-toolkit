package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "report-controller")
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

	@PostMapping(value = "/isReportAlreadySubmitted")
	@Operation(summary = "Check if report submitted", description = "Check whether the report has already been submitted.", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<Boolean> isReportAlreadySubmitted(
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForPartner(reportRequestWrapper, errors);
		return service.isReportAlreadySubmitted(reportRequestWrapper);
	}

	@PostMapping(value = "/generateDraftReport")
	@Operation(summary = "Generate draft report", description = "Generate draft report for compliance collection", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<?> generateDraftReport(@RequestBody @Valid RequestWrapper<ReportRequestDto> value,
			@RequestHeader String origin, Errors errors) throws Exception {
		validateRequestForPartner(value, errors);
		return service.generateDraftReport(value.getRequest(), origin);
	}

	@PostMapping(value = "/generateDraftQAReport")
	@Operation(summary = "Generate QA draft report", description = "Generate draft report for quality assessment collection", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<?> generateDraftQAReport(@RequestBody @Valid RequestWrapper<ReportRequestDto> value,
			@RequestHeader String origin, Errors errors) throws Exception {
		validateRequestForPartner(value, errors);
		return service.generateDraftQAReport(value.getRequest(), origin);
	}

	@PostMapping(value = "/submitReportForReview")
	@Operation(summary = "Submit the report for review", description = "Partner can submit their report for admin review", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ComplianceTestRunSummaryDto> submitReportForReview(
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForPartner(reportRequestWrapper, errors);
		return service.updateReportStatus(service.getPartnerId(), reportRequestWrapper,
				AppConstants.REPORT_STATUS_DRAFT, AppConstants.REPORT_STATUS_REVIEW);
	}

	@PostMapping(value = "/getSubmittedReport")
	@Operation(summary = "Get submitted report", description = "Download partner submitted report for review", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<?> getSubmittedReport(
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForPartner(reportRequestWrapper, errors);
		// when user is downloading submitted report for self, then there is no need for
		// testrunId
		return service.getSubmittedReport(service.getPartnerId(), reportRequestWrapper.getRequest(), true);
	}

	@GetMapping(value = "/getSubmittedReportList")
	@Operation(summary = "Get submitted report list", description = "Get submitted report list", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<ComplianceTestRunSummaryDto>> getSubmittedReportList() throws Exception {
		return service.getReportList(false, null);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@GetMapping(value = "/getPartnerReportList/{reportStatus}")
	@Operation(summary = "Get partner report list", description = "Get partner report list by report status", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<ComplianceTestRunSummaryDto>> getPartnerReportList(@PathVariable String reportStatus)
			throws Exception {
		return service.getReportList(true, reportStatus);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/getPartnerReport/{partnerId}")
	@Operation(summary = "Get partner report", description = "Get partner report by partner id", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<?> getPartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForAdmin(reportRequestWrapper, errors);
		return service.getSubmittedReport(partnerId, reportRequestWrapper.getRequest(), false);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/approvePartnerReport/{partnerId}")
	@Operation(summary = "Approve partner report", description = "Admin can approve partner's report", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ComplianceTestRunSummaryDto> approvePartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForAdmin(reportRequestWrapper, errors);
		return service.updateReportStatus(partnerId, reportRequestWrapper, AppConstants.REPORT_STATUS_REVIEW,
				AppConstants.REPORT_STATUS_APPROVED);
	}

	@PreAuthorize("hasAnyRole(@authorizedRoles.getAdminPartnerReport())")
	@PostMapping(value = "/rejectPartnerReport/{partnerId}")
	@Operation(summary = "Reject partner report", description = "Admin can reject partner's report", tags = "report-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<ComplianceTestRunSummaryDto> rejectPartnerReport(@PathVariable String partnerId,
			@RequestBody @Valid RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors) throws Exception {
		validateRequestForAdmin(reportRequestWrapper, errors);
		return service.updateReportStatus(partnerId, reportRequestWrapper, AppConstants.REPORT_STATUS_REVIEW,
				AppConstants.REPORT_STATUS_REJECTED);
	}

	private void validateRequestForAdmin(RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors)
			throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(ADMIN_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, ADMIN_REPORT_ID);
	}

	private void validateRequestForPartner(RequestWrapper<ReportRequestDto> reportRequestWrapper, Errors errors)
			throws Exception {
		requestValidator.validate(reportRequestWrapper, errors);
		requestValidator.validateId(PARTNER_REPORT_ID, reportRequestWrapper.getId(), errors);
		DataValidationUtil.validate(errors, PARTNER_REPORT_ID);
	}
}