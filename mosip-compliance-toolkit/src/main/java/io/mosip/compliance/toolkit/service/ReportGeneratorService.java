package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
				// 1. Populate all attributes
				VelocityContext velocityContext = new VelocityContext();
				velocityContext.put("testRunDetailsList", testRunTable);
				// 2. Merge velocity HTML template with all attributes
				VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
				StringWriter stringWriter = new StringWriter();
				engine.mergeTemplate("templates/testRunReport.vm", StandardCharsets.UTF_8.name(), velocityContext,
						stringWriter);
				String parsedHtml = stringWriter.toString();
				log.info("Merged Template {}", parsedHtml);

				// 2. Covert the merged HTML to PDF
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				ITextRenderer renderer = new ITextRenderer();
				SharedContext sharedContext = renderer.getSharedContext();
				sharedContext.setPrint(true);
				sharedContext.setInteractive(false);
				renderer.setDocumentFromString(parsedHtml);
				renderer.layout();
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