package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.compliance.toolkit.util.CommonUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.projects.ProjectDto;
import io.mosip.compliance.toolkit.dto.projects.ProjectsResponseDto;
import io.mosip.compliance.toolkit.entity.ProjectSummaryEntity;
import io.mosip.compliance.toolkit.repository.ProjectSummaryRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;


/**
 * This service class defines services for all the endpoints for all projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Component
public class ProjectsService {

	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;

	@Autowired
	private ProjectSummaryRepository projectSummaryRepository;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseWrapper<ProjectsResponseDto> getProjects(String type) {
		log.info("sessionId", "idType", "id", "In getProjects method of Projects Service.");
		ResponseWrapper<ProjectsResponseDto> responseWrapper = new ResponseWrapper<>();
		ProjectsResponseDto projectsResponseDto = new ProjectsResponseDto();
		List<ProjectDto> projectsList = new ArrayList<ProjectDto>();

		boolean isProjectTypeValid = false;
		ToolkitErrorCodes errorCode = null;
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
				errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
				String errorMessage = ToolkitErrorCodes.INVALID_PROJECT_TYPE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode.getErrorCode(),errorMessage));
			} else {
				String partnerId = this.getPartnerId();
				boolean fetchAll = false;
				if (type == null || "".equalsIgnoreCase(type.trim())) {
					fetchAll = true;
				}
				List<ProjectSummaryEntity> projectsSummaryList = null;
				if (fetchAll) {
					log.info("sessionId", "idType", "id", "fetching ALL projects for partner {}", partnerId);
					projectsSummaryList = projectSummaryRepository.getSummaryOfAllProjects(partnerId);
				} else if (type.equalsIgnoreCase(AppConstants.SDK)) {
					log.info("sessionId", "idType", "id", "fetching SDK projects for partner {}", partnerId);
					projectsSummaryList = projectSummaryRepository.getSummaryOfAllSDKProjects(partnerId);
				} else if (type.equalsIgnoreCase(AppConstants.SBI)) {
					log.info("sessionId", "idType", "id", "fetching SBI projects for partner {}", partnerId);
					projectsSummaryList = projectSummaryRepository.getSummaryOfAllSBIProjects(partnerId);
				} else if (type.equalsIgnoreCase(AppConstants.ABIS)) {
					log.info("sessionId", "idType", "id", "fetching ABIS projects for partner {}", partnerId);
					projectsSummaryList = projectSummaryRepository.getSummaryOfAllABISProjects(partnerId);
				}
				if (projectsSummaryList != null) {
					log.info("sessionId", "idType", "id", "number of projects found {}", projectsSummaryList.size());
					projectsSummaryList.forEach(entity -> {
						ProjectDto projectDto = new ProjectDto();
						projectDto.setId(entity.getProjectId());
						projectDto.setName(entity.getProjectName());
						projectDto.setProjectType(entity.getProjectType());
						projectDto.setCollectionsCount(entity.getCollectionsCount());
						projectDto.setCrDate(entity.getProjectCrDate());
						projectDto.setLastRunDt(entity.getRunDate());
						projectDto.setLastRunId(entity.getRunId());
						projectsList.add(projectDto);
					});
				}
			}
			projectsResponseDto.setProjects(projectsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In getProjects method of Projects Service - " + ex.getMessage());
			errorCode = ToolkitErrorCodes.PROJECTS_NOT_AVAILABLE;
			String errorMessage = ToolkitErrorCodes.PROJECTS_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode.getErrorCode(),errorMessage));
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(projectsResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
