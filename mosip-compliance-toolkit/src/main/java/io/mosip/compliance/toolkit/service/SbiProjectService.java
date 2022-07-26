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
import io.mosip.compliance.toolkit.dto.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.SbiProjectResponseDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class SbiProjectService {
	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;
	@Autowired
	private SbiProjectRepository sbiProjectRepository;
	private Logger log = LoggerConfiguration.logConfig(SbiProjectService.class);

	public ResponseWrapper<SbiProjectDto> getSbiProject(String id) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		SbiProjectDto sbiProjectDto = new SbiProjectDto();
		Optional<SbiProjectEntity> optionalSbiProjectEntity = sbiProjectRepository.findById(id);
		if(optionalSbiProjectEntity.isPresent()) {
			SbiProjectEntity sbiProjectEntity = optionalSbiProjectEntity.get();
			
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JavaTimeModule());
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			sbiProjectDto = objectMapper.convertValue(sbiProjectEntity, SbiProjectDto.class); 
			
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setResponse(sbiProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}
