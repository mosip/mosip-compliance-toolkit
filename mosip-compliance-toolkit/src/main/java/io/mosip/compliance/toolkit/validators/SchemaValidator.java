package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator extends ToolkitValidator {

	@Autowired
	TestCasesService service;
	
	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		try {
			String methodResponseJson = responseDto.getMethodResponse();
			String container = "schemas/" + responseDto.getTestCaseType().toLowerCase();
			String responseSchemaJson = getSchemaJson(container, responseDto.getResponseSchema() + ".json");
			return service.validateJsonWithSchema(methodResponseJson, responseSchemaJson);
		} catch (Exception e) {
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
	}
}
