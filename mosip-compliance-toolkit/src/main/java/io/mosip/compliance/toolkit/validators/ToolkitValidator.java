package io.mosip.compliance.toolkit.validators;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;

public abstract class ToolkitValidator implements BaseValidator {

	@Autowired
	public ObjectMapperConfig objectMapperConfig;

	@Autowired
	ResourceLoader resourceLoader;
	
	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;
	
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	protected String getSchemaJson(String container, String fileName) throws Exception {
		// Read File Content
		if (isObjectExistInObjectStore(container, fileName)) {
			InputStream inputStream = getObjectFromObjectStore(container, fileName);
			try (Reader reader = new InputStreamReader(inputStream, UTF_8)) {
				return FileCopyUtils.copyToString(reader);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new ToolkitException(ToolkitErrorCodes.OBJECT_STORE_SCHEMA_NOT_AVAILABLE.getErrorCode(),
					ToolkitErrorCodes.OBJECT_STORE_SCHEMA_NOT_AVAILABLE.getErrorMessage());
		}
	}
	
	private boolean isObjectExistInObjectStore(String container, String objectName) {
		return objectStore.exists(objectStoreAccountName, container, null, null, objectName);
	}
	
	private InputStream getObjectFromObjectStore(String container, String objectName) {
		return objectStore.getObject(objectStoreAccountName, container, null, null, objectName);
	}
}
