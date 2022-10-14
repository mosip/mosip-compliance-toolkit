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
import io.mosip.compliance.toolkit.constants.AppConstants;

@Service
public class ResourceCacheService {
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	@Cacheable(cacheNames = "schemas", key = "{#type, #fileName}")
	public String getSchema(String type, String fileName) throws IOException {
		String schemaResponse = null;
		String container = AppConstants.SCHEMAS.toLowerCase();
		container += (Objects.nonNull(type) ? ("/" + type) : "");
		if (existsInObjectStore(container, fileName)) {
			InputStream inputStream = getFromObjectStore(container, fileName);
			if(Objects.nonNull(inputStream)) {				
				inputStream.reset();
				InputStreamReader isr = new InputStreamReader(inputStream); 
				BufferedReader br = new BufferedReader(isr); 
	            StringBuffer sb = new StringBuffer(); 
	            String str; 
	            while ((str = br.readLine()) != null) { 
	                sb.append(str); 
	            }
	            schemaResponse = sb.toString();
			}
		}
		return schemaResponse;
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
