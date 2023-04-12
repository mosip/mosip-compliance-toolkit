package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator extends ToolkitValidator {

	private static final String JSON_EXT = ".json";
	
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
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey("SCHEMA_VALIDATOR_002");
			return validationResultDto;
		}
	}
}
