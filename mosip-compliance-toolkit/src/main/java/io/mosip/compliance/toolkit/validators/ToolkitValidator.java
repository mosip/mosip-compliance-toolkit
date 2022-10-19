package io.mosip.compliance.toolkit.validators;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.service.ResourceCacheService;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;

public abstract class ToolkitValidator implements BaseValidator {

	@Autowired
	public ObjectMapperConfig objectMapperConfig;

	@Autowired
	ResourceLoader resourceLoader;
	
	@Autowired
	private ResourceCacheService resourceCacheService;

	protected String getSchemaJson(String type, String version, String fileName) throws Exception {
		// Read File Content
		String schemaResponse = resourceCacheService.getSchema(type, version, fileName);
		if(Objects.nonNull(schemaResponse)) {
			return schemaResponse;
		}else {
			throw new ToolkitException(ToolkitErrorCodes.OBJECT_STORE_SCHEMA_NOT_AVAILABLE.getErrorCode(),
					ToolkitErrorCodes.OBJECT_STORE_SCHEMA_NOT_AVAILABLE.getErrorMessage());
		}
	}
}
