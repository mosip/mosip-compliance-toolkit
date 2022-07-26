package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.sbi.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.sbi.SbiProjectResponseDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.exceptions.TestCaseException;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
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

	public ResponseWrapper<SbiProjectResponseDto> getSbiProject(String id) {
		ResponseWrapper<SbiProjectResponseDto> responseWrapper = new ResponseWrapper<>();
		return responseWrapper;

	}

	public ResponseWrapper<SbiProjectResponseDto> addSbiProject(SbiProjectDto sbiProjectDto) {
		ResponseWrapper<SbiProjectResponseDto> responseWrapper = new ResponseWrapper<>();
		
		SbiProjectResponseDto responseDto = new SbiProjectResponseDto();
		List<SbiProjectDto> projectsList = new ArrayList<SbiProjectDto>();

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

			SbiProjectDto projectDto = new SbiProjectDto();
			projectDto.setId(entity.getId());
			projectDto.setName(entity.getName());
			projectDto.setProjectType(entity.getProjectType());
			projectDto.setSbiVersion(entity.getSbiVersion());
			projectDto.setPurpose(entity.getPurpose());
			projectDto.setDeviceType(entity.getDeviceType());
			projectDto.setDeviceSubType(entity.getDeviceSubType());
			projectDto.setPartnerId(entity.getPartnerId());
			projectDto.setCrBy(entity.getCrBy());
			projectDto.setCrDate(entity.getCrDate());

			projectsList.add(projectDto);
			responseDto.setSbiProjects(projectsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In addSbiProject method of Sbi Projects Service - " + ex.getMessage());

			ToolkitErrorCodes errorCode = null; 
			errorCode = ToolkitErrorCodes.INVALID_SBI_PROJECT_DETAILS;
			throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(responseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<SbiProjectResponseDto> saveSbiProject(SbiProjectDto sbiProjectDto) {
		ResponseWrapper<SbiProjectResponseDto> responseWrapper = new ResponseWrapper<>();
		
		SbiProjectResponseDto responseDto = new SbiProjectResponseDto();
		List<SbiProjectDto> projectsList = new ArrayList<SbiProjectDto>();

		try
		{
			List<SbiProjectEntity> sbiProjectEntityList = sbiProjectRepository.findAllByPartnerId(this.getPartnerId(), sbiProjectDto.getId());
			if (!sbiProjectEntityList.isEmpty() || sbiProjectEntityList.size() > 0) {
				SbiProjectEntity entity = sbiProjectEntityList.get(0);
				entity.setName(sbiProjectDto.getName());
				entity.setProjectType(sbiProjectDto.getProjectType());

				entity.setSbiVersion(sbiProjectDto.getSbiVersion());
				entity.setPurpose(sbiProjectDto.getPurpose());
				entity.setDeviceType(sbiProjectDto.getDeviceType());
				entity.setDeviceSubType(sbiProjectDto.getDeviceSubType());
				entity.setPartnerId(this.getPartnerId());
				entity.setUpBy(this.getUserBy());
				entity.setUpdDate(LocalDateTime.now());

				sbiProjectRepository.update(entity);

				SbiProjectDto projectDto = new SbiProjectDto();
				projectDto.setId(entity.getId());
				projectDto.setName(entity.getName());
				projectDto.setProjectType(entity.getProjectType());
				projectDto.setSbiVersion(entity.getSbiVersion());
				projectDto.setPurpose(entity.getPurpose());
				projectDto.setDeviceType(entity.getDeviceType());
				projectDto.setDeviceSubType(entity.getDeviceSubType());
				projectDto.setPartnerId(entity.getPartnerId());
				projectDto.setCrBy(entity.getCrBy());
				projectDto.setCrDate(entity.getCrDate());
				projectDto.setUpBy(entity.getUpBy());
				projectDto.setUpdDate(entity.getUpdDate());

				projectsList.add(projectDto);
				responseDto.setSbiProjects(projectsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In saveSbiProject method of Sbi Projects Service - " + ex.getMessage());

			ToolkitErrorCodes errorCode = null; 
			errorCode = ToolkitErrorCodes.INVALID_SBI_PROJECT_DETAILS;
			throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(responseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}	
}
/*
 public ResponseWrapper<SbiProjectResponseDto> deleteSbiProject(String projectId) {
		ResponseWrapper<SbiProjectResponseDto> responseWrapper = new ResponseWrapper<>();		
		SbiProjectResponseDto responseDto = new SbiProjectResponseDto();
		List<SbiProjectDto> sbiProjectList = new ArrayList<SbiProjectDto>();

		try
		{
			SbiProjectDto sbiProjectDto = null;
		
			Optional<SbiProjectEntity> entity = sbiProjectRepository.findById(projectId);
			if (!entity.isEmpty() || entity.isPresent()) {
				SbiProjectEntity sbiProjectEntity = entity.get();
				LocalDateTime delDtimes = LocalDateTime.now();
				//soft delete from collections
				List<CollectionEntity> collectionEntities = collectionRepository.findAllBySbiProjectId(sbiProjectEntity.getId());
				for(int collectionIndex=0; collectionIndex < collectionEntities.size(); collectionIndex++) {
					CollectionEntity collectionEntity = collectionEntities.get(collectionIndex);
					String collectionId = collectionEntity.getId();
					//soft delete from test_run
					List<TestRunEntity> testRunEntities = testRunRepository.findAllByCollectionId(collectionId);
					for(int testRunIndex=0; testRunIndex < testRunEntities.size(); testRunIndex++) {
						TestRunEntity testRunEntity = testRunEntities.get(testRunIndex);
						String testCaseId = testRunEntity.getId();
						//soft delete from test_run_details
						List<TestRunDetailsEntity> testRunDetailsEntities = testRunDetailsRepository.findAllByTestCaseId(testCaseId);
						testRunDetailsRepository.deleteByTestCaseId(delDtimes, testCaseId);
			        }					
					testRunRepository.deleteByCollectionId(delDtimes, collectionId);
		        }
				collectionRepository.deleteBySbiProjectId(delDtimes, projectId);
				//soft delete from sbi_projects
				sbiProjectRepository.deleteByProjectId(delDtimes, projectId);
				
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
				sbiProjectList.add(sbiProjectDto);
				responseDto.setSbiProjects(sbiProjectList);
			}
			else
			{
				ToolkitErrorCodes errorCode = null; 
				errorCode = ToolkitErrorCodes.ERROR_DELETING_SBI_PROJECT_DETAILS;
				throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In deleteSbiProject method of Sbi Projects Service - " + ex.getMessage());

			ToolkitErrorCodes errorCode = null; 
			errorCode = ToolkitErrorCodes.INVALID_SBI_PROJECT_DETAILS;
			throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(responseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
 */
