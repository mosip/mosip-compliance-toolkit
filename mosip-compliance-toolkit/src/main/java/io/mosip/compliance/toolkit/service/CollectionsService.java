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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.CollectionTestRunDto;
import io.mosip.compliance.toolkit.dto.CollectionTestRunResponseDto;
import io.mosip.compliance.toolkit.dto.TestcaseCollectionDto;
import io.mosip.compliance.toolkit.dto.TestcaseJsonDto;
import io.mosip.compliance.toolkit.dto.TestcasesCollectionResponseDto;
import io.mosip.compliance.toolkit.entity.CollectionTestrunEntity;
import io.mosip.compliance.toolkit.entity.TestcaseCollectionEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestrunRepository;
import io.mosip.compliance.toolkit.repository.TestcaseCollectionRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionsService {

	@Value("${mosip.toolkit.api.id.collections.get}")
	private String getCollectionsId;

	@Value("${mosip.toolkit.api.id.collection.testcases.get}")
	private String getTestCasesByCollectionId;

	@Autowired
	private CollectionTestrunRepository collectionTestrunRepository;

	@Autowired
	private TestcaseCollectionRepository testcaseCollectionRepository;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseWrapper<TestcasesCollectionResponseDto> getTestcasesByCollectionId(String id) {
		ResponseWrapper<TestcasesCollectionResponseDto> responseWrapper = new ResponseWrapper<>();
		TestcasesCollectionResponseDto testcasesCollectionResponseDto = null;
		try {
			List<TestcaseCollectionEntity> testcaseCollectionEntityList = testcaseCollectionRepository
					.getTestcasesByCollectionId(id, getPartnerId());

			if (Objects.nonNull(testcaseCollectionEntityList) && !testcaseCollectionEntityList.isEmpty()) {
				List<TestcaseCollectionDto> testcaseCollectionDtoList = new ArrayList<>();
				for (TestcaseCollectionEntity entity : testcaseCollectionEntityList) {
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.registerModule(new JavaTimeModule());
					objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

					TestcaseCollectionDto testcaseCollectionDto = new TestcaseCollectionDto();
					testcaseCollectionDto.setCollectionId(entity.getCollectionId());
					testcaseCollectionDto.setTestCaseId(entity.getTestCaseId());
					TestcaseJsonDto testCaseConfig = objectMapper.readValue(entity.getTestcaseJson(),
							TestcaseJsonDto.class);
					testcaseCollectionDto.setTestcaseJson(testCaseConfig);
					testcaseCollectionDto.setTestcaseType(entity.getTestcaseType());
					testcaseCollectionDto.setSpecVersion(entity.getSpecVersion());

					testcaseCollectionDtoList.add(testcaseCollectionDto);
				}

				testcasesCollectionResponseDto = new TestcasesCollectionResponseDto();
				testcasesCollectionResponseDto.setTestcaseCollectionDto(testcaseCollectionDtoList);
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getTestcasesByCollectionId method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.COLLECTION_TESTCASES_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestCasesByCollectionId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testcasesCollectionResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<CollectionTestRunResponseDto> getProjectCollectionTestrun(String type, String projectId) {
		ResponseWrapper<CollectionTestRunResponseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestRunResponseDto collectionTestRunResponseDto = null;
		boolean isProjectTypeValid = false;

		try {
			if (AppConstants.SBI.equalsIgnoreCase(type) || AppConstants.ABIS.equalsIgnoreCase(type)
					|| AppConstants.SDK.equalsIgnoreCase(type)) {
				isProjectTypeValid = true;
			}

			if (isProjectTypeValid) {
				if (Objects.nonNull(type) && Objects.nonNull(projectId)) {
					List<CollectionTestrunEntity> collectionsEntityList = null;
					if (AppConstants.SBI.equalsIgnoreCase(type)) {
						collectionsEntityList = collectionTestrunRepository.getCollectionsOfSbiProjects(projectId,
								getPartnerId());
					} else if (AppConstants.SDK.equalsIgnoreCase(type)) {
						collectionsEntityList = collectionTestrunRepository.getCollectionsOfSdkProjects(projectId,
								getPartnerId());
					} else if (AppConstants.ABIS.equalsIgnoreCase(type)) {
						collectionsEntityList = collectionTestrunRepository.getCollectionsOfAbisProjects(projectId,
								getPartnerId());
					}

					if (Objects.nonNull(collectionsEntityList) && !collectionsEntityList.isEmpty()) {
						List<CollectionTestRunDto> collectionTestRunDtoList = new ArrayList<>();
						for (CollectionTestrunEntity entity : collectionsEntityList) {

							ObjectMapper objectMapper = new ObjectMapper();
							objectMapper.registerModule(new JavaTimeModule());
							objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
							CollectionTestRunDto collectionTestRunDto = objectMapper.convertValue(entity,
									CollectionTestRunDto.class);

							collectionTestRunDtoList.add(collectionTestRunDto);
						}
						collectionTestRunResponseDto = new CollectionTestRunResponseDto();
						collectionTestRunResponseDto.setCollectionTestrunDtoList(collectionTestRunDtoList);
					} else {
						List<ServiceError> serviceErrorsList = new ArrayList<>();
						ServiceError serviceError = new ServiceError();
						serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
						serviceError.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage());
						serviceErrorsList.add(serviceError);
						responseWrapper.setErrors(serviceErrorsList);
					}

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
		responseWrapper.setResponse(collectionTestRunResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}
