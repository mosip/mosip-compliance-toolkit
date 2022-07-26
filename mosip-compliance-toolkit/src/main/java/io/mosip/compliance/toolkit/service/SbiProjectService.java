package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCode;
import io.mosip.compliance.toolkit.dto.SbiProjectDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
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
		String partnerId = authUserDetails().getUserId();
		// TODO: hardcoded partnerId
		partnerId = "1234567890";
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
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In getSbiProject method of SbiProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCode.PROJECTS_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCode.PROJECTS_NOT_AVAILABLE.getErrorMessage()+ " " + ex.getMessage());
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
