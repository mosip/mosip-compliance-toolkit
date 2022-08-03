package io.mosip.compliance.toolkit.validators;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

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
		// Read File Content
		Resource resource = resourceLoader.getResource("classpath:" + fileName);
		InputStream inputStream = resource.getInputStream();
		try (Reader reader = new InputStreamReader(inputStream, UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
	}
}
