package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class AbisProjectService {

	@Value("${mosip.toolkit.api.id.abis.project.get}")
	private String getAbisProjectId;
	@Value("${mosip.toolkit.api.id.abis.project.post}")
	private String getAbisProjectPostId;
	@Value("${mosip.toolkit.api.id.abis.project.put}")
	private String putAbisProjectId;

	@Autowired
	private AbisProjectRepository abisProjectRepository;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(AbisProjectService.class);

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

	public ResponseWrapper<AbisProjectDto> getAbisProject(String id) {
		ResponseWrapper<AbisProjectDto> responseWrapper = new ResponseWrapper<>();
		AbisProjectDto abisProjectDto = null;
		try {
			Optional<AbisProjectEntity> optionalProjectEntity = abisProjectRepository.findById(id, getPartnerId());
			if (optionalProjectEntity.isPresent()) {
				AbisProjectEntity abisProjectEntity = optionalProjectEntity.get();

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				abisProjectDto = objectMapper.convertValue(abisProjectEntity, AbisProjectDto.class);

			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getAbisProject method of AbisProjectService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getAbisProjectId);
		responseWrapper.setResponse(abisProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}
