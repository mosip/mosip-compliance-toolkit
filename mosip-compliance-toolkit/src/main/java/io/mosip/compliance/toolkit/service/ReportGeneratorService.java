package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Base64;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.report.AbisProjectTable;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.report.SbiProjectTable;
import io.mosip.compliance.toolkit.dto.report.SdkProjectTable;
import io.mosip.compliance.toolkit.dto.report.TestRunTable;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ReportGeneratorService {

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

	public ResponseEntity<Resource> createReport(ReportRequestDto requestDto, String origin) {
		try {
			log.info("Started createReport processing");
			// Get the ProjectDetails
			String projectType = requestDto.getProjectType();
			String projectId = requestDto.getProjectId();
			log.info("projectType {}", projectType);
			// 1. TODO add all validations

			// 2. Populate all attributes
			VelocityContext velocityContext = populateVelocityAttributes(requestDto, origin, projectType, projectId);
			// 3. Merge velocity HTML template with all attributes
			String mergedHtml = mergeVelocityTemplate(velocityContext);
			// 4. Covert the merged HTML to PDF
			ByteArrayResource resource = convertHtmltToPdf(mergedHtml);
			// 5. Send PDF in response
			return sendPdfResponse(requestDto, resource);
		} catch (Exception e) {
			// TODO: err handling
			log.info("Exception in createReport {}", e.getLocalizedMessage());
		}
		return ResponseEntity.noContent().build();
	}

	private VelocityContext populateVelocityAttributes(ReportRequestDto requestDto, String origin, String projectType,
			String projectId) {
		// Get the test run details
		ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = getTestRunDetails(
				requestDto.getTestRunId());
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put("projectType", projectType);
		velocityContext.put("origin", getOrigin(origin));
		if (ProjectTypes.SBI.getCode().equals(projectType)) {
			velocityContext.put("sbiProjectDetailsTable", getSbiProjectDetails(projectId, testRunDetailsResponse));
		}
		if (ProjectTypes.SDK.getCode().equals(projectType)) {
			velocityContext.put("sdkProjectDetailsTable", getSdkProjectDetails(projectId));
		}
		if (ProjectTypes.ABIS.getCode().equals(projectType)) {
			velocityContext.put("abisProjectDetailsTable", getAbisProjectDetails(projectId));
		}

		velocityContext.put("testRunStartTime", getTestRunStartDt(testRunDetailsResponse));
		velocityContext.put("testRunDetailsList", populateTestRubTable(testRunDetailsResponse));
		velocityContext.put("timeTakenByTestRun", getTestRunExecutionTime(testRunDetailsResponse));
		log.info("Added all attributes in velocity template successfully");
		return velocityContext;
	}

	private SbiProjectTable getSbiProjectDetails(String projectId,
			ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		ResponseWrapper<SbiProjectDto> sbiProjectResponse = sbiProjectService.getSbiProject(projectId);
		SbiProjectTable sbiProjectTable = new SbiProjectTable();
		if (sbiProjectResponse.getErrors().size() == 0) {
			SbiProjectDto sbiProjectDto = sbiProjectResponse.getResponse();
			sbiProjectTable.setProjectName(sbiProjectDto.getName());
			sbiProjectTable.setProjectType(sbiProjectDto.getProjectType());
			sbiProjectTable.setPurpose(sbiProjectDto.getPurpose());
			sbiProjectTable.setSpecVersion(sbiProjectDto.getSbiVersion());
			// sbiProjectTable.setSbiHash("TODO");
			sbiProjectTable.setDeviceType(sbiProjectDto.getDeviceType());
			sbiProjectTable.setDeviceSubType(sbiProjectDto.getDeviceSubType());

			String dummyImg = "data:image/jpeg;base64,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";
			List<String> deviceImages = new ArrayList<>();
			deviceImages.add(dummyImg);
			deviceImages.add(dummyImg);
			deviceImages.add(dummyImg);
			deviceImages.add(dummyImg);
			deviceImages.add(dummyImg);
			sbiProjectTable.setDeviceImages(deviceImages);
			//set the device details
			try {
				List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponse.getResponse()
						.getTestRunDetailsList();
				for (TestRunDetailsDto testRunDetailsDto : testRunDetailsList) {
					ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
							.readValue(testRunDetailsDto.getMethodResponse(), ObjectNode.class);
					JsonNode arrBiometricNodes = methodResponse.get(AppConstants.BIOMETRICS);
					if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
						for (final JsonNode biometricNode : arrBiometricNodes) {
							JsonNode dataNode = biometricNode.get(AppConstants.DECODED_DATA);
							JsonNode digitalIdDecoded = dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA);
							String make = digitalIdDecoded.get(AppConstants.MAKE).asText();
							String model = digitalIdDecoded.get(AppConstants.MODEL).asText();
							String serialNo = digitalIdDecoded.get(AppConstants.SERIAL_NO).asText();
							String deviceProvider = digitalIdDecoded.get(AppConstants.DEVICE_PROVIDER).asText();
							String deviceProviderId = digitalIdDecoded.get(AppConstants.DEVICE_PROVIDER_ID).asText();
							sbiProjectTable.setDeviceMake(make);
							sbiProjectTable.setDeviceModel(model);
							sbiProjectTable.setDeviceSerialNo(serialNo);
							sbiProjectTable.setDeviceProvider(deviceProvider);
							sbiProjectTable.setDeviceProviderId(deviceProviderId);
							break;
						}
						break;
					}
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
		return sbiProjectTable;
	}

	private SdkProjectTable getSdkProjectDetails(String projectId) {
		ResponseWrapper<SdkProjectDto> sdkProjectResponse = sdkProjectService.getSdkProject(projectId);
		SdkProjectTable sdkProjectTable = new SdkProjectTable();
		if (sdkProjectResponse.getErrors().size() == 0) {
			SdkProjectDto sdkProjectDto = sdkProjectResponse.getResponse();
			sdkProjectTable.setProjectName(sdkProjectDto.getName());
			sdkProjectTable.setProjectType(sdkProjectDto.getProjectType());
			sdkProjectTable.setPurpose(sdkProjectDto.getPurpose());
			sdkProjectTable.setSpecVersion(sdkProjectDto.getSdkVersion());
			// sdkProjectTable.setSdkHash("TODO");
		}
		return sdkProjectTable;
	}

	private AbisProjectTable getAbisProjectDetails(String projectId) {
		ResponseWrapper<AbisProjectDto> abisProjectResponse = abisProjectService.getAbisProject(projectId);
		AbisProjectTable abisProjectTable = new AbisProjectTable();
		if (abisProjectResponse.getErrors().size() == 0) {
			AbisProjectDto abisProjectDto = abisProjectResponse.getResponse();
			abisProjectTable.setProjectName(abisProjectDto.getName());
			abisProjectTable.setProjectType(abisProjectDto.getProjectType());
			abisProjectTable.setSpecVersion(abisProjectDto.getAbisVersion());
			// abisProjectTable.setAbisHash("TODO");
		}
		return abisProjectTable;
	}

	private String getOrigin(String origin) {
		origin = origin.replace("https://", "");
		origin = origin.replace("http://", "");
		return origin;
	}

	private ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String testRunId) {
		ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = testRunService.getTestRunDetails(testRunId);
		return testRunDetailsResponse;
	}

	private String getLogoBase64Img() throws Exception {
		String logoText = null;
		InputStream inputStream = null;
		try {
			String logoFilePath = "classpath:templates/logo.png";
			File logoFile = ResourceUtils.getFile(logoFilePath);
			inputStream = new FileInputStream(logoFile);
			logoText = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
			log.info("logoText {}", logoText);
		} catch (Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return logoText;
	}

	private List<TestRunTable> populateTestRubTable(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		List<TestRunTable> testRunTable = new ArrayList<>();
		if (testRunDetailsResponse.getErrors().size() == 0) {
			List<TestRunDetailsDto> testRunDetailsList = testRunDetailsResponse.getResponse().getTestRunDetailsList();
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
		}
		return testRunTable;
	}

	private String getTestRunExecutionTime(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		if (testRunDetailsResponse.getErrors().size() == 0) {
			LocalDateTime testRunEndDt = testRunDetailsResponse.getResponse().getExecutionDtimes();
			LocalDateTime testRunStartDt = testRunDetailsResponse.getResponse().getRunDtimes();
			long milliSeconds = testRunStartDt.until(testRunEndDt, ChronoUnit.MILLIS);
			String timeDiffStr = String.format("%d minutes %d seconds", TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
					TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
			return timeDiffStr;
		}
		return "";
	}

	private String getTestRunStartDt(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		if (testRunDetailsResponse.getErrors().size() == 0) {
			LocalDateTime testRunStartDt = testRunDetailsResponse.getResponse().getRunDtimes();
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
			return formatter.format(testRunStartDt);
		}
		return "";
	}

	private String mergeVelocityTemplate(VelocityContext velocityContext) throws Exception {
		VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
		StringWriter stringWriter = new StringWriter();
		engine.mergeTemplate("templates/testRunReport.vm", StandardCharsets.UTF_8.name(), velocityContext,
				stringWriter);
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