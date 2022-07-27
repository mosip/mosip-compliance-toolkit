package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.SbiProjectDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class SbiProjectService {
	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;
	@Autowired
	private SbiProjectRepository sbiProjectRepository;
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
		SbiProjectDto sbiProjectDto = new SbiProjectDto();
		try {
			Optional<SbiProjectEntity> optionalSbiProjectEntity = sbiProjectRepository.findById(id, getPartnerId());
			if(optionalSbiProjectEntity.isPresent()) {
				SbiProjectEntity sbiProjectEntity = optionalSbiProjectEntity.get();

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				sbiProjectDto = objectMapper.convertValue(sbiProjectEntity, SbiProjectDto.class); 
			}
		}catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id", "In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.SBI_PROJECT_NOT_AVAILABLE.getErrorMessage()+ " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SbiProjectDto> addSbiProject(SbiProjectDto sbiProjectDto) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		
		try
		{
			LocalDateTime crDate = LocalDateTime.now();
			SbiProjectEntity entity = new SbiProjectEntity ();
			entity.setId(RandomIdGenerator.generateUUID(sbiProjectDto.getProjectType().toLowerCase(), "", 36));
			entity.setName(sbiProjectDto.getName());
			entity.setProjectType(sbiProjectDto.getProjectType());
			entity.setSbiVersion(sbiProjectDto.getSbiVersion());
			entity.setPurpose(sbiProjectDto.getPurpose());
			entity.setDeviceType(sbiProjectDto.getDeviceType());
			entity.setDeviceSubType(sbiProjectDto.getDeviceSubType());
			entity.setPartnerId(this.getPartnerId());
			entity.setCrBy(this.getUserBy());
			entity.setCrDate(crDate);
			entity.setDeleted(false);
			
			sbiProjectRepository.save(entity);

			sbiProjectDto.setId(entity.getId());
			sbiProjectDto.setPartnerId(entity.getPartnerId());
			sbiProjectDto.setCrBy(entity.getCrBy());
			sbiProjectDto.setCrDate(entity.getCrDate());
		}catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id", "In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.SAVE_SBI_PROJECT_DETAILS_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.SAVE_SBI_PROJECT_DETAILS_ERROR.getErrorMessage()+ " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
