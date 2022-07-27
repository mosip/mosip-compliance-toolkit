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
import io.mosip.compliance.toolkit.constants.ToolkitErrorCode;
import io.mosip.compliance.toolkit.dto.CollectionTestRunDto;
import io.mosip.compliance.toolkit.dto.CollectionTestRunResponseDto;
import io.mosip.compliance.toolkit.entity.CollectionsSummaryEntity;
import io.mosip.compliance.toolkit.repository.CollectionsSummaryRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionsService {
	
	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;

	@Autowired
	private CollectionsSummaryRepository collectionSummaryRepository;

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
		CollectionTestRunResponseDto collectionsSummaryResponseDto = new CollectionTestRunResponseDto();
		List<CollectionsSummaryEntity> collectionsEntityList = new ArrayList<>();
		
		try {
		if (Objects.nonNull(type) && Objects.nonNull(projectId)) {
			if (AppConstants.SBI.equalsIgnoreCase(type)) {
				collectionsEntityList = collectionSummaryRepository.getCollectionsOfSbiProjects(projectId, getPartnerId());
			}else if(AppConstants.SDK.equalsIgnoreCase(type)) {
				collectionsEntityList = collectionSummaryRepository.getCollectionsOfSdkProjects(projectId, getPartnerId());
			}else if(AppConstants.ABIS.equalsIgnoreCase(type)) {
				collectionsEntityList = collectionSummaryRepository.getCollectionsOfAbisProjects(projectId, getPartnerId());
			}else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCode.INVALID_PROJECT_TYPE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCode.INVALID_PROJECT_TYPE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		}
		
		if(!collectionsEntityList.isEmpty()) {
			List<CollectionTestRunDto> collectionsSummaryDtoList = new ArrayList<>();
			for(CollectionsSummaryEntity entity: collectionsEntityList) {
				
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				CollectionTestRunDto collectionsSummaryDto = objectMapper.convertValue(entity, CollectionTestRunDto.class);
				
				collectionsSummaryDtoList.add(collectionsSummaryDto);
			}
			
			collectionsSummaryResponseDto.setCollectionsSummaryList(collectionsSummaryDtoList);
		}
		}catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id", "In getProjectCollectionsSummary method of CollectionsService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCode.COLLECTION_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCode.COLLECTION_NOT_AVAILABLE.getErrorMessage()+ " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collectionsSummaryResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
	
	

}
