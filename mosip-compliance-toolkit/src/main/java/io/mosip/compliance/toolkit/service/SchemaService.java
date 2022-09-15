package io.mosip.compliance.toolkit.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.SchemaDataDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class SchemaService {

	@Value("${mosip.toolkit.api.id.schema.post}")
	private String postSchemaId;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(SchemaService.class);

	public ResponseWrapper<Boolean> addSchemaToObjectStore(SchemaDataDto schemaData, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean schemaAdded = false;
		try {
			if (Objects.nonNull(schemaData) && Objects.nonNull(file)) {
				ProjectTypes projectTypes = ProjectTypes.fromCode(schemaData.getProjectType());
				String container = "schemas" + "/" + projectTypes.getCode().toLowerCase();
				String fileName = file.getOriginalFilename();
				InputStream is = file.getInputStream();
				schemaAdded = putObjectToObjectStore(container, fileName, is);
				is.close();
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (ToolkitException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSchemaToObjectStore method of SchemaService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getErrorText());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSchemaToObjectStore method of SchemaService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SCHEMA_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.SCHEMA_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postSchemaId);
		responseWrapper.setResponse(schemaAdded);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private boolean putObjectToObjectStore(String container, String objectName, InputStream data) {
		return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
	}
}
