package io.mosip.compliance.toolkit.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.validators.SignatureValidator;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class ResourceCacheService {
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	private Logger log = LoggerConfiguration.logConfig(ResourceCacheService.class);

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	@Cacheable(cacheNames = "schemas", key = "{#type, #version, #fileName}")
	public String getSchema(String type, String version, String fileName) throws Exception {
		System.out.println("getSchema");
		try {
			String schemaResponse = null;
			String container = AppConstants.SCHEMAS.toLowerCase();
			if (Objects.nonNull(type) && Objects.nonNull(version)) {
				container += ("/" + type + "/" + version);
			} else {
				container += "";
			}
			System.out.println("container: " + container);
			System.out.println("fileName: " + fileName);
			if (existsInObjectStore(container, fileName)) {
				System.out.println("existsInObjectStore");
				InputStream inputStream = getFromObjectStore(container, fileName);
				if (Objects.nonNull(inputStream)) {
					System.out.println("getFromObjectStore");
					inputStream.reset();
					InputStreamReader isr = new InputStreamReader(inputStream);
					BufferedReader br = new BufferedReader(isr);
					StringBuffer sb = new StringBuffer();
					String str;
					while ((str = br.readLine()) != null) {
						sb.append(str);
					}
					schemaResponse = sb.toString();
					System.out.println("schemaResponse: " + schemaResponse);
					inputStream.close();
				}
			}
			return schemaResponse;
		} catch (Exception e) {
			log.error("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In getSchema - " + e.getMessage());
			throw e;
		}
	}

	@CacheEvict(cacheNames = "schemas", key = "{#type, #version #fileName}")
	public boolean putSchema(String type, String version, String fileName, InputStream inputStream) {
		String container = AppConstants.SCHEMAS.toLowerCase();
		if (Objects.nonNull(type) && Objects.nonNull(version)) {
			container += ("/" + type + "/" + version);
		} else {
			container += "";
		}
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
