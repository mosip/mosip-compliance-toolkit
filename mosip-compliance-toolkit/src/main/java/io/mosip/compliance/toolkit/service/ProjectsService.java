package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.ProjectDto;
import io.mosip.compliance.toolkit.dto.ProjectsResponseDto;
import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.entity.SdkProjectEntity;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.repository.SdkProjectRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ProjectsService {

	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;

	@Autowired
	private SdkProjectRepository sdkProjectRepository;

	@Autowired
	private SbiProjectRepository sbiProjectRepository;

	@Autowired
	private AbisProjectRepository abisProjectRepository;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUserId();
		// TODO: hardcoded partnerId
		partnerId = "1234567890";
		return partnerId;
	}

	public ResponseWrapper<ProjectsResponseDto> getProjects(String type) {
		log.info("sessionId", "idType", "id", "In getProjects method of Projects Service.");
		ResponseWrapper<ProjectsResponseDto> responseWrapper = new ResponseWrapper<>();
		ProjectsResponseDto projectsResponseDto = new ProjectsResponseDto();
		List<ProjectDto> projectsList = new ArrayList<ProjectDto>();

		boolean isProjectTypeValid = false;
		if (Objects.nonNull(type)) {
			if (AppConstants.SBI.equalsIgnoreCase(type) || AppConstants.ABIS.equalsIgnoreCase(type)
					|| AppConstants.SDK.equalsIgnoreCase(type)) {
				isProjectTypeValid = true;
			}
		} else {
			isProjectTypeValid = true;
		}
		try {
			if (!isProjectTypeValid) {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError
						.setErrorCode(io.mosip.compliance.toolkit.exceptions.ErrorCodes.TOOLKIT_REQ_ERR_006.getCode());
				serviceError.setMessage(
						io.mosip.compliance.toolkit.exceptions.ErrorMessages.INVALID_PROJECT_TYPE.getMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			} else {
				String partnerId = this.getPartnerId();
				log.info("fetching projects for partner {}", partnerId);
				boolean fetchAll = false;
				if (type == null || "".equalsIgnoreCase(type.trim())) {
					fetchAll = true;
				}
				if (fetchAll || type.equalsIgnoreCase(AppConstants.SDK)) {
					List<SdkProjectEntity> sdkProjectEntityList = new ArrayList<SdkProjectEntity>();
					sdkProjectEntityList = sdkProjectRepository.findAllByPartnerId(partnerId);
					sdkProjectEntityList.forEach(entity -> {
						ProjectDto projectDto = new ProjectDto();
						projectDto.setId(entity.getId());
						projectDto.setName(entity.getName());
						projectDto.setProjectType(entity.getProjectType());
						projectDto.setCrDate(entity.getCrDate());
						projectsList.add(projectDto);
					});
				}
				if (fetchAll || type.equalsIgnoreCase(AppConstants.SBI)) {
					List<SbiProjectEntity> sbiProjectEntityList = new ArrayList<SbiProjectEntity>();
					sbiProjectEntityList = sbiProjectRepository.findAllByPartnerId(partnerId);
					sbiProjectEntityList.forEach(entity -> {
						ProjectDto projectDto = new ProjectDto();
						projectDto.setId(entity.getId());
						projectDto.setName(entity.getName());
						projectDto.setProjectType(entity.getProjectType());
						projectDto.setCrDate(entity.getCrDate());
						projectsList.add(projectDto);
					});
				}
				if (fetchAll || type.equalsIgnoreCase(AppConstants.ABIS)) {
					List<AbisProjectEntity> abisProjectEntityList = new ArrayList<AbisProjectEntity>();
					abisProjectEntityList = abisProjectRepository.findAllByPartnerId(partnerId);
					abisProjectEntityList.forEach(entity -> {
						ProjectDto projectDto = new ProjectDto();
						projectDto.setId(entity.getId());
						projectDto.setName(entity.getName());
						projectDto.setProjectType(entity.getProjectType());
						projectDto.setCrDate(entity.getCrDate());
						projectsList.add(projectDto);
					});
				}
			}
			projectsResponseDto.setProjects(projectsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In getProjects method of Projects Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(io.mosip.compliance.toolkit.exceptions.ErrorCodes.TOOLKIT_PROJECTS_001.getCode());
			serviceError.setMessage(
					io.mosip.compliance.toolkit.exceptions.ErrorMessages.PROJECTS_NOT_AVAILABLE.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(projectsResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;

	}
}
