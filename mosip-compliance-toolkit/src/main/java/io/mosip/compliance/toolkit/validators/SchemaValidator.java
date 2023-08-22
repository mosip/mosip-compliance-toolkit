package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator extends ToolkitValidator {

	private static final String JSON_EXT = ".json";

	private Logger log = LoggerConfiguration.logConfig(SchemaValidator.class);
	
	@Autowired
	TestCasesService service;
	
	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		try {
			String methodResponseJson = responseDto.getMethodResponse();
			String type = responseDto.getTestCaseType().toLowerCase();
			String version = responseDto.getSpecVersion();
			String responseSchemaJson = getSchemaJson(type, version, responseDto.getResponseSchema() + JSON_EXT);
			return service.validateJsonWithSchema(methodResponseJson, responseSchemaJson);
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SchemaValidator - " + e.getMessage());
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
	}
}
