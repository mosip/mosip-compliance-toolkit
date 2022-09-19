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
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class ResourceManagementService {

	private static final String BLANK_SPACE = " ";

	private static final String ZIP_EXT = ".zip";

	private static final String UNDERSCORE = "_";

	private static final String SCHEMAS = "schemas";

	@Value("${mosip.toolkit.api.id.schema.post}")
	private String postSchemaId;

	@Value("$(mosip.toolkit.api.id.biometric.default.testdata.post)")
	private String postBioDefaultTestDataId;

	@Value("$(mosip.toolkit.api.id.biometric.sample.testdata.post)")
	private String postBioSampleTestDataId;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(ResourceManagementService.class);

	public ResponseWrapper<Boolean> uploadSchema(String strProjectType, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean schemaAdded = false;
		try {
			if (Objects.nonNull(strProjectType) && Objects.nonNull(file)) {
				ProjectTypes projectTypes = ProjectTypes.fromCode(strProjectType);
				String container = SCHEMAS + "/" + projectTypes.getCode().toLowerCase();
				String fileName = file.getOriginalFilename();
				InputStream is = file.getInputStream();
				schemaAdded = putInObjectStore(container, fileName, is);
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
					"In addSchemaToObjectStore method of ResourceManagementService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getErrorText());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSchemaToObjectStore method of ResourceManagementService Service - " + ex.getMessage());
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

	public ResponseWrapper<Boolean> uploadDefaultBioTestDataFile(String purpose, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (Objects.nonNull(file) && Objects.nonNull(purpose) && !file.isEmpty() && file.getSize() > 0) {
				SdkPurpose sdkPurpose = SdkPurpose.fromCode(purpose);
				String defaultFileName = AppConstants.MOSIP_DEFAULT.toUpperCase() + UNDERSCORE
						+ sdkPurpose.toString().toUpperCase() + ZIP_EXT;
				InputStream is = file.getInputStream();
				status = putInObjectStore(null, defaultFileName, is);
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
					"In uploadMosipDefaultDataFile method of ResourceManagementService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In uploadMosipDefaultDataFile method of ResourceManagementService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBioDefaultTestDataId);
		responseWrapper.setResponse(status);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<Boolean> uploadSampleBioTestDataFile(String purpose, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (Objects.nonNull(file) && Objects.nonNull(purpose) && !file.isEmpty() && file.getSize() > 0) {
				SdkPurpose sdkPurpose = SdkPurpose.fromCode(purpose);
				String defaultFileName = AppConstants.SAMPLE.toUpperCase() + UNDERSCORE
						+ sdkPurpose.toString().toUpperCase() + ZIP_EXT;
				InputStream is = file.getInputStream();
				status = putInObjectStore(null, defaultFileName, is);
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
					"In uploadSampleBioTestDataFile method of ResourceManagementService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In uploadSampleBioTestDataFile method of ResourceManagementService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBioSampleTestDataId);
		responseWrapper.setResponse(status);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private boolean putInObjectStore(String container, String objectName, InputStream data) {
		return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
	}

}
