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
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class BiometricTestDataService {

	private static final String BLANK_SPACE = " ";

	private static final String ZIP_EXT = ".zip";

	private static final String UNDERSCORE = "_";

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

	public ResponseWrapper<List<BiometricTestDataDto>> getListOfBiometricTestData() {
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
					ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE + ex.getMessage());
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

				String container = inputEntity.getPartnerId() + "/" + inputEntity.getPurpose();
				if (!objectStore.exists(objectStoreAccountName, container, null, null, inputEntity.getFileId())) {
					BiometricTestDataEntity entity = biometricTestDataRepository.save(inputEntity);

					boolean status = false;
					InputStream is = file.getInputStream();
					try {
						status = putInObjectStore(container, inputEntity.getFileId(), is);
					} catch (Exception ex) {
						log.debug("sessionId", "idType", "id", ex.getStackTrace());
						log.error("sessionId", "idType", "id",
								"In addBiometricTestdata method of BiometricTestDataService Service - "
										+ ex.getMessage());
					}
					is.close();
					if (status) {
						biometricTestData = mapper.convertValue(entity, BiometricTestDataDto.class);
					} else {
						biometricTestDataRepository.delete(entity);
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
					serviceError.setMessage(
							ToolkitErrorCodes.OBJECT_STORE_FILE_EXISTS.getErrorMessage() + inputEntity.getFileId());
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
			serviceError.setErrorCode(ToolkitErrorCodes.BIO_TEST_DATA_FILE_EXISTS.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.BIO_TEST_DATA_FILE_EXISTS.getErrorMessage() + BLANK_SPACE + inputBiometricTestDataDto.getName());
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
					ToolkitErrorCodes.BIOMETRIC_TESTDATA_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBiometricTestDataId);
		responseWrapper.setResponse(biometricTestData);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<List<String>> getBioTestDataFileNames(String purpose) {
		ResponseWrapper<List<String>> responseWrapper = new ResponseWrapper<>();
		List<String> testDataNames = new ArrayList<>();
		try {
			String partnerId = getPartnerId();
			List<ObjectDto> objects = objectStore.getAllObjects(objectStoreAccountName, partnerId + "/" + purpose);
			if (Objects.nonNull(objects) && !objects.isEmpty()) {
				List<String> fileNames = new ArrayList<>();
				for (ObjectDto objectDto : objects) {
					fileNames.add(objectDto.getObjectName());
				}
				String[] inputFileNames = fileNames.toArray(new String[0]);
				testDataNames = biometricTestDataRepository.findNamesByFileIds(inputFileNames, partnerId);
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
					ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getBioTestDataFileNames);
		responseWrapper.setResponse(testDataNames);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<Boolean> uploadSampleBioTestDataFile(String type, MultipartFile file) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		boolean status = false;
		try {
			if (Objects.nonNull(file) && Objects.nonNull(type) && !file.isEmpty() && file.getSize() > 0
					&& (type.equalsIgnoreCase(AppConstants.MOSIP_DEFAULT)
							|| type.equalsIgnoreCase(AppConstants.SAMPLE))) {
				String purpose = getPurposeFrmFileName(file, type);
				SdkPurpose sdkPurpose = SdkPurpose.valueOf(purpose);
				String defaultFileName = type.toUpperCase() + UNDERSCORE + sdkPurpose.toString().toUpperCase()
						+ ZIP_EXT;
				if (existsInObjectStore(null, defaultFileName)) {
					deleteFrmObjectStore(null, defaultFileName);
				}
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
					"In uploadSampleBioTestDataFile method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In uploadSampleBioTestDataFile method of BiometricTestDataService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postBioDefaultTestDataId);
		responseWrapper.setResponse(status);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private String getPurposeFrmFileName(MultipartFile file, String type) {
		String fileName = file.getOriginalFilename();
		String[] nameArr = fileName.split(ZIP_EXT);
		String firstPart = nameArr[0];
		String[] nameArr1 = firstPart.split(UNDERSCORE);
		String purpose = BLANK_SPACE;
		int index = 0;
		for (String item : nameArr1) {
			if (index > 0 && type.equalsIgnoreCase(AppConstants.SAMPLE)) {
				purpose = purpose.concat(item);
				purpose = purpose.concat(UNDERSCORE);
			}
			if (index > 1 && type.equalsIgnoreCase(AppConstants.MOSIP_DEFAULT)) {
				purpose = purpose.concat(item);
				purpose = purpose.concat(UNDERSCORE);
			}
			index++;
		}
		purpose = purpose.trim();
		if ((purpose.lastIndexOf(UNDERSCORE) + 1) ==  purpose.length()) {
			purpose = purpose.substring(0, purpose.lastIndexOf(UNDERSCORE));
		}
		return purpose;
	}

	public ResponseEntity<Resource> getSampleBioTestDataFile(String purpose) {
		ByteArrayResource resource = null;
		try {
			SdkPurpose sdkPurpose = SdkPurpose.fromCode(purpose);
			String defaultFileName = AppConstants.SAMPLE + UNDERSCORE + sdkPurpose.toString().toUpperCase() + ZIP_EXT;
			if (existsInObjectStore(null, defaultFileName)) {
				InputStream inputStream = getFromObjectStore(null, defaultFileName);
				resource = new ByteArrayResource(inputStream.readAllBytes());
				inputStream.close();

				HttpHeaders header = new HttpHeaders();
				header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + defaultFileName);
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

	public ResponseEntity<Resource> getBiometricTestDataFile(String bioTestDataId) {
		ByteArrayResource resource = null;
		try {
			String partnerId = getPartnerId();
			BiometricTestDataEntity biometricTestDataEntity = biometricTestDataRepository.findById(bioTestDataId,
					partnerId);
			if (Objects.nonNull(biometricTestDataEntity)) {
				String fileName = biometricTestDataEntity.getFileId();
				String purpose = biometricTestDataEntity.getPurpose();
				if (Objects.nonNull(fileName) && Objects.nonNull(purpose)) {
					String container = partnerId + "/" + purpose;
					InputStream inputStream = getFromObjectStore(container, fileName);
					resource = new ByteArrayResource(inputStream.readAllBytes());
					inputStream.close();

					HttpHeaders header = new HttpHeaders();
					header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
					header.add("Cache-Control", "no-cache, no-store, must-revalidate");
					header.add("Pragma", "no-cache");
					header.add("Expires", "0");

					return ResponseEntity.ok().headers(header).contentLength(resource.contentLength())
							.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
				}
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getBiometricTestData method of BiometricTestDataService Service - " + ex.getMessage());
		}

		return ResponseEntity.noContent().build();
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

	private boolean deleteFrmObjectStore(String container, String objectName) {
		return objectStore.deleteObject(objectStoreAccountName, container, null, null, objectName);
	}

}
