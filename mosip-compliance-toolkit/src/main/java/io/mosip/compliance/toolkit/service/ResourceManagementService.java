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
	
	private static final String JSON_EXT = ".json";

	private static final String UNDERSCORE = "_";

	private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + "sbi";

	private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + "sdk";

	@Value("$(mosip.toolkit.api.id.resource.file.post)")
	private String postResourceFileId;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(ResourceManagementService.class);

	public ResponseWrapper<Boolean> uploadResourceFile(String type, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (Objects.nonNull(file) && Objects.nonNull(type) && !file.isEmpty() && file.getSize() > 0) {

				String fileName = file.getOriginalFilename();
				String container = null;
				String objectName = null;
				switch (type) {
				case AppConstants.SAMPLE:
					if (!fileName.endsWith(ZIP_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.TESTDATA;
					String purposeSample = fileName.replace(AppConstants.SAMPLE + UNDERSCORE, "").replace(ZIP_EXT, "");
					SdkPurpose sdkPurposeSample = SdkPurpose.valueOf(purposeSample);
					objectName = AppConstants.SAMPLE + UNDERSCORE + sdkPurposeSample.toString().toUpperCase() + ZIP_EXT;

					break;
				case AppConstants.MOSIP_DEFAULT:
					if (!fileName.endsWith(ZIP_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.TESTDATA;
					String purposeDefault = fileName.replace(AppConstants.MOSIP_DEFAULT + UNDERSCORE, "")
							.replace(ZIP_EXT, "");
					SdkPurpose sdkPurposeDefault = SdkPurpose.valueOf(purposeDefault);
					objectName = AppConstants.MOSIP_DEFAULT + UNDERSCORE + sdkPurposeDefault.toString().toUpperCase()
							+ ZIP_EXT;
					break;
				case AppConstants.SCHEMAS:
					if (!fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS;
					objectName = fileName;
					break;
				case SBI_SCHEMA:
					if (!fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS + "/" + AppConstants.SBI.toLowerCase();
					objectName = fileName;
					break;
				case SDK_SCHEMA:
					if (!fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS + "/" + AppConstants.SDK.toLowerCase();
					objectName = fileName;
					break;
				default:
					throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode(),
							ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				}
				InputStream is = file.getInputStream();
				status = putInObjectStore(container, objectName, is);
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
			serviceError.setErrorCode(ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postResourceFileId);
		responseWrapper.setResponse(status);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private boolean putInObjectStore(String container, String objectName, InputStream data) {
		return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
	}

}
