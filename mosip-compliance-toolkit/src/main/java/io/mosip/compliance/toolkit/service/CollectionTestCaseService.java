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
import io.mosip.compliance.toolkit.dto.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.entity.CollectionTestCaseEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestCaseRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class CollectionTestCaseService {
	@Value("${mosip.toolkit.api.id.collection.testcase.post}")
	private String postCollectionTestCaseId;

	@Autowired
	private CollectionTestCaseRepository collectionTestCaseRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	public ResponseWrapper<CollectionTestCaseDto> saveCollectionTestCaseMapping(
			CollectionTestCaseDto inputCollectionTestCase) {
		ResponseWrapper<CollectionTestCaseDto> responseWrapper = new ResponseWrapper<>();
		CollectionTestCaseDto collectionTestCase = null;
		try {
			if (Objects.nonNull(inputCollectionTestCase)) {
				ObjectMapper mapper = objectMapperConfig.objectMapper();
				CollectionTestCaseEntity entity = mapper.convertValue(inputCollectionTestCase,
						CollectionTestCaseEntity.class);

				CollectionTestCaseEntity outputEntity = collectionTestCaseRepository.save(entity);

				collectionTestCase = mapper.convertValue(outputEntity, CollectionTestCaseDto.class);
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
					"In saveCollectionTestCaseMapping method of CollectionTestCaseService Service - "
							+ ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.COLLECTION_TESTCASE_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postCollectionTestCaseId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(collectionTestCase);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

}
