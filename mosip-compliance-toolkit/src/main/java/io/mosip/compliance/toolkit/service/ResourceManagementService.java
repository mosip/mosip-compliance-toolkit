package io.mosip.compliance.toolkit.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Objects;

import io.mosip.compliance.toolkit.util.CommonErrorUtil;
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
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;

@Service
public class ResourceManagementService {

	private static final String BLANK_SPACE = " ";

	private static final String ZIP_EXT = ".zip";
	
	private static final String JSON_EXT = ".json";

	private static final String UNDERSCORE = "_";

	private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SBI;

	private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SDK;

	private static final String ABIS_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.ABIS;

	@Value("${mosip.toolkit.document.scan}")
    private Boolean scanDocument;
    
	/**
     * Autowired reference for {@link #VirusScanner}
     */
    @Autowired
    VirusScanner<Boolean, InputStream> virusScan;

    
	@Value("$(mosip.toolkit.api.id.resource.file.post)")
	private String postResourceFileId;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(ResourceManagementService.class);

	public ResponseWrapper<Boolean> uploadResourceFile(String type, String version, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (scanDocument) {
                isVirusScanSuccess(file);
            }
			
			if (Objects.nonNull(file) && Objects.nonNull(type) && !file.isEmpty() && file.getSize() > 0) {
				if ((type.equals(SBI_SCHEMA) || type.equals(SDK_SCHEMA)) && !Objects.nonNull(version)) {
					throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode(),
							ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				}
				String fileName = file.getOriginalFilename();
				String container = null;
				String objectName = null;
				switch (type) {
				case AppConstants.MOSIP_DEFAULT:
					if (Objects.isNull(fileName) || !fileName.endsWith(ZIP_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.TESTDATA;
					String purposeDefault = fileName.replace(AppConstants.MOSIP_DEFAULT + UNDERSCORE, "")
							.replace(ZIP_EXT, "");
					if(purposeDefault.contains(AppConstants.ABIS)) {
						objectName = AppConstants.MOSIP_DEFAULT + UNDERSCORE + purposeDefault.toUpperCase() + ZIP_EXT;
					} else {
						SdkPurpose sdkPurposeDefault = SdkPurpose.valueOf(purposeDefault);
						objectName = AppConstants.MOSIP_DEFAULT + UNDERSCORE + sdkPurposeDefault.toString().toUpperCase()
								+ ZIP_EXT;
					}
					break;
				case AppConstants.SCHEMAS:
					if (Objects.isNull(fileName) || !fileName.equals(AppConstants.TESTCASE_SCHEMA_JSON)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS.toLowerCase();
					objectName = fileName;
					break;
				case SBI_SCHEMA:
					if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.SBI.toLowerCase() + "/" + version;
					objectName = fileName;
					break;
				case SDK_SCHEMA:
					if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.SDK.toLowerCase() + "/" + version;
					objectName = fileName;
					break;
				case ABIS_SCHEMA:
					if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
						throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
								ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
					}
					container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.ABIS.toLowerCase() + "/" + version;
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
				String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
				String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
				responseWrapper.setErrors(CommonErrorUtil.getServiceErr(errorCode,errorMessage));
			}
		} catch (ToolkitException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In uploadResourceFile method of ResourceManagementService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonErrorUtil.getServiceErr(errorCode,errorMessage));
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In uploadResourceFile method of ResourceManagementService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode();
			String errorMessage = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage();
			responseWrapper.setErrors(CommonErrorUtil.getServiceErr(errorCode,errorMessage));
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
	
    private boolean isVirusScanSuccess(MultipartFile file) {
        try {
            log.info("sessionId", "idType", "id", "In isVirusScanSuccess method of ResourceManagementService");
            return virusScan.scanDocument(file.getBytes());
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
					"In isVirusScanSuccess method of ResourceManagementService Service - " + e.getMessage());
            throw new VirusScannerException(ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode(),
                    ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + e.getMessage());
        }
    }

}
