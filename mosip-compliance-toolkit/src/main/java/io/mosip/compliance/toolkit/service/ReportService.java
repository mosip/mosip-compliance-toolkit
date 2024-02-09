package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.mosip.compliance.toolkit.entity.ComplianceReportSummaryEntity;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.report.AbisProjectTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores;
import io.mosip.compliance.toolkit.dto.report.ComplianceTestRunSummaryDto;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto.Partner;
import io.mosip.compliance.toolkit.dto.report.PartnerTable;
import io.mosip.compliance.toolkit.dto.report.ReportDataDto;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.report.SbiProjectTable;
import io.mosip.compliance.toolkit.dto.report.SdkProjectTable;
import io.mosip.compliance.toolkit.dto.report.TestRunTable;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.ComplianceTestRunSummaryRepository;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.repository.SdkProjectRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ReportService {
 	
  private static final String RACES_STR = "races";

	private static final String OCCUPATIONS_STR = "occupations";

	private static final String AGE_GROUPS_STR = "ageGroups";

	private static final String TEST_RUN_REPORT_VM = "testRunReport.vm";

	private static final String BIOMETRIC_TYPE = "biometricType";

	private static final String BIOMETRIC_SCORES = "biometricScores";

	private static final String STATUS_TEXT = "statusText";

	private static final String COLLECTION_NAME = "collectionName";

	private static final String COUNT_OF_FAILED_TEST_CASES = "countOfFailedTestCases";

	private static final String COUNT_OF_PASSED_TEST_CASES = "countOfPassedTestCases";

	private static final String TOTAL_TEST_CASES_COUNT = "totalTestCasesCount";

	private static final String TIME_TAKEN_BY_TEST_RUN = "timeTakenByTestRun";

	private static final String TEST_RUN_DETAILS_LIST = "testRunDetailsList";

	private static final String REPORT_VALIDITY_DATE = "reportValidityDate";

	private static final String REPORT_EXPIRY_PERIOD = "reportExpiryPeriod";

	private static final String TEST_RUN_START_TIME = "testRunStartTime";

	private static final String ABIS_PROJECT_DETAILS_TABLE = "abisProjectDetailsTable";

	private static final String SDK_PROJECT_DETAILS_TABLE = "sdkProjectDetailsTable";

	private static final String SBI_PROJECT_DETAILS_TABLE = "sbiProjectDetailsTable";

	private static final String PARTNER_DETAILS = "partnerDetails";

	private static final String ORIGIN_KEY = "origin";

	private static final String PROJECT_TYPE = "projectType";

	private static final String BLANK_STRING = "";

	private static final String VALIDATION_ERR_REPORT_UNDER_REVIEW = "Report for this project is already under review hence new report cannot be generated.";

	private static final String VALIDATION_ERR_TEST_DATA = "Only MOSIP_DEFAULT test data should be used with the Test Run to generate the report. It is failing for testcase :";

	private static final String VALIDATION_ERR_DEVICE_INFO = "Make, model and serial number of the device should be same in all the testcases. It is failing for testcase :";

	private Logger log = LoggerConfiguration.logConfig(ReportService.class);

	@Autowired
	private TestRunService testRunService;

	@Autowired
	private SbiProjectService sbiProjectService;

	@Autowired
	private SdkProjectService sdkProjectService;

	@Autowired
	private AbisProjectService abisProjectService;

	@Autowired
	private CollectionsService collectionsService;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@Autowired
	private PartnerManagerHelper partnerManagerHelper;

	@Value("${mosip.toolkit.report.expiryperiod.in.months}")
	private int reportExpiryPeriod;

	@Value("${mosip.toolkit.sdk.testcases.ignore.list}")
	private String ignoreTestDataSourceForSdkTestcases;

	@Value("${mosip.toolkit.abis.testcases.ignore.list}")
	private String ignoreTestDataSourceForAbisTestcases;

	@Value("$(mosip.toolkit.api.id.admin.report.get)")
	private String getAdminReportId;

	@Value("$(mosip.toolkit.api.id.partner.report.get)")
	private String getPartnerReportId;

	@Autowired
	private ComplianceTestRunSummaryRepository complianceTestRunSummaryRepository;

	@Autowired
	private CollectionsRepository collectionsRepository;

	@Autowired
	private SbiProjectRepository sbiProjectRepository;

	@Autowired
	private SdkProjectRepository sdkProjectRepository;

	@Autowired
	private AbisProjectRepository abisProjectRepository;

	@Autowired
	ResourceCacheService resourceCacheService;

	@Autowired
	BiometricScoresService biometricScoresService;

	@Value("#{'${mosip.toolkit.quality.assessment.age.groups}'.split(',')}")
	private List<String> ageGroups;

	@Value("#{'${mosip.toolkit.quality.assessment.occupations}'.split(',')}")
	private List<String> occupations;

	@Value("#{'${mosip.toolkit.quality.assessment.races}'.split(',')}")
	private List<String> races;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	private String getUserBy() {
		String crBy = authUserDetails().getUsername();
		return crBy;
	}

	public ResponseEntity<?> generateDraftReport(ReportRequestDto requestDto, String origin) {
		try {
			log.info("sessionId", "idType", "id", "Started generateDraftReport processing");
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			logInput(requestDto, projectType, projectId);
			// 1. get the test run details
			ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = getTestRunDetails(
					requestDto.getTestRunId());
			ResponseEntity<Resource> errResource = handleServiceErrors(requestDto, testRunDetailsResponse.getErrors());
			if (errResource != null) {
				return errResource;
			}
			TestRunDetailsResponseDto testRunDetailsResponseDto = testRunDetailsResponse.getResponse();
			// 2. Validate that report can be generated
			// Chk if report is already under review, so new report cannot be generated
			ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
			pk.setPartnerId(getPartnerId());
			pk.setProjectId(projectId);
			pk.setCollectionId(testRunDetailsResponseDto.getCollectionId());
			Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
			if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())) {
				if (!AppConstants.REPORT_STATUS_DRAFT.equals(optionalEntity.get().getReportStatus())) {
					// report is already under review, so new report cannot be generated
					return handleValidationErrors(requestDto, VALIDATION_ERR_REPORT_UNDER_REVIEW);
				}
			}
			SbiProjectTable sbiProjectTable = new SbiProjectTable();
			if (ProjectTypes.SBI.getCode().equals(projectType)) {
				String invalidTestCaseId = this.validateDeviceInfo(getPartnerId(), testRunDetailsResponseDto,
						sbiProjectTable);
				if (!BLANK_STRING.equals(invalidTestCaseId)) {
					return handleValidationErrors(requestDto, VALIDATION_ERR_DEVICE_INFO + invalidTestCaseId);
				}
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType) || ProjectTypes.SDK.getCode().equals(projectType)) {
				String invalidTestCaseId = this.validateTestDataSource(testRunDetailsResponseDto, projectType);
				if (!BLANK_STRING.equals(invalidTestCaseId)) {
					return handleValidationErrors(requestDto, VALIDATION_ERR_TEST_DATA + invalidTestCaseId);
				}
			}
			// 3. Get the Project details
			SbiProjectDto sbiProjectDto = null;
			SdkProjectDto sdkProjectDto = null;
			AbisProjectDto abisProjectDto = null;
			if (ProjectTypes.SBI.getCode().equals(projectType)) {
				ResponseWrapper<SbiProjectDto> sbiProjectResponse = sbiProjectService.getSbiProject(projectId);
				ResponseEntity<Resource> sbiErrResource = handleServiceErrors(requestDto,
						sbiProjectResponse.getErrors());
				if (sbiErrResource != null) {
					return sbiErrResource;
				}
				sbiProjectDto = sbiProjectResponse.getResponse();
			}
			if (ProjectTypes.SDK.getCode().equals(projectType)) {
				ResponseWrapper<SdkProjectDto> sdkProjectResponse = sdkProjectService.getSdkProject(projectId);
				ResponseEntity<Resource> sdkErrResource = handleServiceErrors(requestDto,
						sdkProjectResponse.getErrors());
				if (sdkErrResource != null) {
					return sdkErrResource;
				}
				sdkProjectDto = sdkProjectResponse.getResponse();
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType)) {
				ResponseWrapper<AbisProjectDto> abisProjectResponse = abisProjectService.getAbisProject(projectId);
				ResponseEntity<Resource> abisErrResource = handleServiceErrors(requestDto,
						abisProjectResponse.getErrors());
				if (abisErrResource != null) {
					return abisErrResource;
				}
				abisProjectDto = abisProjectResponse.getResponse();
			}
			// 4. Populate all attributes in velocity template
			VelocityContext velocityContext = populateVelocityAttributes(testRunDetailsResponseDto, sbiProjectDto,
					sdkProjectDto, abisProjectDto, origin, projectType, projectId, sbiProjectTable, null, null);
			// 5. Merge velocity HTML template with all attributes
			String mergedHtml = mergeVelocityTemplate(velocityContext, TEST_RUN_REPORT_VM);
			// 6. Covert the merged HTML to PDF
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			// 7. Save Report Data in DB for future
			saveReportData(projectType, projectId, testRunDetailsResponseDto, velocityContext);
			// 8. Send PDF in response
			return sendPdfResponse(requestDto, resource);

		} catch (Exception e) {
			log.info("sessionId", "idType", "id", "Exception in generateDraftReport " + e.getLocalizedMessage());
			try {
				return handleValidationErrors(requestDto, e.getLocalizedMessage());
			} catch (Exception e1) {
				return ResponseEntity.noContent().build();
			}
		}

	}

	public ResponseEntity<?> generateDraftQAReport(ReportRequestDto requestDto, String origin) {
		try {
			log.info("sessionId", "idType", "id", "Started generateDraftQAReport processing");
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			logInput(requestDto, projectType, projectId);
			// 1. Basic request validation
			if (!ProjectTypes.SBI.getCode().equals(projectType)) {
				return handleValidationErrors(requestDto, "This report is availble only for SBI projects");
			}
			if (!Objects.nonNull(projectId)) {
				return handleValidationErrors(requestDto, "Invalid request. Project Id cannot be null");
			}
			if (!Objects.nonNull(requestDto.getTestRunId())) {
				return handleValidationErrors(requestDto, "Invalid request. Test Run Id cannot be null");
			}
			// 2. get the test run details
			ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = getTestRunDetails(
					requestDto.getTestRunId());
			ResponseEntity<Resource> errResource = handleServiceErrors(requestDto, testRunDetailsResponse.getErrors());
			if (errResource != null) {
				return errResource;
			}
			TestRunDetailsResponseDto testRunDetailsResponseDto = testRunDetailsResponse.getResponse();
			// 3. Business rules validations to check that report can be generated
			// Chk if report is already under review, so new report cannot be generated
			ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
			pk.setPartnerId(getPartnerId());
			pk.setProjectId(projectId);
			pk.setCollectionId(testRunDetailsResponseDto.getCollectionId());
			Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
			if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())) {
				if (!AppConstants.REPORT_STATUS_DRAFT.equals(optionalEntity.get().getReportStatus())) {
					// report is already under review, so new report cannot be generated
					return handleValidationErrors(requestDto, VALIDATION_ERR_REPORT_UNDER_REVIEW);
				}
			}
			SbiProjectTable sbiProjectTable = new SbiProjectTable();
			String invalidTestCaseId = this.validateDeviceInfo(getPartnerId(), testRunDetailsResponseDto,
					sbiProjectTable);
			if (!BLANK_STRING.equals(invalidTestCaseId)) {
				return handleValidationErrors(requestDto, VALIDATION_ERR_DEVICE_INFO + invalidTestCaseId);
			}
			// 4. Get the Project details
			SbiProjectDto sbiProjectDto = null;
			ResponseWrapper<SbiProjectDto> sbiProjectResponse = sbiProjectService.getSbiProject(projectId);
			ResponseEntity<Resource> sbiErrResource = handleServiceErrors(requestDto, sbiProjectResponse.getErrors());
			if (sbiErrResource != null) {
				return sbiErrResource;
			}
			sbiProjectDto = sbiProjectResponse.getResponse();
			String biometricType = sbiProjectDto.getDeviceType();
			// 5. Get the list of biometric scores
			List<BiometricScores> biometricScoresList = null;
			if (AppConstants.BIOMETRIC_SCORES_FINGER.equals(biometricType)) {
				biometricScoresList = biometricScoresService.getFingerBiometricScoresList(getPartnerId(), projectId,
						requestDto.getTestRunId());
			}
			if (AppConstants.BIOMETRIC_SCORES_FACE.equals(biometricType)) {
				biometricScoresList = biometricScoresService.getFaceBiometricScoresList(getPartnerId(), projectId,
						requestDto.getTestRunId());
			}
			if (AppConstants.BIOMETRIC_SCORES_IRIS.equals(biometricType)) {
				biometricScoresList = biometricScoresService.getIrisBiometricScoresList(getPartnerId(), projectId,
						requestDto.getTestRunId());
			}
			// 6. Populate all attributes in velocity template
			VelocityContext velocityContext = populateVelocityAttributes(testRunDetailsResponseDto, sbiProjectDto, null,
					null, origin, projectType, projectId, sbiProjectTable, biometricScoresList, biometricType);

			// 7. Merge velocity HTML template with all attributes
			String mergedHtml = mergeVelocityTemplate(velocityContext, TEST_RUN_REPORT_VM);
			// 8. Covert the merged HTML to PDF
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			// 9. Save Report Data in DB for future
			saveReportData(projectType, projectId, testRunDetailsResponseDto, velocityContext);
			// 10. Send PDF in response
			return sendPdfResponse(requestDto, resource);

		} catch (Exception e) {
			log.info("sessionId", "idType", "id", "Exception in generateDraftQAReport " + e.getLocalizedMessage());
			try {
				return handleValidationErrors(requestDto, e.getLocalizedMessage());
			} catch (Exception e1) {
				return ResponseEntity.noContent().build();
			}
		}

	}

	private void logInput(ReportRequestDto requestDto, String projectType, String projectId) {
		log.info("sessionId", "idType", "id", "projectType: " + projectType);
		log.info("sessionId", "idType", "id", "projectId: " + projectId);
		log.info("sessionId", "idType", "id", "collectionId: " + requestDto.getCollectionId());
		if (requestDto.getTestRunId() != null) {
			log.info("sessionId", "idType", "id", "testRunId: " + requestDto.getTestRunId());
		} else {
			log.info("sessionId", "idType", "id", "testRunId: null");
		}
	}

	private void saveReportData(String projectType, String projectId,
			TestRunDetailsResponseDto testRunDetailsResponseDto, VelocityContext velocityContext)
			throws JsonProcessingException {

		ReportDataDto reportDataDto = new ReportDataDto();
		reportDataDto.setProjectType(velocityContext.get(PROJECT_TYPE).toString());
		reportDataDto.setOrigin(velocityContext.get(ORIGIN_KEY).toString());
		reportDataDto.setStatusText(velocityContext.get(STATUS_TEXT).toString());
		reportDataDto.setPartnerDetails((PartnerTable) velocityContext.get(PARTNER_DETAILS));
		if (ProjectTypes.SBI.getCode().equals(projectType)) {
			reportDataDto.setSbiProjectDetailsTable((SbiProjectTable) velocityContext.get(SBI_PROJECT_DETAILS_TABLE));
		}
		if (ProjectTypes.SDK.getCode().equals(projectType)) {
			reportDataDto.setSdkProjectDetailsTable((SdkProjectTable) velocityContext.get(SDK_PROJECT_DETAILS_TABLE));
		}
		if (ProjectTypes.ABIS.getCode().equals(projectType)) {
			reportDataDto
					.setAbisProjectDetailsTable((AbisProjectTable) velocityContext.get(ABIS_PROJECT_DETAILS_TABLE));
		}
		reportDataDto.setCollectionName(velocityContext.get(COLLECTION_NAME).toString());
		reportDataDto.setTestRunStartTime(velocityContext.get(TEST_RUN_START_TIME).toString());
		reportDataDto.setReportExpiryPeriod(Integer.parseInt(velocityContext.get(REPORT_EXPIRY_PERIOD).toString()));
		reportDataDto.setReportValidityDate(velocityContext.get(REPORT_VALIDITY_DATE).toString());
		reportDataDto.setTestRunDetailsList(getObjectMapper().convertValue(velocityContext.get(TEST_RUN_DETAILS_LIST),
				new TypeReference<List<TestRunTable>>() {
				}));
		reportDataDto.setTimeTakenByTestRun(velocityContext.get(TIME_TAKEN_BY_TEST_RUN).toString());
		reportDataDto.setTotalTestCasesCount(Integer.parseInt(velocityContext.get(TOTAL_TEST_CASES_COUNT).toString()));
		reportDataDto.setCountOfPassedTestCases(
				Integer.parseInt(velocityContext.get(COUNT_OF_PASSED_TEST_CASES).toString()));
		reportDataDto.setCountOfFailedTestCases(
				Integer.parseInt(velocityContext.get(COUNT_OF_FAILED_TEST_CASES).toString()));
		if (velocityContext.get(BIOMETRIC_TYPE) != null && velocityContext.get(BIOMETRIC_SCORES) != null) {
			reportDataDto.setBiometricType(velocityContext.get(BIOMETRIC_TYPE).toString());
			reportDataDto.setAgeGroups(getObjectMapper().convertValue(velocityContext.get(AGE_GROUPS_STR),
					new TypeReference<List<String>>() {
					}));
			reportDataDto.setOccupations(getObjectMapper().convertValue(velocityContext.get(OCCUPATIONS_STR),
					new TypeReference<List<String>>() {
					}));
			reportDataDto.setRaces(
					getObjectMapper().convertValue(velocityContext.get(RACES_STR), new TypeReference<List<String>>() {
					}));
			reportDataDto.setBiometricScores(getObjectMapper().convertValue(velocityContext.get(BIOMETRIC_SCORES),
					new TypeReference<List<BiometricScores>>() {
					}));
    }
		LocalDateTime nowDate = LocalDateTime.now();
		ComplianceTestRunSummaryEntity entity = new ComplianceTestRunSummaryEntity();
		entity.setProjectId(projectId);
		entity.setCollectionId(testRunDetailsResponseDto.getCollectionId());
		entity.setRunId(testRunDetailsResponseDto.getRunId());
		entity.setProjectType(projectType);
		entity.setPartnerId(getPartnerId());
		entity.setOrgName(resourceCacheService.getOrgName(this.getPartnerId()));
		String reportData = getObjectMapper().writeValueAsString(reportDataDto);
		String encodedReportData = StringUtil.base64Encode(reportData);
		entity.setReportDataJson(encodedReportData);
		entity.setReportStatus(AppConstants.REPORT_STATUS_DRAFT);

		ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
		pk.setPartnerId(getPartnerId());
		pk.setProjectId(projectId);
		pk.setCollectionId(testRunDetailsResponseDto.getCollectionId());
		Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
		if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())) {
			log.info("sessionId", "idType", "id", "Updating report data in DB");
			entity.setUpdBy(this.getUserBy());
			entity.setUpdDtimes(nowDate);
			entity.setCrBy(optionalEntity.get().getCrBy());
			entity.setCrDtimes(optionalEntity.get().getCrDtimes());
		} else {
			entity.setCrBy(this.getUserBy());
			entity.setCrDtimes(nowDate);
		}
		entity.setDeleted(false);

		complianceTestRunSummaryRepository.save(entity);
		log.info("sessionId", "idType", "id", "Saved report data successfully in DB");
	}

	private ResponseEntity<Resource> handleServiceErrors(ReportRequestDto requestDto, List<ServiceError> serviceErrors)
			throws Exception, IOException {
		ResponseEntity<Resource> errResource = null;
		if (serviceErrors != null && serviceErrors.size() > 0) {
			String err = serviceErrors.get(0).getMessage();
			String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_ERR.getErrorMessage() + " - " + err;
			log.error("sessionId", "idType", "id", "In handleServiceErrors method - " + err);
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("error", errorMessage);
			String mergedHtml = mergeVelocityTemplate(velocityContext, "errTestRunReport.vm");
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			errResource = sendPdfResponse(requestDto, resource);
		}
		return errResource;
	}

	private ResponseEntity<Resource> handleValidationErrors(ReportRequestDto requestDto, String message)
			throws Exception, IOException {
		String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_ERR.getErrorMessage() + " - " + message;
		log.error("sessionId", "idType", "id", "In handleValidationErrors method - " + message);
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("error", errorMessage);
		String mergedHtml = mergeVelocityTemplate(velocityContext, "errTestRunReport.vm");
		ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
		ResponseEntity<Resource> errResource = sendPdfResponse(requestDto, resource);
		return errResource;
	}

	private VelocityContext populateVelocityAttributes(TestRunDetailsResponseDto testRunDetailsResponseDto,
			SbiProjectDto sbiProjectDto, SdkProjectDto sdkProjectDto, AbisProjectDto abisProjectDto, String origin,
			String projectType, String projectId, SbiProjectTable sbiProjectTable,
			List<BiometricScores> biometricScoresList, String biometricType) throws Exception {

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put(PROJECT_TYPE, projectType);
		velocityContext.put(STATUS_TEXT, AppConstants.REPORT_STATUS_DRAFT.toUpperCase());
		velocityContext.put(ORIGIN_KEY, getOrigin(origin));
		velocityContext.put(PARTNER_DETAILS, getPartnerDetails(getPartnerId()));
		if (ProjectTypes.SBI.getCode().equals(projectType)) {
			velocityContext.put(SBI_PROJECT_DETAILS_TABLE, getSbiProjectDetails(sbiProjectDto,
					testRunDetailsResponseDto.getTestRunDetailsList(), sbiProjectTable));
		}
		if (ProjectTypes.SDK.getCode().equals(projectType)) {
			velocityContext.put(SDK_PROJECT_DETAILS_TABLE, getSdkProjectDetails(sdkProjectDto));
		}
		if (ProjectTypes.ABIS.getCode().equals(projectType)) {
			velocityContext.put(ABIS_PROJECT_DETAILS_TABLE, getAbisProjectDetails(abisProjectDto));
		}
		List<TestCaseDto> allTestCases = getAllTestcases(testRunDetailsResponseDto);
		int countOfAllTestCases = allTestCases.size();
		int countOfSuccessTestCases = countOfSuccessTestCases(allTestCases, testRunDetailsResponseDto);
		int countOfFailedTestCases = countOfAllTestCases - countOfSuccessTestCases;

		velocityContext.put(COLLECTION_NAME,
				this.getCollectionName(testRunDetailsResponseDto.getCollectionId(), this.getPartnerId()));
		velocityContext.put(TEST_RUN_START_TIME, getTestRunStartDt(testRunDetailsResponseDto));
		velocityContext.put(REPORT_EXPIRY_PERIOD, reportExpiryPeriod);
		velocityContext.put(REPORT_VALIDITY_DATE, getReportValidityDt(testRunDetailsResponseDto));
		velocityContext.put(TEST_RUN_DETAILS_LIST, populateTestRunTable(allTestCases, testRunDetailsResponseDto));
		velocityContext.put(TIME_TAKEN_BY_TEST_RUN, getTestRunExecutionTime(testRunDetailsResponseDto));
		velocityContext.put(TOTAL_TEST_CASES_COUNT, countOfAllTestCases);
		velocityContext.put(COUNT_OF_PASSED_TEST_CASES, countOfSuccessTestCases);
		velocityContext.put(COUNT_OF_FAILED_TEST_CASES, countOfFailedTestCases);
		if (biometricScoresList != null && biometricType != null) {
			velocityContext.put(AGE_GROUPS_STR, ageGroups);
			velocityContext.put(OCCUPATIONS_STR, occupations);
			velocityContext.put(RACES_STR, races);
			velocityContext.put(BIOMETRIC_TYPE, biometricType);
			velocityContext.put(BIOMETRIC_SCORES, biometricScoresList);
		}
		log.info("sessionId", "idType", "id", "Added all attributes in velocity template successfully");
		return velocityContext;
	}

	private String validateTestDataSource(TestRunDetailsResponseDto testRunDetailsResponseDto, String projectType) {

		String invalidTestCaseId = BLANK_STRING;
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		boolean validationResult = true;
		List<String> ignoreSdkTestcaseList = Arrays.asList(ignoreTestDataSourceForSdkTestcases.split(","));
		List<String> ignoreAbisTestcaseList = Arrays.asList(ignoreTestDataSourceForAbisTestcases.split(","));
		for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
			if (ProjectTypes.SDK.getCode().equals(projectType)
					&& !ignoreSdkTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("sessionId", "idType", "id", "testdata validation failed for {}",
						testRunDetailsDto.getTestcaseId());
				validationResult = false;
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType)
					&& !ignoreAbisTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("sessionId", "idType", "id", "testdata validation failed for {}",
						testRunDetailsDto.getTestcaseId());
				validationResult = false;
			}
			if (!validationResult) {
				invalidTestCaseId = testRunDetailsDto.getTestcaseId();
				break;
			}
		}

		return invalidTestCaseId;
	}

	private String validateDeviceInfo(String partnerId, TestRunDetailsResponseDto testRunDetailsResponseDto,
			SbiProjectTable sbiProjectTable) {
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		String invalidTestCaseId = BLANK_STRING;
		boolean validationResult = true;
		String currentTestCaseId = BLANK_STRING;
		for (TestRunDetailsDto testRunPartialDetails : testRunDetailsList) {
			boolean proceed = false;
			if (!currentTestCaseId.equals(testRunPartialDetails.getTestcaseId())) {
				proceed = true;
				currentTestCaseId = testRunPartialDetails.getTestcaseId();
			} else if (currentTestCaseId.equals(testRunPartialDetails.getTestcaseId())) {
				proceed = false;
			}
			log.info("currentTestCaseId: " + currentTestCaseId);
			log.info("methodId: " + testRunPartialDetails.getMethodId());
			log.info("proceed: " + proceed);
			if (proceed) {
				// get the method details
				TestRunDetailsDto testRunFullDetails = null;
				try {
					log.info("sessionId", "idType", "id",
							"Fetching full test run details: " + testRunPartialDetails.getMethodId());
					ResponseWrapper<TestRunDetailsDto> testRunFullDetailsResp = testRunService.getMethodDetails(
							partnerId, testRunPartialDetails.getRunId(), testRunPartialDetails.getTestcaseId(),
							testRunPartialDetails.getMethodId());
					if (testRunFullDetailsResp != null) {
						testRunFullDetails = testRunFullDetailsResp.getResponse();
					}
				} catch (Exception ex) {
					log.debug("sessionId", "idType", "id", ex.getStackTrace());
					log.debug("sessionId", "idType", "id", "unable to fetch test run full details{}",
							testRunPartialDetails.getTestcaseId());
				}
				if (validationResult && testRunFullDetails != null && testRunFullDetails.getMethodResponse() != null) {
					// check if the method is "discover"
					try {
						ArrayNode discoverRespArr = (ArrayNode) getObjectMapper()
								.readValue(testRunFullDetails.getMethodResponse(), ArrayNode.class);
						if (discoverRespArr != null && discoverRespArr.size() > 0) {
							ObjectNode discoverResp = (ObjectNode) discoverRespArr.get(0);
							validationResult = validateDeviceMakeModelSerialNo(sbiProjectTable, validationResult,
									discoverResp);
						}
					} catch (Exception ex) {
						// ignore since method may not be "discover"
					}
				}
				if (validationResult && testRunFullDetails != null && testRunFullDetails.getMethodResponse() != null) {
					// check if the method is "deviceInfo"
					try {
						ArrayNode deviceInfoRespArr = (ArrayNode) getObjectMapper()
								.readValue(testRunFullDetails.getMethodResponse(), ArrayNode.class);
						ObjectNode deviceInfoResp = null;
						if (deviceInfoRespArr != null && deviceInfoRespArr.size() > 0) {
							deviceInfoResp = (ObjectNode) deviceInfoRespArr.get(0);
							ObjectNode deviceInfoDecoded = (ObjectNode) deviceInfoResp
									.get(AppConstants.DEVICE_INFO_DECODED);
							validationResult = validateDeviceMakeModelSerialNo(sbiProjectTable, validationResult,
									deviceInfoDecoded);
						}
					} catch (Exception ex) {
						// ignore since method may not be "deviceInfo"
					}
				}
				if (validationResult && testRunFullDetails != null && testRunFullDetails.getMethodResponse() != null) {
					// check if the method is "capture" or "rcapture"
					try {
						ObjectNode methodResponse = (ObjectNode) getObjectMapper()
								.readValue(testRunFullDetails.getMethodResponse(), ObjectNode.class);
						JsonNode arrBiometricNodes = methodResponse.get(AppConstants.BIOMETRICS);
						if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
							for (final JsonNode biometricNode : arrBiometricNodes) {
								JsonNode dataNode = biometricNode.get(AppConstants.DECODED_DATA);
								validationResult = validateDeviceMakeModelSerialNo(sbiProjectTable, validationResult,
										dataNode);
							}

						}
					} catch (Exception e) {
						// ignore since method may not be "capture" or "rcapture"
					}
				}
				if (!validationResult) {
					invalidTestCaseId = testRunFullDetails.getTestcaseId();
					break;
				}
			}
		}
		log.info("sessionId", "idType", "id", "validateDeviceInfo, validationResult: {}", validationResult);
		return invalidTestCaseId;
	}

	private boolean validateDeviceMakeModelSerialNo(SbiProjectTable sbiProjectTable, boolean validationResult,
			JsonNode dataNode) {
		JsonNode digitalIdDecoded = dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA);
		String makeInResp = digitalIdDecoded.get(AppConstants.MAKE).asText();
		if (sbiProjectTable.getDeviceMake() == null && validationResult) {
			sbiProjectTable.setDeviceMake(makeInResp);
		} else {
			if (makeInResp == null) {
				validationResult = false;
			} else if (!makeInResp.equals(sbiProjectTable.getDeviceMake())) {
				validationResult = false;
			}
		}
		String modelInResp = digitalIdDecoded.get(AppConstants.MODEL).asText();
		if (sbiProjectTable.getDeviceModel() == null && validationResult) {
			sbiProjectTable.setDeviceModel(modelInResp);
		} else {
			if (modelInResp == null) {
				validationResult = false;
			} else if (!modelInResp.equals(sbiProjectTable.getDeviceModel())) {
				validationResult = false;
			}
		}
		String serialNoInResp = digitalIdDecoded.get(AppConstants.SERIAL_NO).asText();
		if (sbiProjectTable.getDeviceSerialNo() == null && validationResult) {
			sbiProjectTable.setDeviceSerialNo(serialNoInResp);
		} else {
			if (serialNoInResp == null) {
				validationResult = false;
			} else if (!serialNoInResp.equals(sbiProjectTable.getDeviceSerialNo())) {
				validationResult = false;
			}
		}
		// also set the device provider and device provider id
		sbiProjectTable.setDeviceProvider(digitalIdDecoded.get(AppConstants.DEVICE_PROVIDER).asText());
		sbiProjectTable.setDeviceProviderId(digitalIdDecoded.get(AppConstants.DEVICE_PROVIDER_ID).asText());

		return validationResult;
	}

	private PartnerTable getPartnerDetails(String projectId) throws Exception {
		PartnerTable partnerTable = new PartnerTable();
		PartnerDetailsDto partnerDetailsDto = partnerManagerHelper.getPartnerDetails(projectId);
		if (partnerDetailsDto != null && partnerDetailsDto.getErrors().size() == 0) {
			Partner partner = partnerDetailsDto.getResponse();
			partnerTable.setOrgName(partner.getOrganizationName());
			partnerTable.setAddress(partner.getAddress());
			partnerTable.setEmail(partner.getEmailId());
			partnerTable.setPhoneNumber(partner.getContactNumber());
		}
		return partnerTable;
	}

	private SbiProjectTable getSbiProjectDetails(SbiProjectDto sbiProjectDto,
			List<TestRunDetailsDto> testRunDetailsList, SbiProjectTable sbiProjectTable) {
		sbiProjectTable.setProjectName(sbiProjectDto.getName());
		sbiProjectTable.setProjectType(sbiProjectDto.getProjectType());
		sbiProjectTable.setPurpose(sbiProjectDto.getPurpose());
		sbiProjectTable.setSpecVersion(sbiProjectDto.getSbiVersion());
		sbiProjectTable.setSbiHash(sbiProjectDto.getSbiHash());
		sbiProjectTable.setDeviceType(sbiProjectDto.getDeviceType());
		sbiProjectTable.setDeviceSubType(sbiProjectDto.getDeviceSubType());
		sbiProjectTable.setWebsite(sbiProjectDto.getWebsiteUrl());
		List<String> deviceImages = new ArrayList<>();
		if (sbiProjectDto.getDeviceImage1() != null) {
			deviceImages.add(sbiProjectDto.getDeviceImage1());
		}
		if (sbiProjectDto.getDeviceImage2() != null) {
			deviceImages.add(sbiProjectDto.getDeviceImage2());
		}
		if (sbiProjectDto.getDeviceImage3() != null) {
			deviceImages.add(sbiProjectDto.getDeviceImage3());
		}
		if (sbiProjectDto.getDeviceImage4() != null) {
			deviceImages.add(sbiProjectDto.getDeviceImage4());
		}
		sbiProjectTable.setDeviceImages(deviceImages);
		return sbiProjectTable;
	}

	private SdkProjectTable getSdkProjectDetails(SdkProjectDto sdkProjectDto) {
		SdkProjectTable sdkProjectTable = new SdkProjectTable();
		sdkProjectTable.setProjectName(sdkProjectDto.getName());
		sdkProjectTable.setProjectType(sdkProjectDto.getProjectType());
		sdkProjectTable.setPurpose(sdkProjectDto.getPurpose());
		sdkProjectTable.setSpecVersion(sdkProjectDto.getSdkVersion());
		sdkProjectTable.setSdkHash(sdkProjectDto.getSdkHash());
		sdkProjectTable.setWebsite(sdkProjectDto.getWebsiteUrl());
		return sdkProjectTable;
	}

	private AbisProjectTable getAbisProjectDetails(AbisProjectDto abisProjectDto) {
		AbisProjectTable abisProjectTable = new AbisProjectTable();
		abisProjectTable.setProjectName(abisProjectDto.getName());
		abisProjectTable.setProjectType(abisProjectDto.getProjectType());
		abisProjectTable.setSpecVersion(abisProjectDto.getAbisVersion());
		abisProjectTable.setAbisHash(abisProjectDto.getAbisHash());
		abisProjectTable.setWebsite(abisProjectDto.getWebsiteUrl());
		return abisProjectTable;
	}

	private String getOrigin(String origin) {
		origin = origin.replace("https://", BLANK_STRING);
		origin = origin.replace("http://", BLANK_STRING);
		return origin;
	}

	private ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String testRunId) {
		ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponseDto = testRunService
				.getTestRunDetails(getPartnerId(), testRunId, false);
		return testRunDetailsResponseDto;
	}

	private List<TestRunTable> populateTestRunTable(List<TestCaseDto> testcasesList,
			TestRunDetailsResponseDto testRunDetailsResponseDto) {
		List<TestRunTable> testRunTable = new ArrayList<>();
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		for (TestCaseDto testcase : testcasesList) {
			TestRunTable item = new TestRunTable();
			String testCaseId = testcase.getTestId();
			String testCaseName = testcase.getTestName();
			if (testCaseName.contains("&")) {
				testCaseName = testCaseName.replace("&", "and");
			}
			item.setTestCaseId(testCaseId);
			item.setTestCaseName(testCaseName);
			boolean matchingTestCaseFound = false;
			String result = "";
			for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
				if (testCaseId.equals(testRunDetailsDto.getTestcaseId())) {
					matchingTestCaseFound = true;
					// for each method in a testcase, check the result
					if (AppConstants.SUCCESS.equals(testRunDetailsDto.getResultStatus())) {
						if (!AppConstants.FAILURE.equals(result)) {
							result = AppConstants.SUCCESS;
						}
					} else {
						result = AppConstants.FAILURE;
					}
				}
			}
			if (!matchingTestCaseFound) {
				item.setResultStatus(AppConstants.FAILURE);
			}
			item.setResultStatus(result);
			testRunTable.add(item);
		}
		if (testRunTable.size() > 0) {
			testRunTable.sort(Comparator.comparing(TestRunTable::getTestCaseId));
		}
		return testRunTable;
	}

	private String getTestRunExecutionTime(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		LocalDateTime testRunEndDt = testRunDetailsResponseDto.getExecutionDtimes();
		LocalDateTime testRunStartDt = testRunDetailsResponseDto.getRunDtimes();
		long milliSeconds = testRunStartDt.until(testRunEndDt, ChronoUnit.MILLIS);
		String timeDiffStr = String.format("%d minutes %d seconds", TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
				TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
		return timeDiffStr;

	}

	private List<TestCaseDto> getAllTestcases(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		ResponseWrapper<CollectionTestCasesResponseDto> testcasesForCollection = collectionsService
				.getTestCasesForCollection(getPartnerId(), testRunDetailsResponseDto.getCollectionId());
		List<TestCaseDto> testcasesList = testcasesForCollection.getResponse().getTestcases();
		return testcasesList;
	}

	private int countOfSuccessTestCases(List<TestCaseDto> testcasesList,
			TestRunDetailsResponseDto testRunDetailsResponseDto) {
		int passCount = 0;
		for (TestCaseDto testcase : testcasesList) {
			String testCaseId = testcase.getTestId();
			boolean matchingTestCaseFound = false;
			String result = "";
			for (TestRunDetailsDto testRunDetailsDto : testRunDetailsResponseDto.getTestRunDetailsList()) {
				if (testCaseId.equals(testRunDetailsDto.getTestcaseId())) {
					matchingTestCaseFound = true;
					// for each method in a testcase, check the result
					if (AppConstants.SUCCESS.equals(testRunDetailsDto.getResultStatus())) {
						if (!AppConstants.FAILURE.equals(result)) {
							result = AppConstants.SUCCESS;
						}
					} else {
						result = AppConstants.FAILURE;
					}
				}
			}
			if (matchingTestCaseFound && AppConstants.SUCCESS.equals(result)) {
				passCount++;
			}
		}
		return passCount;
	}

	private String getTestRunStartDt(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		LocalDateTime testRunStartDt = testRunDetailsResponseDto.getRunDtimes();
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
		return formatter.format(testRunStartDt);

	}

	private String getReportValidityDt(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		LocalDateTime testRunStartDt = testRunDetailsResponseDto.getRunDtimes();
		LocalDateTime reportValidityDt = testRunStartDt.plusMonths(reportExpiryPeriod);
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
		return formatter.format(reportValidityDt);

	}

	private String mergeVelocityTemplate(VelocityContext velocityContext, String templateName) throws Exception {
		VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
		StringWriter stringWriter = new StringWriter();
		engine.mergeTemplate("templates/" + templateName, StandardCharsets.UTF_8.name(), velocityContext, stringWriter);
		String mergedHtml = stringWriter.toString();
		log.info("sessionId", "idType", "id", "Merged Template successfully");
		return mergedHtml;
	}

	private ByteArrayResource convertHtmltToPdf(String mergedHtml) throws IOException {
		ITextRenderer renderer = new ITextRenderer();
		SharedContext sharedContext = renderer.getSharedContext();
		sharedContext.setPrint(true);
		sharedContext.setInteractive(false);
		renderer.setDocumentFromString(mergedHtml);
		renderer.layout();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		renderer.createPDF(outputStream);
		byte[] bytes = outputStream.toByteArray();
		ByteArrayResource resource = new ByteArrayResource(bytes);
		outputStream.close();
		log.info("sessionId", "idType", "id", "Converted html to pdf successfully");
		return resource;
	}

	private ResponseEntity<Resource> sendPdfResponse(ReportRequestDto requestDto, ByteArrayResource resource) {
		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + requestDto.getTestRunId() + ".pdf");
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
				.contentType(MediaType.APPLICATION_PDF).body(resource);
	}

	public ResponseWrapper<Boolean> isReportAlreadySubmitted(RequestWrapper<ReportRequestDto> requestWrapper) {
		ReportRequestDto requestDto = requestWrapper.getRequest();
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean isReportAlreadySubmitted = false;
		try {
			log.info("sessionId", "idType", "id", "Started isReportAlreadySubmitted processing");
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			String collectionId = requestDto.getCollectionId();
			logInput(requestDto, projectType, projectId);
			// validate the reportStatus
			ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
			pk.setPartnerId(getPartnerId());
			pk.setProjectId(projectId);
			pk.setCollectionId(collectionId);
			Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
			if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())) {
				if (!AppConstants.REPORT_STATUS_DRAFT.equals(optionalEntity.get().getReportStatus())) {
					isReportAlreadySubmitted = true;
				}
			} else {
				// no previous report data present
				isReportAlreadySubmitted = false;
			}
			responseWrapper.setResponse(Boolean.valueOf(isReportAlreadySubmitted));
		} catch (Exception ex) {
			log.info("sessionId", "idType", "id",
					"Exception in checkIfReportCanBeGenerated " + ex.getLocalizedMessage());
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In isReportAlreadySubmitted method of ReportGenerator Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.TOOLKIT_REPORT_ERR.getErrorCode();
			String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_ERR.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getPartnerReportId.toString());
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<List<ComplianceTestRunSummaryDto>> getReportList(boolean isAdmin, String reportStatus) {

		ResponseWrapper<List<ComplianceTestRunSummaryDto>> responseWrapper = new ResponseWrapper<>();

		List<ComplianceTestRunSummaryDto> responseList = new ArrayList<>();
		try {
			log.info("sessionId", "idType", "id", "Started getReportList processing");
			log.info("sessionId", "idType", "id", "isAdmin: " + isAdmin);
			log.info("sessionId", "idType", "id", "reportStatus: " + reportStatus);
			// validate the reportStatus
			if (isAdmin && !(AppConstants.REPORT_STATUS_REVIEW.equals(reportStatus)
					|| AppConstants.REPORT_STATUS_APPROVED.equals(reportStatus)
					|| AppConstants.REPORT_STATUS_REJECTED.equals(reportStatus))) {
				String errorCode = ToolkitErrorCodes.TOOLKIT_INVALID_REPORT_STATUS_ERR.getErrorCode();
				String errorMessage = ToolkitErrorCodes.TOOLKIT_INVALID_REPORT_STATUS_ERR.getErrorMessage() + " "
						+ reportStatus;
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			} else {
				// get the list of reports
				List<ComplianceReportSummaryEntity> listEntity = null;
				if (isAdmin) {
					listEntity = complianceTestRunSummaryRepository.findAllByReportStatus(reportStatus);
				} else {
					listEntity = complianceTestRunSummaryRepository.findAllBySubmittedReportsPartnerId(getPartnerId());
				}
				ObjectMapper objectMapper = getObjectMapper();
				for (ComplianceReportSummaryEntity respEntity : listEntity) {
					ComplianceTestRunSummaryDto complianceTestRunSummaryDto = (ComplianceTestRunSummaryDto) objectMapper
							.convertValue(respEntity, new TypeReference<ComplianceTestRunSummaryDto>() {
							});
					complianceTestRunSummaryDto.setProjectName(getProjectName(respEntity));
					responseList.add(complianceTestRunSummaryDto);
				}
			}
			responseWrapper.setResponse(responseList);
		} catch (Exception ex) {
			log.info("sessionId", "idType", "id", "Exception in getReportList " + ex.getLocalizedMessage());
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getReportList method of ReportGenerator Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.TOOLKIT_REPORT_GET_ERR.getErrorCode();
			String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_GET_ERR.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getAdminReportId.toString());
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private String getProjectName(ComplianceReportSummaryEntity respEntity) {
		String projectType = respEntity.getProjectType();
		String projectName = null;
		if (AppConstants.SBI.equals(projectType)) {
			projectName = sbiProjectRepository.getProjectNameById(respEntity.getProjectId(), respEntity.getPartnerId());
		}
		if (AppConstants.SDK.equals(projectType)) {
			projectName = sdkProjectRepository.getProjectNameById(respEntity.getProjectId(), respEntity.getPartnerId());
		}
		if (AppConstants.ABIS.equals(projectType)) {
			projectName = abisProjectRepository.getProjectNameById(respEntity.getProjectId(),
					respEntity.getPartnerId());
		}
		return projectName;
	}

	private String getCollectionName(String collectionId, String partnerId) {
		String collectionName = collectionsRepository.getCollectionNameById(collectionId, partnerId);
		return collectionName;
	}

	private ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = objectMapperConfig.objectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

	public ResponseEntity<?> getSubmittedReport(String partnerId, ReportRequestDto requestDto,
			boolean ignoreTestRunId) {
		try {
			log.info("sessionId", "idType", "id", "Started getSubmittedReport processing");
			log.info("sessionId", "idType", "id", "partnerId: " + partnerId);
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			String collectionId = requestDto.getCollectionId();
			String testRunId = requestDto.getTestRunId();
			logInput(requestDto, projectType, projectId);
			// 1. get the report data
			ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
			pk.setPartnerId(partnerId);
			pk.setProjectId(projectId);
			pk.setCollectionId(collectionId);
			Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
			if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())
					&& ((!ignoreTestRunId && testRunId != null && testRunId.equals(optionalEntity.get().getRunId())))
					|| ignoreTestRunId) {
				String reportStatus = optionalEntity.get().getReportStatus();
				if (!AppConstants.REPORT_STATUS_DRAFT.equals(reportStatus)) {
					log.info("sessionId", "idType", "id", "report data is available in DB");
					String reportDateEncoded = optionalEntity.get().getReportDataJson();
					String reportDataDecoded = StringUtil.base64Decode(reportDateEncoded);
					ReportDataDto reportDataDto = (ReportDataDto) getObjectMapper().readValue(reportDataDecoded,
							ReportDataDto.class);
					// 2. Populate all attributes in velocity template VelocityContext
					VelocityContext velocityContext = new VelocityContext();
					velocityContext.put(PROJECT_TYPE, reportDataDto.getProjectType());
					if (!AppConstants.REPORT_STATUS_REVIEW.equals(reportStatus)) {
						velocityContext.put(STATUS_TEXT, reportStatus.toUpperCase());
						velocityContext.put("reviewedBy", optionalEntity.get().getUpdBy());
						DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
						velocityContext.put("reviewDt",
								formatter.format(optionalEntity.get().getApproveRejectDtimes()));
					} else {
						// report should say status as draft only
						velocityContext.put(STATUS_TEXT, AppConstants.REPORT_STATUS_DRAFT.toUpperCase());
					}
					velocityContext.put(ORIGIN_KEY, reportDataDto.getOrigin());
					velocityContext.put(PARTNER_DETAILS, reportDataDto.getPartnerDetails());
					if (ProjectTypes.SBI.getCode().equals(projectType)) {
						velocityContext.put(SBI_PROJECT_DETAILS_TABLE, reportDataDto.getSbiProjectDetailsTable());
					}
					if (ProjectTypes.SDK.getCode().equals(projectType)) {
						velocityContext.put(SDK_PROJECT_DETAILS_TABLE, reportDataDto.getSdkProjectDetailsTable());
					}
					if (ProjectTypes.ABIS.getCode().equals(projectType)) {
						velocityContext.put(ABIS_PROJECT_DETAILS_TABLE, reportDataDto.getAbisProjectDetailsTable());
					}
					velocityContext.put(COLLECTION_NAME, reportDataDto.getCollectionName());
					velocityContext.put(TEST_RUN_START_TIME, reportDataDto.getTestRunStartTime());
					velocityContext.put(REPORT_EXPIRY_PERIOD, reportDataDto.getReportExpiryPeriod());
					velocityContext.put(REPORT_VALIDITY_DATE, reportDataDto.getReportValidityDate());
					velocityContext.put(TEST_RUN_DETAILS_LIST, reportDataDto.getTestRunDetailsList());
					velocityContext.put(TIME_TAKEN_BY_TEST_RUN, reportDataDto.getTimeTakenByTestRun());
					velocityContext.put(TOTAL_TEST_CASES_COUNT, reportDataDto.getTotalTestCasesCount());
					velocityContext.put(COUNT_OF_PASSED_TEST_CASES, reportDataDto.getCountOfPassedTestCases());
					velocityContext.put(COUNT_OF_FAILED_TEST_CASES, reportDataDto.getCountOfFailedTestCases());
					if (reportDataDto.getBiometricScores() != null && reportDataDto.getBiometricType() != null) {
						velocityContext.put(AGE_GROUPS_STR, reportDataDto.getAgeGroups());
						velocityContext.put(OCCUPATIONS_STR, reportDataDto.getOccupations());
						velocityContext.put(RACES_STR, reportDataDto.getRaces());
						velocityContext.put(BIOMETRIC_TYPE, reportDataDto.getBiometricType());
						velocityContext.put(BIOMETRIC_SCORES, reportDataDto.getBiometricScores());
					}
					log.info("sessionId", "idType", "id", "Added all attributes in velocity template successfully");
					// 3. merge report data with template
					String mergedHtml = mergeVelocityTemplate(velocityContext, TEST_RUN_REPORT_VM);
					// 4. Covert the merged HTML to PDF
					ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
					// 5. Send PDF in response
					return sendPdfResponse(requestDto, resource);
				} else {
					return handleValidationErrors(requestDto,
							"Report Status is Draft hence it cannot be viewed unless submitted for Review. ");
				}
			} else {
				return handleValidationErrors(requestDto,
						"Report Data is not available. Try with correct values for partner id, project type, project id, collection id and test run id.");
			}
		} catch (Exception e) {
			log.info("sessionId", "idType", "id", "Exception in getSubmittedReport " + e.getLocalizedMessage());
			try {
				return handleValidationErrors(requestDto, e.getLocalizedMessage());
			} catch (Exception e1) {
				return ResponseEntity.noContent().build();
			}
		}

	}

	public ResponseWrapper<ComplianceTestRunSummaryDto> updateReportStatus(String partnerId,
			RequestWrapper<ReportRequestDto> requestWrapper, String oldStatus, String newStatus) {
		ReportRequestDto requestDto = requestWrapper.getRequest();
		ResponseWrapper<ComplianceTestRunSummaryDto> responseWrapper = new ResponseWrapper<>();
		ComplianceTestRunSummaryDto complianceTestRunSummaryDto = new ComplianceTestRunSummaryDto();
		try {
			log.info("sessionId", "idType", "id", "Started updateReportStatus processing");
			log.info("sessionId", "idType", "id", "partnerId: " + partnerId);
			log.info("sessionId", "idType", "id", "oldStatus: " + oldStatus);
			log.info("sessionId", "idType", "id", "newStatus: " + newStatus);

			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			String collectionId = requestDto.getCollectionId();
			String testRunId = requestDto.getTestRunId();
			logInput(requestDto, projectType, projectId);
			// 1. get the report data
			ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
			pk.setPartnerId(partnerId);
			pk.setProjectId(projectId);
			pk.setCollectionId(collectionId);
			Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
			if (optionalEntity.isPresent() && testRunId.equals(optionalEntity.get().getRunId())
					&& projectType.equals(optionalEntity.get().getProjectType())) {
				ComplianceTestRunSummaryEntity entity = optionalEntity.get();
				if (oldStatus.equals(entity.getReportStatus())) {
					log.info("sessionId", "idType", "id", "report with status: " + oldStatus + " is available in DB");
					LocalDateTime nowDate = LocalDateTime.now();
					entity.setReportStatus(newStatus);
					if (AppConstants.REPORT_STATUS_REVIEW.equals(newStatus)) {
						entity.setPartnerComments(requestDto.getPartnerComments());
						entity.setReviewDtimes(nowDate);
					}
					if (AppConstants.REPORT_STATUS_APPROVED.equals(newStatus)
							|| AppConstants.REPORT_STATUS_REJECTED.equals(newStatus)) {
						entity.setAdminComments(requestDto.getAdminComments());
						entity.setApproveRejectDtimes(nowDate);
					}
					entity.setUpdBy(this.getUserBy());
					entity.setUpdDtimes(nowDate);
					ComplianceTestRunSummaryEntity respEntity = complianceTestRunSummaryRepository.save(entity);
					complianceTestRunSummaryDto = (ComplianceTestRunSummaryDto) getObjectMapper()
							.convertValue(respEntity, new TypeReference<ComplianceTestRunSummaryDto>() {
							});
					responseWrapper.setResponse(complianceTestRunSummaryDto);
				} else {
					String errorCode = ToolkitErrorCodes.TOOLKIT_REPORT_STATUS_INVALID_ERR.getErrorCode();
					String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_STATUS_INVALID_ERR.getErrorMessage() + " - "
							+ entity.getReportStatus();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
				}
			} else {
				String errorCode = ToolkitErrorCodes.TOOLKIT_REPORT_NOT_AVAILABLE_ERR.getErrorCode();
				String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_NOT_AVAILABLE_ERR.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			}
		} catch (Exception ex) {
			log.info("sessionId", "idType", "id", "Exception in updateReportStatus " + ex.getLocalizedMessage());
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateReportStatus method of ReportGenerator Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.TOOLKIT_REPORT_STATUS_UPDATE_ERR.getErrorCode();
			String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_STATUS_UPDATE_ERR.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(requestWrapper.getId());
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}