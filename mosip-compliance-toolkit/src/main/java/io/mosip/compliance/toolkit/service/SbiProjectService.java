package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.kernel.core.exception.ServiceError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.DeviceSubTypes;
import io.mosip.compliance.toolkit.constants.DeviceTypes;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.EncryptionKeyResponseDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class SbiProjectService {
	@Value("${mosip.toolkit.api.id.sbi.project.get}")
	private String getSbiProjectId;

	@Value("${mosip.toolkit.api.id.sbi.project.post}")
	private String getSbiProjectPostId;

	@Value("${mosip.toolkit.api.id.sbi.project.put}")
	private String putSbiProjectId;

	@Autowired
	public ObjectMapperConfig objectMapperConfig;

	@Autowired
	private SbiProjectRepository sbiProjectRepository;

	@Autowired
	private KeyManagerHelper keyManagerHelper;

	@Autowired
	private CollectionsService collectionsService;

	private Logger log = LoggerConfiguration.logConfig(SbiProjectService.class);

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

	public ResponseWrapper<SbiProjectDto> getSbiProject(String id) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		SbiProjectDto sbiProjectDto = null;
		try {
			Optional<SbiProjectEntity> optionalSbiProjectEntity = sbiProjectRepository.findById(id, getPartnerId());
			if (optionalSbiProjectEntity.isPresent()) {
				SbiProjectEntity sbiProjectEntity = optionalSbiProjectEntity.get();

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				sbiProjectDto = objectMapper.convertValue(sbiProjectEntity, SbiProjectDto.class);

			} else {
				String errorCode = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorCode();
				String errorMessage = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		}
		responseWrapper.setId(getSbiProjectId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SbiProjectDto> addSbiProject(SbiProjectDto sbiProjectDto) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();

		try {
			if (isValidSbiProject(sbiProjectDto)) {
				LocalDateTime crDate = LocalDateTime.now();
				SbiProjectEntity entity = new SbiProjectEntity();
				entity.setId(RandomIdGenerator.generateUUID(sbiProjectDto.getProjectType().toLowerCase(), "", 36));
				entity.setName(sbiProjectDto.getName());
				entity.setProjectType(sbiProjectDto.getProjectType());
				entity.setSbiVersion(sbiProjectDto.getSbiVersion());
				entity.setPurpose(sbiProjectDto.getPurpose());
				entity.setDeviceType(sbiProjectDto.getDeviceType());
				entity.setDeviceSubType(sbiProjectDto.getDeviceSubType());
				entity.setDeviceImages(sbiProjectDto.getDeviceImages());
				entity.setSbiHash(sbiProjectDto.getSbiHash());
				entity.setWebsiteUrl(sbiProjectDto.getWebsiteUrl());
				entity.setPartnerId(this.getPartnerId());
				entity.setCrBy(this.getUserBy());
				entity.setCrDate(crDate);
				entity.setDeleted(false);

				sbiProjectRepository.save(entity);
				collectionsService.addAllTestcasesCollection(sbiProjectDto,
						entity);


				sbiProjectDto.setId(entity.getId());
				sbiProjectDto.setPartnerId(entity.getPartnerId());
				sbiProjectDto.setCrBy(entity.getCrBy());
				sbiProjectDto.setCrDate(entity.getCrDate());
			}
		} catch (ToolkitException ex) {
			sbiProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getErrorCode();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (DataIntegrityViolationException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorCode();
			String errorMessage = null;
			if (sbiProjectDto != null) {
				errorMessage = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorMessage() + " " + sbiProjectDto.getName();
			} else {
				errorMessage = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorMessage();
			}
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (Exception ex) {
			sbiProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SBI_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SBI_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		}
		responseWrapper.setId(getSbiProjectPostId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SbiProjectDto> updateSbiProject(SbiProjectDto sbiProjectDto) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if (Objects.nonNull(sbiProjectDto)) {
				String projectId = sbiProjectDto.getId();
				Optional<SbiProjectEntity> optionalSbiProjectEntity = sbiProjectRepository.findById(projectId,
						getPartnerId());
				if (optionalSbiProjectEntity.isPresent()) {
					SbiProjectEntity entity = optionalSbiProjectEntity.get();
					LocalDateTime updDate = LocalDateTime.now();
					String deviceImages = sbiProjectDto.getDeviceImages();
					String sbiHash = sbiProjectDto.getSbiHash();
					String websiteUrl = sbiProjectDto.getWebsiteUrl();
					if (Objects.nonNull(deviceImages) && !deviceImages.isEmpty()) {
						entity.setDeviceImages(deviceImages);
					}
					if (Objects.nonNull(sbiHash) && !sbiHash.isEmpty()) {
						entity.setSbiHash(sbiHash);
					}
					if (Objects.nonNull(websiteUrl) && !websiteUrl.isEmpty()) {
						entity.setWebsiteUrl(websiteUrl);
					}
					entity.setUpBy(this.getUserBy());
					entity.setUpdDate(updDate);
					SbiProjectEntity outputEntity = sbiProjectRepository.save(entity);
					sbiProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity,
							SbiProjectDto.class);
				} else {
					String errorCode = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorCode();
					String errorMessage = ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
				}
			}
		} catch (ToolkitException ex) {
			sbiProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (Exception ex) {
			sbiProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSbiProject method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SBI_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SBI_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		}
		responseWrapper.setId(putSbiProjectId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}


	/**
	 * Verifies all values of SbiProjectDto. validates ProjectType, SpecVersion,
	 * Purpose, DeviceType, DeviceSubType
	 *
	 * @param SbiProjectDto
	 * @return boolean
	 */
	private boolean isValidSbiProject(SbiProjectDto sbiProjectDto) throws ToolkitException {
		ToolkitErrorCodes errorCode = null;

		ProjectTypes projectTypesCode = ProjectTypes.fromCode(sbiProjectDto.getProjectType());
		SbiSpecVersions specVersionCode = SbiSpecVersions.fromCode(sbiProjectDto.getSbiVersion());
		Purposes purposeCode = Purposes.fromCode(sbiProjectDto.getPurpose());
		DeviceTypes deviceTypeCode = DeviceTypes.fromCode(sbiProjectDto.getDeviceType());
		DeviceSubTypes deviceSubTypeCode = DeviceSubTypes.fromCode(sbiProjectDto.getDeviceSubType());

		switch (projectTypesCode) {
		case SBI:
			switch (specVersionCode) {
			case SPEC_VER_0_9_5:
			case SPEC_VER_1_0_0:
				break;
			default:
				errorCode = ToolkitErrorCodes.INVALID_SBI_SPEC_VERSION_FOR_PROJECT_TYPE;
				throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
			}
			break;
		default:
			errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		switch (purposeCode) {
		case AUTH:
			switch (deviceTypeCode) {
			case FINGER:
				switch (deviceSubTypeCode) {
				case SINGLE:
				case TOUCHLESS:
				case SLAP:
					break;
				case DOUBLE:
				case FULL_FACE:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			case IRIS:
				switch (deviceSubTypeCode) {
				case SINGLE:
				case DOUBLE:
					break;
				case SLAP:
				case TOUCHLESS:
				case FULL_FACE:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			case FACE:
				switch (deviceSubTypeCode) {
				case FULL_FACE:
					break;
				case SINGLE:
				case DOUBLE:
				case SLAP:
				case TOUCHLESS:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			default:
				errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
				throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
			}
			break;
		case REGISTRATION:
			switch (deviceTypeCode) {
			case FINGER:
				switch (deviceSubTypeCode) {
				case SLAP:
				case TOUCHLESS:
					break;
				case SINGLE:
				case DOUBLE:
				case FULL_FACE:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			case IRIS:
				switch (deviceSubTypeCode) {
				case DOUBLE:
					break;
				case SINGLE:
				case TOUCHLESS:
				case FULL_FACE:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			case FACE:
				switch (deviceSubTypeCode) {
				case FULL_FACE:
					break;
				case SINGLE:
				case DOUBLE:
				case TOUCHLESS:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				default:
					errorCode = ToolkitErrorCodes.INVALID_DEVICE_SUB_TYPE;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
				}
				break;
			default:
				errorCode = ToolkitErrorCodes.INVALID_DEVICE_TYPE;
				throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
			}
			break;
		default:
			errorCode = ToolkitErrorCodes.INVALID_PURPOSE;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		return true;
	}

	public ResponseWrapper<String> getEncryptionKey() {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		String result = null;
		try {
			io.restassured.response.Response postResponse = keyManagerHelper.encryptionKeyResponse();
			result = postResponse.getBody().asString();
			EncryptionKeyResponseDto keyManagerResponseDto = objectMapperConfig.objectMapper()
					.readValue(postResponse.getBody().asString(), EncryptionKeyResponseDto.class);

			if ((keyManagerResponseDto.getErrors() != null && keyManagerResponseDto.getErrors().size() > 0)) {
				keyManagerResponseDto.getErrors().get(0).getMessage();
				throw new ToolkitException(ToolkitErrorCodes.ENCRYPTION_KEY_ERROR.getErrorCode(),
						keyManagerResponseDto.getErrors().get(0).getMessage());
			} else {
				EncryptionKeyResponseDto.EncryptionKeyResponse e = keyManagerResponseDto.getResponse();
				result = e.getCertificate();
			}

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getEncryptionKey method of SbiProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.ENCRYPTION_KEY_ERROR.getErrorCode();
			String errorMessage = ToolkitErrorCodes.ENCRYPTION_KEY_ERROR.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		}
		responseWrapper.setId(getSbiProjectId);
		responseWrapper.setResponse(result);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
