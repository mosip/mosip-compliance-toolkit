package io.mosip.compliance.toolkit.validators;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ToolkitValidator implements BaseValidator {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ResourceLoader resourceLoader;

	protected String getSchemaJson(String fileName) throws Exception {
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
