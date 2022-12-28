package io.mosip.compliance.toolkit.validators;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

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
