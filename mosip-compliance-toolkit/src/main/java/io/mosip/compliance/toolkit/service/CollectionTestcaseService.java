package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.CollectionTestcaseDto;
import io.mosip.compliance.toolkit.entity.CollectionTestcaseEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestcaseRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionTestcaseService {
	@Value("${mosip.toolkit.api.id.collection.testcase.post}")
	private String postCollectionTestcaseId;

	@Autowired
	private CollectionTestcaseRepository collectionTestcaseRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	public ResponseWrapper<CollectionTestcaseDto> saveCollectionTestcaseMapping(
			CollectionTestcaseDto inputCollectionTestcase) {
		ResponseWrapper<CollectionTestcaseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestcaseDto collectionTestcase = null;
		try {
			if (Objects.nonNull(inputCollectionTestcase)) {
				ObjectMapper mapper = objectMapperConfig.objectMapper();
				CollectionTestcaseEntity entity = mapper.convertValue(inputCollectionTestcase,
						CollectionTestcaseEntity.class);

				CollectionTestcaseEntity outputEntity = collectionTestcaseRepository.save(entity);

				collectionTestcase = mapper.convertValue(outputEntity, CollectionTestcaseDto.class);
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In saveCollectionTestcaseMapping method of CollectionTestcaseService Service - "
							+ ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postCollectionTestcaseId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collectionTestcase);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}
