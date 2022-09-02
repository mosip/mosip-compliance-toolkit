package io.mosip.compliance.toolkit.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class BiometricTestDataService {

	@Value("$(mosip.toolkit.api.id.biometric.testdata.get)")
	private String getBiometricTestDataId;

	@Value("$(mosip.toolkit.api.id.biometric.testdata.post)")
	private String postBiometricTestDataId;

	@Value("$(mosip.toolkit.api.id.biometric.testdata.filenames.get)")
	private String getBioTestDataFileNames;

	@Value("$(mosip.toolkit.api.id.biometric.default.testdata.post)")
	private String postBioDefaultTestDataId;

	private Logger log = LoggerConfiguration.logConfig(BiometricTestDataService.class);

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	private String getUserBy() {
		String crBy = authUserDetails().getMail();
		return crBy;
	}

	@Autowired
	private BiometricTestDataRepository biometricTestDataRepository;

	public ResponseWrapper<List<BiometricTestDataDto>> getBiometricTestdata() {
		ResponseWrapper<List<BiometricTestDataDto>> responseWrapper = new ResponseWrapper<>();
		List<BiometricTestDataDto> biometricTestDataList = new ArrayList<>();
		try {
			List<BiometricTestDataEntity> entities = biometricTestDataRepository.findAllByPartnerId(getPartnerId());
			if (Objects.nonNull(entities) && !entities.isEmpty()) {
				ObjectMapper mapper = objectMapperConfig.objectMapper();
				for (BiometricTestDataEntity entity : entities) {
					BiometricTestDataDto testData = mapper.convertValue(entity, BiometricTestDataDto.class);
					biometricTestDataList.add(testData);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getBiometricTestDataId);
		responseWrapper.setResponse(biometricTestDataList);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<BiometricTestDataDto> addBiometricTestdata(BiometricTestDataDto inputBiometricTestDataDto,
			MultipartFile file) {
		ResponseWrapper<BiometricTestDataDto> responseWrapper = new ResponseWrapper<>();
		BiometricTestDataDto biometricTestData = null;
		try {
			if (Objects.nonNull(inputBiometricTestDataDto) && Objects.nonNull(file) && !file.isEmpty()
					&& file.getSize() > 0) {

				ObjectMapper mapper = objectMapperConfig.objectMapper();
				BiometricTestDataEntity inputEntity = mapper.convertValue(inputBiometricTestDataDto,
						BiometricTestDataEntity.class);
				inputEntity.setId(RandomIdGenerator.generateUUID("btd", "", 36));
				inputEntity.setPartnerId(getPartnerId());
				inputEntity.setFileId(file.getOriginalFilename());
				inputEntity.setCrBy(getUserBy());
				inputEntity.setCrDate(LocalDateTime.now());
				inputEntity.setUpBy(null);
				inputEntity.setUpdDate(null);
				inputEntity.setDeleted(false);
				inputEntity.setDelTime(null);

				if (!objectStore.exists(objectStoreAccountName, inputEntity.getPartnerId(), null, null,
						inputEntity.getFileId())) {
					BiometricTestDataEntity entity = biometricTestDataRepository.save(inputEntity);
					
					InputStream is = file.getInputStream();
					boolean status = objectStore.putObject(objectStoreAccountName, inputEntity.getPartnerId(), null,
							null, inputEntity.getFileId(), is);
					is.close();
					if (status) {
						biometricTestData = mapper.convertValue(entity, BiometricTestDataDto.class);
					} else {
						List<ServiceError> serviceErrorsList = new ArrayList<>();
						ServiceError serviceError = new ServiceError();
						serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorCode());
						serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_UNABLE_TO_ADD_FILE.getErrorMessage());
						serviceErrorsList.add(serviceError);
						responseWrapper.setErrors(serviceErrorsList);
					}
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_FILE_EXISTS.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_FILE_EXISTS.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}

			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (DataIntegrityViolationException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.DUPLICATE_VALUE_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.DUPLICATE_VALUE_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBiometricTestDataId);
		responseWrapper.setResponse(biometricTestData);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<List<String>> getBioTestDataFileNames() {
		ResponseWrapper<List<String>> responseWrapper = new ResponseWrapper<>();
		List<String> fileNames = new ArrayList<>();
		try {
			String partnerId = getPartnerId();
			List<ObjectDto> objects = objectStore.getAllObjects(objectStoreAccountName, partnerId);
			if (Objects.nonNull(objects) && !objects.isEmpty()) {
				for (ObjectDto objectDto : objects) {
					fileNames.add(objectDto.getObjectName());
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getBioTestDataFileNames method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getBioTestDataFileNames);
		responseWrapper.setResponse(fileNames);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<Boolean> addDefaultBioTestData(MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (Objects.nonNull(file) && !file.isEmpty() && file.getSize() > 0) {
				String defaultFileName = AppConstants.MOSIP_DEFAULT + ".zip";
				if (isObjectExistInObjectStore(null, defaultFileName)) {
					deleteObjectInObjectStore(null, defaultFileName);
				}
				InputStream is = file.getInputStream();
				status = putObjectToObjectStore(null, defaultFileName, is);
				is.close();
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addDefaultBioTestData method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBioDefaultTestDataId);
		responseWrapper.setResponse(status);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseEntity<Resource> getDefaultBioTestData() {
		ByteArrayResource resource = null;
		try {
			String defaultFileName = AppConstants.MOSIP_DEFAULT + ".zip";
			if (isObjectExistInObjectStore(null, defaultFileName)) {
				InputStream inputStream = getObjectFromObjectStore(null, defaultFileName);
				resource = new ByteArrayResource(inputStream.readAllBytes());
				inputStream.close();

				HttpHeaders header = new HttpHeaders();
				header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; " + defaultFileName);
				header.add("Cache-Control", "no-cache, no-store, must-revalidate");
				header.add("Pragma", "no-cache");
				header.add("Expires", "0");

				return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
						.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getDefaultBioTestData method of BiometricTestDataService Service - " + ex.getMessage());
		}

		return ResponseEntity.noContent().build();
	}

	private boolean isObjectExistInObjectStore(String container, String objectName) {
		return objectStore.exists(objectStoreAccountName, container, null, null, objectName);
	}

	private InputStream getObjectFromObjectStore(String container, String objectName) {
		return objectStore.getObject(objectStoreAccountName, container, null, null, objectName);
	}

	private boolean putObjectToObjectStore(String container, String objectName, InputStream data) {
		return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
	}

	private boolean deleteObjectInObjectStore(String container, String objectName) {
		return objectStore.deleteObject(objectStoreAccountName, container, null, null, objectName);
	}

}
