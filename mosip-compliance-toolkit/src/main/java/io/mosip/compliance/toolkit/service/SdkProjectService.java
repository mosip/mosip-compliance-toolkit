package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.SdkProjectEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.repository.SdkProjectRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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

	@Value("${mosip.toolkit.default.collection.name}")
	private String defaultCollectionName;
	@Autowired
	private SdkProjectRepository sdkProjectRepository;
	
	@Autowired
	private BiometricTestDataRepository biometricTestDataRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@Autowired
	private TestCasesService testCasesService;

	@Autowired
	private CollectionsService collectionsService;
	
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

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
				String errorCode = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode();
				String errorMessage = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
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
				boolean isValidTestFile = false;
				String partnerId = this.getPartnerId();
				if(Objects.isNull(sdkProjectDto.getBioTestDataFileName())) {
					isValidTestFile = false;
				} else if(sdkProjectDto.getBioTestDataFileName().equals(AppConstants.MOSIP_DEFAULT)) {
					isValidTestFile = true;
				} else {
					BiometricTestDataEntity biometricTestData = biometricTestDataRepository
							.findByTestDataName(sdkProjectDto.getBioTestDataFileName(), partnerId);
					String fileName = biometricTestData.getFileId();
					String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/" + sdkProjectDto.getPurpose();
					if (objectStore.exists(objectStoreAccountName, container, null, null, fileName)) {
						isValidTestFile = true;
					}
				}

				if (isValidTestFile) {
					LocalDateTime crDate = LocalDateTime.now();
					SdkProjectEntity entity = new SdkProjectEntity();
					entity.setId(RandomIdGenerator.generateUUID(sdkProjectDto.getProjectType().toLowerCase(), "", 36));
					entity.setName(sdkProjectDto.getName());
					entity.setProjectType(sdkProjectDto.getProjectType());
					entity.setPurpose(sdkProjectDto.getPurpose());
					entity.setUrl(sdkProjectDto.getUrl());
					entity.setSdkHash(sdkProjectDto.getSdkHash());
					entity.setWebsiteUrl(sdkProjectDto.getWebsiteUrl());
					entity.setSdkVersion(sdkProjectDto.getSdkVersion());
					entity.setBioTestDataFileName(sdkProjectDto.getBioTestDataFileName());
					entity.setPartnerId(partnerId);
					entity.setCrBy(this.getUserBy());
					entity.setCrDate(crDate);
					entity.setDeleted(false);

					SdkProjectEntity outputEntity = sdkProjectRepository.save(entity);
					//Add a default "ALL" collection for the newly created project
					addDefaultCollection(sdkProjectDto, entity.getId());

					sdkProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity, SdkProjectDto.class);
					sdkProjectDto.setId(entity.getId());
					sdkProjectDto.setPartnerId(entity.getPartnerId());
					sdkProjectDto.setCrBy(entity.getCrBy());
					sdkProjectDto.setCrDate(entity.getCrDate());
				} else {
					String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
					String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
				}
			}
		} catch (ToolkitException ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (DataIntegrityViolationException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorCode();
			String errorMessage = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorMessage() + " " + sdkProjectDto.getName();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (Exception ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		}
		responseWrapper.setId(getSdkProjectPostId);
		responseWrapper.setResponse(sdkProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public void addDefaultCollection(SdkProjectDto sdkProjectDto,
									 String projectId) {
		log.debug("sessionId", "idType", "id", "Started addDefaultCollection for SDK project: {}",projectId);
		try {
			//1. Add a new default collection
			CollectionRequestDto collectionRequestDto = new CollectionRequestDto();
			collectionRequestDto.setProjectId(projectId);
			collectionRequestDto.setProjectType(sdkProjectDto.getProjectType());
			collectionRequestDto.setCollectionName(defaultCollectionName);
			ResponseWrapper<CollectionDto> addCollectionWrapper = collectionsService.addCollection(collectionRequestDto);
			String defaultCollectionId = null;
			if (addCollectionWrapper.getResponse() != null) {
				defaultCollectionId = addCollectionWrapper.getResponse().getCollectionId();
				log.debug("sessionId", "idType", "id", "Default collection added: {}", defaultCollectionId);
			} else {
				log.debug("sessionId", "idType", "id", "Default collection could not be added for this project: {}", projectId);
			}
			if (defaultCollectionId != null) {
				//2. Get the testcases
				ResponseWrapper<List<TestCaseDto>> testCaseWrapper = testCasesService.getSdkTestCases(
						sdkProjectDto.getSdkVersion(),
						sdkProjectDto.getPurpose());
				List<CollectionTestCaseDto> inputList = new ArrayList<>();
				if (testCaseWrapper.getResponse() != null && testCaseWrapper.getResponse().size() > 0) {
					for (TestCaseDto testCase : testCaseWrapper.getResponse()) {
						CollectionTestCaseDto collectionTestCaseDto = new CollectionTestCaseDto();
						collectionTestCaseDto.setCollectionId(defaultCollectionId);
						collectionTestCaseDto.setTestCaseId(testCase.getTestId());
						inputList.add(collectionTestCaseDto);
					}
				}
				//3. add the testcases for the default collection
				if (inputList.size() > 0 ) {
					collectionsService.addTestCasesForCollection(inputList);
				}
			}
		} catch (Exception ex) {
			//This is a fail safe operation, so exception can be ignored
			log.debug("sessionId", "idType", "id", "Error in adding default collection: {}", ex.getLocalizedMessage());
		}
	}

	public ResponseWrapper<SdkProjectDto> updateSdkProject(SdkProjectDto sdkProjectDto) {
		ResponseWrapper<SdkProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if (Objects.nonNull(sdkProjectDto)) {
				String projectId = sdkProjectDto.getId();
				String partnerId = this.getPartnerId();
				Optional<SdkProjectEntity> optionalSdkProjectEntity = sdkProjectRepository.findById(projectId,
						getPartnerId());
				if (optionalSdkProjectEntity.isPresent()) {
					
						SdkProjectEntity entity = optionalSdkProjectEntity.get();
						LocalDateTime updDate = LocalDateTime.now();
						String url = sdkProjectDto.getUrl();
						String sdkHash = sdkProjectDto.getSdkHash();
						String websiteUrl = sdkProjectDto.getWebsiteUrl();
						String bioTestDataName = sdkProjectDto.getBioTestDataFileName();
//					Updating SDK project values
						if (Objects.nonNull(url) && !url.isEmpty()) {
							entity.setUrl(url);
						}
						if (Objects.nonNull(sdkHash) && !sdkHash.isEmpty()) {
							entity.setSdkHash(sdkHash);
						}
						if (Objects.nonNull(websiteUrl) && !websiteUrl.isEmpty()) {
							entity.setWebsiteUrl(websiteUrl);
						}
						if (Objects.nonNull(bioTestDataName) && !bioTestDataName.isEmpty()) {
							if (bioTestDataName.equals(AppConstants.MOSIP_DEFAULT)) {
								entity.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);
							} else {
								BiometricTestDataEntity biometricTestData = biometricTestDataRepository
										.findByTestDataName(bioTestDataName, partnerId);
								String fileName = biometricTestData.getFileId();
								String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/"
										+ entity.getPurpose();
								if (objectStore.exists(objectStoreAccountName, container, null, null, fileName)) {
									entity.setBioTestDataFileName(bioTestDataName);
								} else {
									String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
									String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage();
									responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
								}
							}
						}
						entity.setUpBy(this.getUserBy());
						entity.setUpdDate(updDate);
						SdkProjectEntity outputEntity = sdkProjectRepository.save(entity);
						sdkProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity,
								SdkProjectDto.class);
					
				} else {
					String errorCode = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorCode();
					String errorMessage = ToolkitErrorCodes.SDK_PROJECT_NOT_AVAILABLE.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
				}
			}
		} catch (ToolkitException ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
		} catch (Exception ex) {
			sdkProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateSdkProject method of SdkProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.SDK_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
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
