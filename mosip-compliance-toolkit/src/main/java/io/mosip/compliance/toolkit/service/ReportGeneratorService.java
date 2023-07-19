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

	public ResponseEntity<Resource> createReport(String testRunId) {
		try {
			// Get the test run details
			ResponseWrapper<TestRunDetailsResponseDto> responseWrapper = testRunService.getTestRunDetails(testRunId);
			if (responseWrapper.getErrors().size() == 0) {

				List<TestRunTable> testRunTable = new ArrayList<>();
				List<TestRunDetailsDto> testRunDetailsList = responseWrapper.getResponse().getTestRunDetailsList();
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
				
				LocalDateTime testRunEndDt = responseWrapper.getResponse().getExecutionDtimes();
				LocalDateTime testRunStartDt = responseWrapper.getResponse().getRunDtimes();
				long milliSeconds = testRunStartDt.until(testRunEndDt, ChronoUnit.MILLIS);
				String timeDiffStr = String.format("%d minutes %d seconds", 
						  TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
						  TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - 
						  TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
				 DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

				// 1. Populate all attributes
				VelocityContext velocityContext = new VelocityContext();
				velocityContext.put("testRunStartTime", testRunStartDt.format(formatter));
				velocityContext.put("testRunDetailsList", testRunTable);
				velocityContext.put("timeTakenByTestRun", timeDiffStr);
				// 2. Merge velocity HTML template with all attributes
				VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
				StringWriter stringWriter = new StringWriter();
				engine.mergeTemplate("templates/testRunReport.vm", StandardCharsets.UTF_8.name(), velocityContext,
						stringWriter);
				String parsedHtml = stringWriter.toString();
				log.info("Merged Template {}", parsedHtml);

				// 2. Covert the merged HTML to PDF
				ITextRenderer renderer = new ITextRenderer();
				SharedContext sharedContext = renderer.getSharedContext();
				sharedContext.setPrint(true);
				sharedContext.setInteractive(false);
//				LineBreakingStrategy strategy = new LineBreakingStrategy() {
//					
//					@Override
//					public BreakPointsProvider getBreakPointsProvider(String text, String lang, CalculatedStyle style) {
//						// TODO Auto-generated method stub
//						return null;
//					}
//				};
//				sharedContext.setLineBreakingStrategy(strategy);
				renderer.setDocumentFromString(parsedHtml);
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
}