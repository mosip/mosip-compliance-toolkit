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
import java.util.List;
import java.util.concurrent.TimeUnit;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.report.AbisProjectTable;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto.Partner;
import io.mosip.compliance.toolkit.dto.report.PartnerTable;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.report.SbiProjectTable;
import io.mosip.compliance.toolkit.dto.report.SdkProjectTable;
import io.mosip.compliance.toolkit.dto.report.TestRunTable;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ReportGeneratorService {

	private static final String VALIDATION_ERR_TEST_DATA = "Only MOSIP_DEFAULT test data should be used with the Test Run to generate the report.";

	private static final String VALIDATION_ERR_DEVICE_INFO = "Make, model and serial number of the device should be same in all the testcases.";

	private Logger log = LoggerConfiguration.logConfig(ReportGeneratorService.class);

	@Autowired
	private TestRunService testRunService;

	@Autowired
	private SbiProjectService sbiProjectService;

	@Autowired
	private SdkProjectService sdkProjectService;

	@Autowired
	private AbisProjectService abisProjectService;

	@Autowired
	private TestCasesService testCaseService;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@Autowired
	private PartnerManagerHelper partnerManagerHelper;

	@Value("${mosip.toolkit.report.expiryperiod.in.months}")
	private int reportExpiryPeriod;

	@Value("${mosip.toolkit.api.id.create.report.post}")
	private String postCreateReport;

	@Value("${mosip.toolkit.sdk.testcases.ignore.list}")
	private String ignoreSdkTestcases;

	@Value("${mosip.toolkit.abis.testcases.ignore.list}")
	private String ignoreAbisTestcases;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseEntity<?> createReport(ReportRequestDto requestDto, String origin) {
		try {
			log.info("Started createReport processing");
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			log.info("projectType {}", projectType);
			log.info("projectId {}", projectId);
			log.info("testRunId {}", requestDto.getTestRunId());
			// 1. get the test run details
			ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = getTestRunDetails(
					requestDto.getTestRunId());
			ResponseEntity<Resource> errResource = handleServiceErrors(requestDto, testRunDetailsResponse.getErrors());
			if (errResource != null) {
				return errResource;
			}
			TestRunDetailsResponseDto testRunDetailsResponseDto = testRunDetailsResponse.getResponse();
			// 2. Validate that report can be generated
			SbiProjectTable sbiProjectTable = new SbiProjectTable();
			if (ProjectTypes.SBI.getCode().equals(projectType)) {
				boolean validationResult = this.validateDeviceInfo(testRunDetailsResponseDto, sbiProjectTable);
				if (!validationResult) {
					return handleValidationErrors(requestDto, VALIDATION_ERR_DEVICE_INFO);
				}
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType) || ProjectTypes.SDK.getCode().equals(projectType)) {
				boolean validationResult = this.validateTestDataSource(testRunDetailsResponseDto, projectType);
				if (!validationResult) {
					return handleValidationErrors(requestDto, VALIDATION_ERR_TEST_DATA);
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
					sdkProjectDto, abisProjectDto, origin, projectType, projectId, sbiProjectTable);
			// 5. Merge velocity HTML template with all attributes
			String mergedHtml = mergeVelocityTemplate(velocityContext, "testRunReport.vm");
			// 6. Covert the merged HTML to PDF
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			// 7. Send PDF in response
			return sendPdfResponse(requestDto, resource);

		} catch (Exception e) {
			log.info("Exception in createReport {}", e.getLocalizedMessage());
			try {
				return handleValidationErrors(requestDto, e.getLocalizedMessage());
			} catch (Exception e1) {
				return ResponseEntity.noContent().build();
			}
		}

	}

	private ResponseEntity<Resource> handleServiceErrors(ReportRequestDto requestDto, List<ServiceError> serviceErrors)
			throws Exception, IOException {
		ResponseEntity<Resource> errResource = null;
		if (serviceErrors != null && serviceErrors.size() > 0) {
			String err = serviceErrors.get(0).getMessage();
			String errorMessage = ToolkitErrorCodes.TOOLKIT_REPORT_ERR.getErrorMessage() + " - " + err;
			log.error("sessionId", "idType", "id", "In createReport method - " + err);
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
		log.error("sessionId", "idType", "id", "In createReport method - " + message);
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("error", errorMessage);
		String mergedHtml = mergeVelocityTemplate(velocityContext, "errTestRunReport.vm");
		ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
		ResponseEntity<Resource> errResource = sendPdfResponse(requestDto, resource);
		return errResource;
	}

	private VelocityContext populateVelocityAttributes(TestRunDetailsResponseDto testRunDetailsResponseDto,
			SbiProjectDto sbiProjectDto, SdkProjectDto sdkProjectDto, AbisProjectDto abisProjectDto, String origin,
			String projectType, String projectId, SbiProjectTable sbiProjectTable) throws Exception {

		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("projectType", projectType);
		velocityContext.put("origin", getOrigin(origin));
		velocityContext.put("partnerDetails", getPartnerDetails(getPartnerId()));
		if (ProjectTypes.SBI.getCode().equals(projectType)) {
			velocityContext.put("sbiProjectDetailsTable", getSbiProjectDetails(sbiProjectDto,
					testRunDetailsResponseDto.getTestRunDetailsList(), sbiProjectTable));
		}
		if (ProjectTypes.SDK.getCode().equals(projectType)) {
			velocityContext.put("sdkProjectDetailsTable", getSdkProjectDetails(sdkProjectDto));
		}
		if (ProjectTypes.ABIS.getCode().equals(projectType)) {
			velocityContext.put("abisProjectDetailsTable", getAbisProjectDetails(abisProjectDto));
		}

		velocityContext.put("testRunStartTime", getTestRunStartDt(testRunDetailsResponseDto));
		velocityContext.put("reportExpiryPeriod", reportExpiryPeriod);
		velocityContext.put("reportValidityDate", getReportValidityDt(testRunDetailsResponseDto));
		velocityContext.put("testRunDetailsList", populateTestRunTable(testRunDetailsResponseDto));
		velocityContext.put("timeTakenByTestRun", getTestRunExecutionTime(testRunDetailsResponseDto));
		log.info("Added all attributes in velocity template successfully");
		return velocityContext;
	}

	private boolean validateTestDataSource(TestRunDetailsResponseDto testRunDetailsResponseDto, String projectType) {
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		boolean validationResult = true;
		List<String> ignoreSdkTestcaseList = Arrays.asList(ignoreSdkTestcases.split(","));
		List<String> ignoreAbisTestcaseList = Arrays.asList(ignoreAbisTestcases.split(","));
		for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
			if (ProjectTypes.SDK.getCode().equals(projectType)
					&& !ignoreSdkTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("testdata validation failed for {}", testRunDetailsDto.getTestcaseId());
				validationResult = false;
				break;
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType)
					&& !ignoreAbisTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("testdata validation failed for {}", testRunDetailsDto.getTestcaseId());
				validationResult = false;
				break;
			}
		}
		return validationResult;
	}

	private boolean validateDeviceInfo(TestRunDetailsResponseDto testRunDetailsResponseDto,
			SbiProjectTable sbiProjectTable) {
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		boolean validationResult = true;
		for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
			if (validationResult) {
				// check if the method is "discover"
				try {
					ArrayNode discoverRespArr = (ArrayNode) objectMapperConfig.objectMapper()
							.readValue(testRunDetailsDto.getMethodResponse(), ArrayNode.class);
					if (discoverRespArr != null && discoverRespArr.size() > 0) {
						ObjectNode discoverResp = (ObjectNode) discoverRespArr.get(0);
						validationResult = validateDeviceMakeModelSerialNo(sbiProjectTable, validationResult,
								discoverResp);
					}
				} catch (Exception ex) {
					// ignore since method may not be "discover"
				}
			}
			if (validationResult) {
				// check if the method is "deviceInfo"
				try {
					ArrayNode deviceInfoRespArr = (ArrayNode) objectMapperConfig.objectMapper()
							.readValue(testRunDetailsDto.getMethodResponse(), ArrayNode.class);
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
			if (validationResult) {
				// check if the method is "capture" or "rcapture"
				try {
					ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
							.readValue(testRunDetailsDto.getMethodResponse(), ObjectNode.class);
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
				break;
			}
		}
		log.info("validateDeviceInfo, validationResult: {}", validationResult);
		return validationResult;
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
		io.restassured.response.Response partnerResp = partnerManagerHelper.getPartnerDetails(projectId);
		PartnerDetailsDto partnerDetailsDto = objectMapperConfig.objectMapper()
				.readValue(partnerResp.getBody().asString(), PartnerDetailsDto.class);
		if (partnerDetailsDto.getErrors().size() == 0) {
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
		String dummyImg = "data:image/jpeg;base64,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";
		List<String> deviceImages = new ArrayList<>();
		deviceImages.add(dummyImg);
		deviceImages.add(dummyImg);
		deviceImages.add(dummyImg);
		deviceImages.add(dummyImg);
		deviceImages.add(dummyImg);
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
		origin = origin.replace("https://", "");
		origin = origin.replace("http://", "");
		return origin;
	}

	private ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String testRunId) {
		ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponseDto = testRunService
				.getTestRunDetails(testRunId);
		return testRunDetailsResponseDto;
	}

	private List<TestRunTable> populateTestRunTable(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		List<TestRunTable> testRunTable = new ArrayList<>();
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
			TestRunTable item = new TestRunTable();
			String testCaseId = testRunDetailsDto.getTestcaseId();
			ResponseWrapper<TestCaseDto> testCaseDto = testCaseService.getTestCaseById(testCaseId);
			String testCaseName = testCaseDto.getResponse().getTestName();
			item.setTestCaseId(testCaseId);
			if (testCaseName.contains("&")) {
				testCaseName = testCaseName.replace("&", "and");
			}
			item.setTestCaseName(testCaseName);
			item.setResultStatus(testRunDetailsDto.getResultStatus());
			testRunTable.add(item);
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
		log.info("Merged Template successfully");
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
		log.info("Converted html to pdf successfully");
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
}