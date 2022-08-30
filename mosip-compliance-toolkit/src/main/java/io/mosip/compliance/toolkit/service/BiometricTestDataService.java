package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

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
			if (Objects.nonNull(inputBiometricTestDataDto) && Objects.nonNull(file)) {

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
					InputStream is = file.getInputStream();
					boolean status = objectStore.putObject(objectStoreAccountName, inputEntity.getPartnerId(), null,
							null, inputEntity.getFileId(), is);
					is.close();
					if (status) {
						BiometricTestDataEntity entity = biometricTestDataRepository.save(inputEntity);
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
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In saveBiometricTestdata method of BiometricTestDataService Service - " + ex.getMessage());
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

}
