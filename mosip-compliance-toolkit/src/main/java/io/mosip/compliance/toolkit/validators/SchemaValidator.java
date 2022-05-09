package io.mosip.compliance.toolkit.validators;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.ResponseValidateDto;
import io.mosip.compliance.toolkit.dto.ValidationResponseDto;
import io.mosip.compliance.toolkit.service.TestCasesService;

@Component
public class SchemaValidator implements BaseValidator {

	@Autowired
	TestCasesService service;

	@Override
	public ValidationResponseDto validateResponse(ResponseValidateDto responseDto) {
		try {
			String methodResponseJson = responseDto.getMethodResponse();
			try {
				File schemaJsonFile = ResourceUtils
						.getFile("classpath:schemas/" + responseDto.getTestCaseType().toLowerCase() + "/"
								+ responseDto.getResponseSchema() + ".json");
				// Read File Content
				String responseSchemaJson = new String(Files.readAllBytes(schemaJsonFile.toPath()));
				return service.validateJsonWithSchema(methodResponseJson, responseSchemaJson);
			} catch (Exception ex) {
				System.out.println("exception occured: " + ex.getLocalizedMessage());
				throw new RuntimeException(ex);
				// TODO: handle exception
			}
		} catch (Exception e) {
			ValidationResponseDto validationResponseDto = new ValidationResponseDto();
			validationResponseDto.setStatus(AppConstants.FAILURE);
			validationResponseDto.setDescription(e.getLocalizedMessage());
			return validationResponseDto;
		}
	}
}
