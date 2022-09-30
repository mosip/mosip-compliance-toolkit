package io.mosip.compliance.toolkit.service;

import java.io.InputStream;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AppConstants;

@Service
public class ResourceCacheService {
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	@Cacheable(cacheNames = "schemas", key = "{#type, #fileName}")
	public InputStream getSchema(String type, String fileName) {
		InputStream inputStream = null;
		String container = AppConstants.SCHEMAS.toLowerCase();
		container += (Objects.nonNull(type) ? ("/" + type) : "");
		if (existsInObjectStore(container, fileName)) {
			inputStream = getFromObjectStore(container, fileName);
		}
		return inputStream;
	}

	@CacheEvict(cacheNames = "schemas", key = "{#type, #fileName}")
	public boolean putSchema(String type, String fileName, InputStream inputStream) {
		String container = AppConstants.SCHEMAS.toLowerCase();
		container += (Objects.nonNull(type) ? ("/" + type) : "");
		return putInObjectStore(container, fileName, inputStream);
	}

	private boolean existsInObjectStore(String container, String objectName) {
		return objectStore.exists(objectStoreAccountName, container, null, null, objectName);
	}

	private InputStream getFromObjectStore(String container, String objectName) {
		return objectStore.getObject(objectStoreAccountName, container, null, null, objectName);
	}

	private boolean putInObjectStore(String container, String objectName, InputStream data) {
		return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
	}
}
