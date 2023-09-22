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
import java.util.concurrent.TimeUnit;

import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
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

	private static final String BLANK_STRING = "";

	private static final String VALIDATION_ERR_TEST_DATA = "Only MOSIP_DEFAULT test data should be used with the Test Run to generate the report. It is failing for testcase :";

	private static final String VALIDATION_ERR_DEVICE_INFO = "Make, model and serial number of the device should be same in all the testcases. It is failing for testcase :";

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
	private CollectionsService collectionsService;

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
			log.info("sessionId", "idType", "id", "Started createReport processing");
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			log.info("sessionId", "idType", "id", "projectType {}", projectType);
			log.info("sessionId", "idType", "id", "projectId {}", projectId);
			log.info("sessionId", "idType", "id", "testRunId {}", requestDto.getTestRunId());
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
				String invalidTestCaseId = this.validateDeviceInfo(testRunDetailsResponseDto, sbiProjectTable);
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
					sdkProjectDto, abisProjectDto, origin, projectType, projectId, sbiProjectTable);
			// 5. Merge velocity HTML template with all attributes
			String mergedHtml = mergeVelocityTemplate(velocityContext, "testRunReport.vm");
			// 6. Covert the merged HTML to PDF
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			// 7. Send PDF in response
			return sendPdfResponse(requestDto, resource);

		} catch (Exception e) {
			log.info("sessionId", "idType", "id", "Exception in createReport {}", e.getLocalizedMessage());
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
		velocityContext.put("totalTestCasesCount", getTotalTestcases(testRunDetailsResponseDto).size());
		velocityContext.put("countOfPassedTestCases", getCountOfPassedTestCases(testRunDetailsResponseDto));
		velocityContext.put("countOfFailedTestCases", getCountOfFailedTestCases(testRunDetailsResponseDto));
		log.info("sessionId", "idType", "id", "Added all attributes in velocity template successfully");
		return velocityContext;
	}

	private String validateTestDataSource(TestRunDetailsResponseDto testRunDetailsResponseDto, String projectType) {
		
		String invalidTestCaseId = BLANK_STRING;
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		boolean validationResult = true;
		List<String> ignoreSdkTestcaseList = Arrays.asList(ignoreSdkTestcases.split(","));
		List<String> ignoreAbisTestcaseList = Arrays.asList(ignoreAbisTestcases.split(","));
		for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
			if (ProjectTypes.SDK.getCode().equals(projectType)
					&& !ignoreSdkTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("sessionId", "idType", "id", "testdata validation failed for {}", testRunDetailsDto.getTestcaseId());
				validationResult = false;
			}
			if (ProjectTypes.ABIS.getCode().equals(projectType)
					&& !ignoreAbisTestcaseList.contains(testRunDetailsDto.getTestcaseId())
					&& !AppConstants.MOSIP_DEFAULT.equals(testRunDetailsDto.getTestDataSource())) {
				log.info("sessionId", "idType", "id", "testdata validation failed for {}", testRunDetailsDto.getTestcaseId());
				validationResult = false;
			}
			if (!validationResult) {
				invalidTestCaseId = testRunDetailsDto.getTestcaseId();
				break;
			}
		}
		
		return invalidTestCaseId;
	}

	private String validateDeviceInfo(TestRunDetailsResponseDto testRunDetailsResponseDto,
			SbiProjectTable sbiProjectTable) {
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		String invalidTestCaseId = BLANK_STRING;
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
				invalidTestCaseId = testRunDetailsDto.getTestcaseId();
				break;
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
				.getTestRunDetails(testRunId);
		return testRunDetailsResponseDto;
	}

	private List<TestRunTable> populateTestRunTable(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		List<TestRunTable> testRunTable = new ArrayList<>();
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		List<TestCaseDto> testcasesList = getTotalTestcases(testRunDetailsResponseDto);
		for(TestCaseDto testcase: testcasesList) {
			TestRunTable item = new TestRunTable();
			String testCaseId = testcase.getTestId();
			String testCaseName = testcase.getTestName();
			if (testCaseName.contains("&")) {
				testCaseName = testCaseName.replace("&", "and");
			}
			item.setTestCaseId(testCaseId);
			item.setTestCaseName(testCaseName);
			String result = "";
			for(TestRunDetailsDto testRunDetailsDto: testRunDetailsList) {
				if (testCaseId.equals(testRunDetailsDto.getTestcaseId())) {
					result = testRunDetailsDto.getResultStatus();
					break;
				}
			}
			if (result == "") {
				item.setResultStatus(AppConstants.FAILURE);
			} else {
				item.setResultStatus(result);
			}
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

	private List<TestCaseDto> getTotalTestcases(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		ResponseWrapper<CollectionTestCasesResponseDto> testcasesForCollection = collectionsService.getTestCasesForCollection(
				testRunDetailsResponseDto.getCollectionId());
		List<TestCaseDto> testcasesList = testcasesForCollection.getResponse().getTestcases();
		return testcasesList;
	}

	private int getCountOfPassedTestCases(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		int passCount = 0;
		List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponseDto.getTestRunDetailsList();
		for(TestRunDetailsDto item: testRunDetailsList) {
			if(item.getResultStatus().equalsIgnoreCase("success")) {
				passCount++;
			}
		}
		return passCount;
	}

	private int getCountOfFailedTestCases(TestRunDetailsResponseDto testRunDetailsResponseDto) {
		int passCount = getCountOfPassedTestCases(testRunDetailsResponseDto);
		int totalTestcaseCount = getTotalTestcases(testRunDetailsResponseDto).size();
		return totalTestcaseCount-passCount;
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
}