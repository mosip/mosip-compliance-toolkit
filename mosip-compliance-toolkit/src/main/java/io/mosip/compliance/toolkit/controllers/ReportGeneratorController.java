package io.mosip.compliance.toolkit.controllers;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * This controller class defines the endpoints for all ABIS datashare.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@RestController
public class ReportGeneratorController {

	private Logger log = LoggerConfiguration.logConfig(ReportGeneratorController.class);

	@GetMapping(value = "/createReport/{testRunId}")
	public ResponseEntity<Resource> createReport(@PathVariable String testRunId) {
		
		try {
			//1. Populate all attributes
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("title", "my title");
			velocityContext.put("body", "my text");
			
			//2. Merge velocity HTML template with all attributes
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
		} catch (Exception e) {
			log.info("Exception in createReport {}", e.getLocalizedMessage());
		}
		return ResponseEntity.noContent().build();
	}
}