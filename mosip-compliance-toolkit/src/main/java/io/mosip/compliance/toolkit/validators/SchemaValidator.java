package io.mosip.compliance.toolkit.validators;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator implements BaseValidator {

	@Autowired
	TestCasesService service;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		try {
			String methodResponseJson = responseDto.getMethodResponse();
			File schemaJsonFile = ResourceUtils.getFile("classpath:schemas/"
					+ responseDto.getTestCaseType().toLowerCase() + "/" + responseDto.getResponseSchema() + ".json");
			// Read File Content
			String responseSchemaJson = new String(Files.readAllBytes(schemaJsonFile.toPath()));
			return service.validateJsonWithSchema(methodResponseJson, responseSchemaJson);
		} catch (Exception e) {
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
	}
}
