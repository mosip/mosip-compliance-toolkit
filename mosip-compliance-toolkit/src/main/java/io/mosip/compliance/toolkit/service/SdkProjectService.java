package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.entity.SdkProjectEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.SdkProjectRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.compliance.toolkit.constants.SdkSpecVersions;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SdkProjectService {

	@Value("${mosip.toolkit.api.id.sdk.project.get}")
	private String getSdkProjectId;
	@Value("${mosip.toolkit.api.id.sdk.project.post}")
	private String getSdkProjectPostId;
	@Value("${mosip.toolkit.api.id.sdk.project.put}")
	private String putSdkProjectId;

	@Autowired
	private SdkProjectRepository sdkProjectRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(SdkProjectService.class);

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

	public ResponseWrapper<SdkProjectDto> getSdkProject(String id) {
		ResponseWrapper<SdkProjectDto> responseWrapper = new ResponseWrapper<>();
		SdkProjectDto sdkProjectDto = null;
		try {
			Optional<SdkProjectEntity> optionalSdkProjectEntity = sdkProjectRepository.findById(id, getPartnerId());
			if (optionalSdkProjectEntity.isPresent()) {
				SdkProjectEntity sdkProjectEntity = optionalSdkProjectEntity.get();

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				sdkProjectDto = objectMapper.convertValue(sdkProjectEntity, SdkProjectDto.class);

			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSdkProject method of SdkProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getSdkProjectId);
		responseWrapper.setResponse(sdkProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SdkProjectDto> addSdkProject(SdkProjectDto sdkProjectDto) {
		ResponseWrapper<SdkProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if (isValidSdkProject(sdkProjectDto)) {
				LocalDateTime crDate = LocalDateTime.now();
				SdkProjectEntity entity = new SdkProjectEntity();
				entity.setId(RandomIdGenerator.generateUUID(sdkProjectDto.getProjectType().toLowerCase(), "", 36));
				entity.setName(sdkProjectDto.getName());
				entity.setProjectType(sdkProjectDto.getProjectType());
				entity.setPurpose(sdkProjectDto.getPurpose());
				entity.setUrl(sdkProjectDto.getUrl());
				entity.setSdkVersion(sdkProjectDto.getSdkVersion());
				entity.setPartnerId(this.getPartnerId());
				entity.setCrBy(this.getUserBy());
				entity.setCrDate(crDate);
				entity.setDeleted(false);

				SdkProjectEntity outputEntity = sdkProjectRepository.save(entity);

				sdkProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity, SdkProjectDto.class);
				sdkProjectDto.setId(entity.getId());
				sdkProjectDto.setPartnerId(entity.getPartnerId());
				sdkProjectDto.setCrBy(entity.getCrBy());
				sdkProjectDto.setCrDate(entity.getCrDate());
			}
		} catch (ToolkitException ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSdkProject method of SdkProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSdkProject method of SdkProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getSdkProjectPostId);
		responseWrapper.setResponse(sdkProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SdkProjectDto> updateSdkProject(SdkProjectDto sdkProjectDto) {
		ResponseWrapper<SdkProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if(Objects.nonNull(sdkProjectDto)){
				String projectId = sdkProjectDto.getId();
				Optional<SdkProjectEntity> optionalSdkProjectEntity = sdkProjectRepository.findById(projectId, getPartnerId());
				if (optionalSdkProjectEntity.isPresent()) {
					SdkProjectEntity entity = optionalSdkProjectEntity.get();
					LocalDateTime updDate = LocalDateTime.now();
					String url = sdkProjectDto.getUrl();
					String bioTestDataFileName = sdkProjectDto.getBioTestDataFileName();
//					Updating SDK project values
					if (Objects.nonNull(url) && !url.isEmpty()) {
						entity.setUrl(url);
					}
					if (Objects.nonNull(bioTestDataFileName) && !bioTestDataFileName.isEmpty()) {
						entity.setBioTestDataFileName(bioTestDataFileName);
					}
					entity.setUpBy(this.getUserBy());
					entity.setUpdDate(updDate);
					SdkProjectEntity outputEntity = sdkProjectRepository.save(entity);
					sdkProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity, SdkProjectDto.class);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			}
		} catch (ToolkitException ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSdkProject method of SdkProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSdkProject method of SdkProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(putSdkProjectId);
		responseWrapper.setResponse(sdkProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	/**
	 * Verifies all values of SdkProjectDto. validates ProjectType, SpecVersion,
	 * Purpose, DeviceType, DeviceSubType
	 *
	 * @param SdkProjectDto
	 * @return boolean
	 */
	private boolean isValidSdkProject(SdkProjectDto sdkProjectDto) throws ToolkitException {
		ToolkitErrorCodes errorCode = null;

		ProjectTypes projectTypesCode = ProjectTypes.fromCode(sdkProjectDto.getProjectType());
		SdkSpecVersions specVersionCode = SdkSpecVersions.fromCode(sdkProjectDto.getSdkVersion());
		SdkPurpose purposeCode = SdkPurpose.fromCode(sdkProjectDto.getPurpose());
		String url = sdkProjectDto.getUrl();

		if (url == null || url.isEmpty()) {
			errorCode = ToolkitErrorCodes.INVALID_SDK_URL;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		switch (projectTypesCode) {
		case SDK:
			switch (specVersionCode) {
			case SPEC_VER_0_9_0:
				break;
			default:
				errorCode = ToolkitErrorCodes.INVALID_SDK_SPEC_VERSION;
				throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
			}
			break;
		default:
			errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		switch (purposeCode) {
		case CHECK_QUALITY:
		case SEGMENT:
		case EXTRACT_TEMPLATE:
		case CONVERT_FORMAT:
		case MATCHER:
			break;
		default:
			errorCode = ToolkitErrorCodes.INVALID_SDK_PURPOSE;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		return true;
	}
}
