package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayOutputStream;
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
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.compliance.toolkit.dto.report.TestRunTable;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ReportGeneratorService {

	private Logger log = LoggerConfiguration.logConfig(ReportGeneratorService.class);

	@Autowired
	private TestRunService testRunService;

	@Autowired
	private TestCasesService testCaseService;

	@Autowired
	private ResourceLoader resourceLoader;

	public ResponseEntity<Resource> createReport(String testRunId, String origin) {
		try {
			// Get the test run details
			ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = getTestRunDetails(testRunId);
			if (testRunDetailsResponse.getErrors().size() == 0) {

				// 1. Populate all attributes
				VelocityContext velocityContext = new VelocityContext();
				origin = origin.replace("https://", "");
				origin = origin.replace("http://", "");
				velocityContext.put("origin", origin);
				velocityContext.put("testRunStartTime", getTestRunStartDt(testRunDetailsResponse));
				velocityContext.put("testRunDetailsList", populateTestRubTable(testRunDetailsResponse));
				velocityContext.put("timeTakenByTestRun", getTestRunExecutionTime(testRunDetailsResponse));
				// 2. Merge velocity HTML template with all attributes
				VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
				StringWriter stringWriter = new StringWriter();
				engine.mergeTemplate("templates/testRunReport.vm", StandardCharsets.UTF_8.name(), velocityContext,
						stringWriter);
				String mergedHtml = stringWriter.toString();
				log.info("Merged Template {}", mergedHtml);

				// 2. Covert the merged HTML to PDF
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

				// 3. Send PDF in response
				HttpHeaders header = new HttpHeaders();
				header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + testRunId + ".pdf");
				header.add("Cache-Control", "no-cache, no-store, must-revalidate");
				header.add("Pragma", "no-cache");
				header.add("Expires", "0");
				return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
						.contentType(MediaType.APPLICATION_PDF).body(resource);

			}
		} catch (Exception e) {
			log.info("Exception in createReport {}", e.getLocalizedMessage());
		}
		return ResponseEntity.noContent().build();
	}

	private ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String testRunId) {
		ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = testRunService.getTestRunDetails(testRunId);
		return testRunDetailsResponse;
	}

	/*
	private String getLogoBase64Img() throws Exception {
		String logoText = null;
		InputStream inputStream = null;
		try {
			String logoFilePath = "classpath:templates/logo.png";
			File logoFile = ResourceUtils.getFile(logoFilePath);
			inputStream = new FileInputStream(logoFile);
			logoText = Base64.encodeBase64String(inputStream.readAllBytes());
			log.info("logoText {}", logoText);
		} catch(Exception e) {
			throw e;
		} finally {
			if (inputStream != null) {
				inputStream.close();	
			}
		}
		return logoText;
	}*/

	private List<TestRunTable> populateTestRubTable(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		List<TestRunTable> testRunTable = new ArrayList<>();
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
		return testRunTable;
	}

	private String getTestRunExecutionTime(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		LocalDateTime testRunEndDt = testRunDetailsResponse.getResponse().getExecutionDtimes();
		LocalDateTime testRunStartDt = testRunDetailsResponse.getResponse().getRunDtimes();
		long milliSeconds = testRunStartDt.until(testRunEndDt, ChronoUnit.MILLIS);
		String timeDiffStr = String.format("%d minutes %d seconds", TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
				TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
		return timeDiffStr;
	}

	private String getTestRunStartDt(ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse) {
		LocalDateTime testRunStartDt = testRunDetailsResponse.getResponse().getRunDtimes();
		DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
		return formatter.format(testRunStartDt);
	}
}