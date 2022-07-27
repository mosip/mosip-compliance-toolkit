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
import io.mosip.compliance.toolkit.entity.CollectionTestrunEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestrunRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionsService {

	@Value("${mosip.toolkit.api.id.collections.get}")
	private String getCollectionsId;

	@Autowired
	private CollectionTestrunRepository collectionSummaryRepository;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseWrapper<CollectionTestRunResponseDto> getProjectCollectionsSummary(String type, String projectId){
		ResponseWrapper<CollectionTestRunResponseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestRunResponseDto collectionTestRunResponseDto = null;
		boolean isProjectTypeValid = false;
		
		if (AppConstants.SBI.equalsIgnoreCase(type) || AppConstants.ABIS.equalsIgnoreCase(type)
				|| AppConstants.SDK.equalsIgnoreCase(type)) {
			isProjectTypeValid = true;
		}
		
		if(isProjectTypeValid) {
			if (Objects.nonNull(type) && Objects.nonNull(projectId)) {
				List<CollectionTestrunEntity> collectionsEntityList = null;
				if (AppConstants.SBI.equalsIgnoreCase(type)) {
					collectionsEntityList = collectionSummaryRepository.getCollectionsOfSbiProjects(projectId, getPartnerId());
				}else if(AppConstants.SDK.equalsIgnoreCase(type)) {
					collectionsEntityList = collectionSummaryRepository.getCollectionsOfSdkProjects(projectId, getPartnerId());
				}else if(AppConstants.ABIS.equalsIgnoreCase(type)) {
					collectionsEntityList = collectionSummaryRepository.getCollectionsOfAbisProjects(projectId, getPartnerId());
				}
				
				if(Objects.nonNull(collectionsEntityList) && !collectionsEntityList.isEmpty()) {
					List<CollectionTestRunDto> collectionTestRunDtoList = new ArrayList<>();
					for(CollectionTestrunEntity entity: collectionsEntityList) {

						ObjectMapper objectMapper = new ObjectMapper();
						objectMapper.registerModule(new JavaTimeModule());
						objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
						CollectionTestRunDto collectionTestRunDto = objectMapper.convertValue(entity, CollectionTestRunDto.class);

						collectionTestRunDtoList.add(collectionTestRunDto);
					}
					collectionTestRunResponseDto = new CollectionTestRunResponseDto();
					collectionTestRunResponseDto.setCollectionsSummaryList(collectionTestRunDtoList);
				}else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
				
			}else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		}else {
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}

		try {
			

		}catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id", "In getProjectCollectionsSummary method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.COLLECTION_NOT_AVAILABLE.getErrorMessage()+ " " + ex.getMessage());
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
