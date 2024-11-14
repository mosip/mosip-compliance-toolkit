package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionsResponseDto;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.compliance.toolkit.entity.CollectionSummaryEntity;
import io.mosip.compliance.toolkit.entity.CollectionTestCaseEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestCaseRepository;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.CollectionsSummaryRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionsService {

	@Value("${mosip.toolkit.api.id.collection.testcase.post}")
	private String postCollectionTestCaseId;

	@Value("${mosip.toolkit.api.id.collections.get}")
	private String getCollectionsId;

	@Value("${mosip.toolkit.api.id.collection.testcases.get}")
	private String getTestCasesForCollectionId;

	@Value("${mosip.toolkit.api.id.collection.get}")
	private String getCollectionId;

	@Value("${mosip.toolkit.api.id.collection.post}")
	private String postCollectionId;

	@Value("${mosip.toolkit.compliance.collection.name}")
	private String complianceCollectionName;

	@Value("${mosip.toolkit.compliance.collection.ignore.testcases}")
	private String complianceIgnoreTestcases;

	@Value("${mosip.toolkit.quality.assessment.collection.name}")
	private String qualityAssessmentCollectionName;

	@Value("${mosip.toolkit.quality.assessment.collection.ignore.testcases}")
	private String qualityAssessmentIgnoreTestcases;

	@Autowired
	private TestCasesService testCasesService;

	@Autowired
	private CollectionsSummaryRepository collectionsSummaryRepository;

	@Autowired
	private CollectionTestCaseRepository collectionTestCaseRepository;

	@Autowired
	private ResourceCacheService resourceCacheService;

	@Autowired
	private CollectionsRepository collectionsRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	private String getUserBy() {
		String crBy = authUserDetails().getMail();
		return crBy;
	}

	public ResponseWrapper<CollectionDto> getCollectionById(String collectionId, String partnerId) {
		ResponseWrapper<CollectionDto> responseWrapper = new ResponseWrapper<>();
		CollectionDto collection = null;
		try {
			CollectionEntity collectionEntity = collectionsRepository.getCollectionById(collectionId, partnerId);

			if (Objects.nonNull(collectionEntity)) {
				String projectId = null;
				if (Objects.nonNull(collectionEntity.getSbiProjectId())) {
					projectId = collectionEntity.getSbiProjectId();
				} else if (Objects.nonNull(collectionEntity.getSdkProjectId())) {
					projectId = collectionEntity.getSdkProjectId();
				} else if (Objects.nonNull(collectionEntity.getAbisProjectId())) {
					projectId = collectionEntity.getAbisProjectId();
				}
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
				mapper.registerModule(new JavaTimeModule());
				collection = mapper.convertValue(collectionEntity, CollectionDto.class);
				collection.setCollectionId(collectionEntity.getId());
				collection.setProjectId(projectId);
				collection.setCollectionType(collectionEntity.getCollectionType());
				collection.setCrDtimes(collectionEntity.getCrDate());
			} else {
				String errorCode = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode();
				String errorMessage = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getCollectionByCollectionId method of CollectionsService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getCollectionId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collection);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<CollectionTestCasesResponseDto> getTestCasesForCollection(String partnerId,
			String collectionId) {
		ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestCasesResponseDto collectionTestCasesResponseDto = null;

		try {
			if (Objects.nonNull(collectionId)) {
				List<String> testCases = collectionTestCaseRepository.getTestCasesByCollectionId(collectionId,
						partnerId);

				if (Objects.nonNull(testCases) && !testCases.isEmpty()) {
					List<TestCaseDto> collectionTestCases = new ArrayList<>(testCases.size());
					ObjectMapper mapper = new ObjectMapper();
					mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
					mapper.registerModule(new JavaTimeModule());
					for (String testCase : testCases) {
						collectionTestCases.add(mapper.readValue(testCase, TestCaseDto.class));
					}
					collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
					collectionTestCasesResponseDto.setCollectionId(collectionId);
					collectionTestCasesResponseDto.setTestcases(collectionTestCases);
				} else {
					String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
					String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
				}
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getCollectionTestCases method of CollectionsService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getTestCasesForCollectionId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collectionTestCasesResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<CollectionsResponseDto> getCollections(String projectType, String projectId) {
		ResponseWrapper<CollectionsResponseDto> responseWrapper = new ResponseWrapper<>();
		CollectionsResponseDto collectionsResponse = null;
		boolean isProjectTypeValid = false;

		try {
			if (Objects.nonNull(projectType) && (AppConstants.SBI.equalsIgnoreCase(projectType)
					|| AppConstants.ABIS.equalsIgnoreCase(projectType)
					|| AppConstants.SDK.equalsIgnoreCase(projectType))) {
				isProjectTypeValid = true;
			}

			if (isProjectTypeValid) {
				if (Objects.nonNull(projectType) && Objects.nonNull(projectId)) {
					List<CollectionSummaryEntity> collectionsEntityList = null;
					if (AppConstants.SBI.equalsIgnoreCase(projectType)) {
						collectionsEntityList = collectionsSummaryRepository.getCollectionsOfSbiProjects(projectId,
								getPartnerId());
					} else if (AppConstants.SDK.equalsIgnoreCase(projectType)) {
						collectionsEntityList = collectionsSummaryRepository.getCollectionsOfSdkProjects(projectId,
								getPartnerId());
					} else if (AppConstants.ABIS.equalsIgnoreCase(projectType)) {
						collectionsEntityList = collectionsSummaryRepository.getCollectionsOfAbisProjects(projectId,
								getPartnerId());
					}
					List<CollectionDto> collectionsList = new ArrayList<>();
					if (Objects.nonNull(collectionsEntityList) && !collectionsEntityList.isEmpty()) {
						ObjectMapper mapper = new ObjectMapper();
						mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
						mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
						mapper.registerModule(new JavaTimeModule());
						for (CollectionSummaryEntity entity : collectionsEntityList) {
							CollectionDto collection = mapper.convertValue(entity, CollectionDto.class);

							collectionsList.add(collection);
						}
					}
					// send empty collections list, in case none are yet added for a project
					collectionsResponse = new CollectionsResponseDto();
					collectionsResponse.setCollections(collectionsList);
				} else {
					String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
					String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
				}
			} else {
				String errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorCode();
				String errorMessage = ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getProjectCollectionTestrun method of CollectionsService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getCollectionsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collectionsResponse);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<CollectionDto> addCollection(CollectionRequestDto collectionRequest) {
		ResponseWrapper<CollectionDto> responseWrapper = new ResponseWrapper<>();
		CollectionDto collection = null;
		try {
			if (validInputRequest(collectionRequest)) {
				if (Objects.nonNull(collectionRequest) && Objects.nonNull(collectionRequest.getProjectType())
						&& (AppConstants.SBI.equalsIgnoreCase(collectionRequest.getProjectType())
						|| AppConstants.ABIS.equalsIgnoreCase(collectionRequest.getProjectType())
						|| AppConstants.SDK.equalsIgnoreCase(collectionRequest.getProjectType()))) {

					String sbiProjectId = null;
					String sdkProjectId = null;
					String abisProjectId = null;
					List<CollectionEntity> duplicates = null;

					switch (collectionRequest.getProjectType()) {
						case AppConstants.SBI:
							sbiProjectId = collectionRequest.getProjectId();
							duplicates = collectionsRepository.getSbiCollectionByName(collectionRequest.getCollectionName(),
									sbiProjectId, getPartnerId());
							break;
						case AppConstants.SDK:
							sdkProjectId = collectionRequest.getProjectId();
							duplicates = collectionsRepository.getSdkCollectionByName(collectionRequest.getCollectionName(),
									sdkProjectId, getPartnerId());
							break;
						case AppConstants.ABIS:
							abisProjectId = collectionRequest.getProjectId();
							duplicates = collectionsRepository.getAbisCollectionByName(collectionRequest.getCollectionName(),
									abisProjectId, getPartnerId());
							break;
					}

					if (Objects.isNull(duplicates) || duplicates.size() <= 0) {
						String collectionType = collectionRequest.getCollectionType();
						if (collectionType == null || "".equals(collectionType)) {
							collectionType = AppConstants.CUSTOM_COLLECTION;
						}
						CollectionEntity inputEntity = new CollectionEntity();
						inputEntity.setId(RandomIdGenerator.generateUUID(
								collectionRequest.getProjectType().toLowerCase(), "", 36));
						inputEntity.setName(collectionRequest.getCollectionName());
						inputEntity.setSbiProjectId(sbiProjectId);
						inputEntity.setSdkProjectId(sdkProjectId);
						inputEntity.setAbisProjectId(abisProjectId);
						inputEntity.setPartnerId(getPartnerId());
						inputEntity.setOrgName(resourceCacheService.getOrgName(getPartnerId()));
						inputEntity.setCollectionType(collectionType);
						inputEntity.setCrBy(getUserBy());
						inputEntity.setCrDate(LocalDateTime.now());
						inputEntity.setUpBy(null);
						inputEntity.setUpdDate(null);
						inputEntity.setDeleted(false);
						inputEntity.setDelTime(null);
						CollectionEntity outputEntity = collectionsRepository.save(inputEntity);

						collection = objectMapperConfig.objectMapper().convertValue(outputEntity, CollectionDto.class);
						collection.setCollectionId(outputEntity.getId());
						collection.setProjectId(collectionRequest.getProjectId());
						collection.setCrDtimes(outputEntity.getCrDate());
					} else {
						String errorCode = ToolkitErrorCodes.COLLECTION_NAME_EXISTS.getErrorCode();
						String errorMessage = ToolkitErrorCodes.COLLECTION_NAME_EXISTS.getErrorMessage()
								+ duplicates.get(0).getName();
						responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
					}
				} else {
					String errorCode = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode();
					String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage();
					responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
				}
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In saveCollection method of CollectionsService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.COLLECTION_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.COLLECTION_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(postCollectionId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collection);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<List<CollectionTestCaseDto>> addTestCasesForCollection(
			List<CollectionTestCaseDto> inputList) {

		ResponseWrapper<List<CollectionTestCaseDto>> responseWrapper = new ResponseWrapper<>();
		List<CollectionTestCaseDto> responseList = new ArrayList<CollectionTestCaseDto>();
		try {
			if (Objects.nonNull(inputList) && inputList.size() > 0) {
				for (CollectionTestCaseDto dto : inputList) {
					// create entity to save in db
					CollectionTestCaseEntity entity = new CollectionTestCaseEntity();
					entity.setCollectionId(dto.getCollectionId());
					entity.setTestcaseId(dto.getTestCaseId());
					// save in db
					CollectionTestCaseEntity outputEntity = collectionTestCaseRepository.save(entity);
					// create dto to send in response
					CollectionTestCaseDto respDto = new CollectionTestCaseDto();
					respDto.setCollectionId(outputEntity.getCollectionId());
					respDto.setTestCaseId(outputEntity.getTestcaseId());
					responseList.add(respDto);
				}
			} else {
				String errorCode = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode();
				String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			}

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In saveCollectionTestCaseMapping method of CollectionTestCaseService Service - "
							+ ex.getMessage());
			String errorCode = ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(postCollectionTestCaseId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(responseList);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public void addDefaultCollection(String collectionType, SbiProjectDto sbiProjectDto, SdkProjectDto sdkProjectDto,
			AbisProjectDto abisProjectDto, String projectId) {
		log.debug("sessionId", "idType", "id", "Started addDefaultCollection for collectionType: {}", collectionType);
		log.debug("sessionId", "idType", "id", "Started addDefaultCollection for project: {}", projectId);
		try {
			// 1. Add a new default collection
			String projectType = "";
			if (abisProjectDto != null && AppConstants.ABIS.equals(abisProjectDto.getProjectType())) {
				projectType = abisProjectDto.getProjectType();
			}
			if (sbiProjectDto != null && AppConstants.SBI.equals(sbiProjectDto.getProjectType())) {
				projectType = sbiProjectDto.getProjectType();
			}
			if (sdkProjectDto != null && AppConstants.SDK.equals(sdkProjectDto.getProjectType())) {
				projectType = sdkProjectDto.getProjectType();
			}
			log.debug("sessionId", "idType", "id", "Started addDefaultCollection for projectType: {}", projectType);
			CollectionRequestDto collectionRequestDto = new CollectionRequestDto();
			collectionRequestDto.setProjectId(projectId);
			collectionRequestDto.setProjectType(projectType);
			if (AppConstants.COMPLIANCE_COLLECTION.equals(collectionType)) {
				collectionRequestDto.setCollectionName(complianceCollectionName);
			}
			if (AppConstants.QUALITY_ASSESSMENT_COLLECTION.equals(collectionType)) {
				collectionRequestDto.setCollectionName(qualityAssessmentCollectionName);
			}
			collectionRequestDto.setCollectionType(collectionType);
			ResponseWrapper<CollectionDto> addCollectionWrapper = this.addCollection(collectionRequestDto);
			String complianceCollectionId = null;
			if (addCollectionWrapper.getResponse() != null) {
				complianceCollectionId = addCollectionWrapper.getResponse().getCollectionId();
				log.debug("sessionId", "idType", "id", "Default collection added: {}", complianceCollectionId);
			} else {
				log.debug("sessionId", "idType", "id", "Default collection could not be added for this project: {}",
						projectId);
			}
			if (complianceCollectionId != null) {
				// 2. Get the testcases
				ResponseWrapper<List<TestCaseDto>> testCaseWrapper = null;
				if (AppConstants.ABIS.equals(projectType)) {
					testCaseWrapper = testCasesService.getAbisTestCases(abisProjectDto.getAbisVersion());
				}
				if (AppConstants.SBI.equals(projectType)) {
					testCaseWrapper = testCasesService.getSbiTestCases(sbiProjectDto.getSbiVersion(),
							sbiProjectDto.getPurpose(), sbiProjectDto.getDeviceType(),
							sbiProjectDto.getDeviceSubType(), sbiProjectDto.getIsAndroidSbi());
				}
				if (AppConstants.SDK.equals(projectType)) {
					testCaseWrapper = testCasesService.getSdkTestCases(sdkProjectDto.getSdkVersion(),
							sdkProjectDto.getPurpose());
				}
				List<CollectionTestCaseDto> inputList = new ArrayList<>();
				if (testCaseWrapper.getResponse() != null && testCaseWrapper.getResponse().size() > 0) {
					List<String> ignoreTestCasesList = new ArrayList<>();
					if (AppConstants.COMPLIANCE_COLLECTION.equals(collectionType)) {
						ignoreTestCasesList = Arrays.asList(complianceIgnoreTestcases.split(","));
					}
					if (AppConstants.QUALITY_ASSESSMENT_COLLECTION.equals(collectionType)) {
						ignoreTestCasesList = Arrays.asList(qualityAssessmentIgnoreTestcases.split(","));
					}
					for (TestCaseDto testCase : testCaseWrapper.getResponse()) {
						if (!ignoreTestCasesList.contains(testCase.getTestId())) {
							boolean proceed = false;
							if (AppConstants.SBI.equals(projectType)) {
								//skip quality assessment testcases for compliance collection
								if (AppConstants.COMPLIANCE_COLLECTION.equals(collectionType)
										&& testCase.getOtherAttributes() != null
										&& !testCase.getOtherAttributes().isQualityAssessmentTestCase()) {
									proceed = true;
								}
								//add only quality assessment testcases for quality assessment collection
								if (AppConstants.QUALITY_ASSESSMENT_COLLECTION.equals(collectionType)
										&& testCase.getOtherAttributes() != null
										&& testCase.getOtherAttributes().isQualityAssessmentTestCase()) {
									proceed = true;
								}	
							} else {
								proceed = true;
							}
							if (proceed) {
								CollectionTestCaseDto collectionTestCaseDto = new CollectionTestCaseDto();
								collectionTestCaseDto.setCollectionId(complianceCollectionId);
								collectionTestCaseDto.setTestCaseId(testCase.getTestId());
								inputList.add(collectionTestCaseDto);
							}
						}
					}
				}
				// 3. add the testcases for the default collection
				if (inputList.size() > 0) {
					this.addTestCasesForCollection(inputList);
				}
			}
		} catch (Exception ex) {
			// This is a fail safe operation, so exception can be ignored
			log.debug("sessionId", "idType", "id", "Error in adding compliance collection: {}",
					ex.getLocalizedMessage());
		}
	}

	private boolean validInputRequest(CollectionRequestDto collectionRequestDto) {
		String collectionName = collectionRequestDto.getCollectionName();
		if (!Pattern.matches(AppConstants.REGEX_PATTERN, collectionName)) {
			String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
					+ AppConstants.COMMA_SEPARATOR
					+ ToolkitErrorCodes.COLLECTION_NAME.getErrorCode();
			throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in collection name");
		}
		return true;
	}
}
