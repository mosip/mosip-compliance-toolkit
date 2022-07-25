package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	private String getPartnerId() {
		String partnerId = authUserDetails().getUserId();
		return partnerId;
	}
	private String getUserBy() {
		String crBy = authUserDetails().getMail();
		return crBy;
	}
	public ResponseWrapper<SbiProjectResponseDto> getSbiProject(String id) {
		ResponseWrapper<SbiProjectResponseDto> responseWrapper = new ResponseWrapper<>();
		SbiProjectResponseDto sbiProjectResponseDto = new SbiProjectResponseDto();
		Optional<SbiProjectEntity> optionalSbiProjectEntity = sbiProjectRepository.findById(id);
		if(optionalSbiProjectEntity.isPresent()) {
			SbiProjectEntity sbiProjectEntity = optionalSbiProjectEntity.get();
			
			SbiProjectDto sbiProjectDto = new SbiProjectDto();
			sbiProjectDto.setId(sbiProjectEntity.getId());
			sbiProjectDto.setName(sbiProjectEntity.getName());
			sbiProjectDto.setProjectType(sbiProjectEntity.getProjectType());
			sbiProjectDto.setSbiVersion(sbiProjectEntity.getSbiVersion());
			sbiProjectDto.setPurpose(sbiProjectEntity.getPurpose());
			sbiProjectDto.setDeviceType(sbiProjectEntity.getDeviceType());
			sbiProjectDto.setDeviceSubType(sbiProjectEntity.getDeviceSubType());
			sbiProjectDto.setPartnerId(sbiProjectEntity.getPartnerId());
			sbiProjectDto.setCrBy(sbiProjectEntity.getCrBy());
			sbiProjectDto.setCrDate(sbiProjectEntity.getCrDate());
			sbiProjectDto.setUpBy(sbiProjectEntity.getUpBy());
			sbiProjectDto.setUpdDate(sbiProjectEntity.getUpdDate());
			
			List<SbiProjectDto> sbiProjectDtoList = new ArrayList<SbiProjectDto>();
			sbiProjectDtoList.add(sbiProjectDto);
			
			sbiProjectResponseDto.setSbiProjects(sbiProjectDtoList);
			
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setResponse(sbiProjectResponseDto);
		responseWrapper.setVersion(AppConstants.VERSION);		
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
	public ResponseWrapper<SbiProjectDto> addSbiProject(List<SbiProjectDto> values) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		return responseWrapper;
	}
	public ResponseWrapper<SbiProjectDto> saveSbiProject(List<SbiProjectDto> values) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		return responseWrapper;
	}
	public ResponseWrapper<SbiProjectDto> deleteSbiProject(String id) {
		ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
		return responseWrapper;
	}

}
