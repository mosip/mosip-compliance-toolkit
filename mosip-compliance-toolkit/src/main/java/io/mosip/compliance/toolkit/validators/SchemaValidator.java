package io.mosip.compliance.toolkit.validators;

import java.io.File;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator implements BaseValidator {

	@Autowired
	TestCasesService service;

	@Autowired
	ResourceLoader resourceLoader;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		try {
			String methodResponseJson = responseDto.getMethodResponse();
			String responseSchemaJson = getSchemaJson("schemas/" + responseDto.getTestCaseType().toLowerCase() + "/"
					+ responseDto.getResponseSchema() + ".json");
			return service.validateJsonWithSchema(methodResponseJson, responseSchemaJson);
		} catch (Exception e) {
			ValidationResultDto validationResultDto = new ValidationResultDto();
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
	}

	private String getSchemaJson(String fileName) throws Exception {
		// File file = ResourceUtils.getFile("classpath:schemas/testcase_schema.json");
		// Read File Content
		Resource res = resourceLoader.getResource("classpath:" + fileName);
		File file = res.getFile();
		return new String(Files.readAllBytes(file.toPath()));
	}
}
