package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.compliance.toolkit.entity.CollectionSummaryEntity;
import io.mosip.compliance.toolkit.entity.CollectionTestCaseEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestCaseRepository;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.CollectionsSummaryRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
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

	@Autowired
	private CollectionsSummaryRepository collectionsSummaryRepository;

	@Autowired
	private CollectionTestCaseRepository collectionTestCaseRepository;

	@Autowired
	private CollectionsRepository collectionsRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

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

	public ResponseWrapper<CollectionDto> getCollectionById(String collectionId) {
		ResponseWrapper<CollectionDto> responseWrapper = new ResponseWrapper<>();
		CollectionDto collection = null;
		try {
			CollectionEntity collectionEntity = collectionsRepository.getCollectionById(collectionId, getPartnerId());

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
				collection.setCrDtimes(collectionEntity.getCrDate());
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getCollectionByCollectionId method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getCollectionId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collection);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<CollectionTestCasesResponseDto> getTestCasesForCollection(String collectionId) {
		ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestCasesResponseDto collectionTestCasesResponseDto = null;

		try {
			if (Objects.nonNull(collectionId)) {
				List<String> testCases = collectionTestCaseRepository.getTestCasesByCollectionId(collectionId,
						getPartnerId());

				if (Objects.nonNull(testCases) && !testCases.isEmpty()) {
					List<TestCaseDto> collectionTestCases = new ArrayList<>(testCases.size());
					ObjectMapper mapper = new ObjectMapper();
			        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
			        mapper.registerModule(new JavaTimeModule());
					for (String testCase : testCases) {
						collectionTestCases
								.add(mapper.readValue(testCase, TestCaseDto.class));
					}
					collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
					collectionTestCasesResponseDto.setCollectionId(collectionId);
					collectionTestCasesResponseDto.setTestcases(collectionTestCases);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getCollectionTestCases method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
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
					|| AppConstants.ABIS.equalsIgnoreCase(projectType) || AppConstants.SDK.equalsIgnoreCase(projectType))) {
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
							CollectionDto collection = mapper.convertValue(entity,
									CollectionDto.class);

							collectionsList.add(collection);
						}
					} 
					// send empty collections list, in case none are yet added for a project
					collectionsResponse = new CollectionsResponseDto();
					collectionsResponse.setCollections(collectionsList);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getProjectCollectionTestrun method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
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
					duplicates = collectionsRepository.getSbiCollectionByName(
							collectionRequest.getCollectionName(), sbiProjectId, getPartnerId());
					break;
				case AppConstants.SDK:
					sdkProjectId = collectionRequest.getProjectId();
					duplicates = collectionsRepository.getSdkCollectionByName(
							collectionRequest.getCollectionName(), sdkProjectId, getPartnerId());
					break;
				case AppConstants.ABIS:
					abisProjectId = collectionRequest.getProjectId();
					duplicates = collectionsRepository.getAbisCollectionByName(
							collectionRequest.getCollectionName(), abisProjectId, getPartnerId());
					break;
				}

				if (Objects.isNull(duplicates) || duplicates.size() <= 0) {
					CollectionEntity inputEntity = new CollectionEntity();
					inputEntity.setId(
							RandomIdGenerator.generateUUID(collectionRequest.getProjectType().toLowerCase(), "", 36));
					inputEntity.setName(collectionRequest.getCollectionName());
					inputEntity.setSbiProjectId(sbiProjectId);
					inputEntity.setSdkProjectId(sdkProjectId);
					inputEntity.setAbisProjectId(abisProjectId);
					inputEntity.setPartnerId(getPartnerId());
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
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NAME_EXISTS.getErrorCode());
					serviceError.setMessage(
							ToolkitErrorCodes.COLLECTION_NAME_EXISTS.getErrorMessage() + duplicates.get(0).getName());
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
					"In saveCollection method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_UNABLE_TO_ADD.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.COLLECTION_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
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
					//create entity to save in db
					CollectionTestCaseEntity entity = new CollectionTestCaseEntity();
					entity.setCollectionId(dto.getCollectionId());
					entity.setTestcaseId(dto.getTestCaseId());
					//save in db
					CollectionTestCaseEntity outputEntity = collectionTestCaseRepository.save(entity);
					//create dto to send in response
					CollectionTestCaseDto respDto = new CollectionTestCaseDto();
					respDto.setCollectionId(outputEntity.getCollectionId());
					respDto.setTestCaseId(outputEntity.getTestcaseId());
					responseList.add(respDto);
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
					"In saveCollectionTestCaseMapping method of CollectionTestCaseService Service - "
							+ ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postCollectionTestCaseId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(responseList);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}


}
