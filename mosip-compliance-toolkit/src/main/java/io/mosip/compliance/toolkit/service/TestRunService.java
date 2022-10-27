package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunStatusDto;
import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.compliance.toolkit.entity.TestRunHistoryEntity;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.TestRunDetailsRepository;
import io.mosip.compliance.toolkit.repository.TestRunRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class TestRunService {

	@Value("${mosip.toolkit.api.id.testrun.post}")
	private String postTestRunId;

	@Value("${mosip.toolkit.api.id.testrun.put}")
	private String putTestRunId;

	@Value("${mosip.toolkit.api.id.testrun.details.post}")

	private String postTestRunDetailsId;

	@Value("${mosip.toolkit.api.id.testrun.details.get}")
	private String getTestRunDetailsId;

	@Value("${mosip.toolkit.api.id.testrun.history.get}")
	private String getTestRunHistoryId;

	@Value("${mosip.toolkit.api.id.testrun.status.get}")
	private String getTestRunStatusId;
	
	@Value("${mosip.toolkit.testrun.archive.offset}")
	private int archiveOffset;

	@Autowired
	TestRunArchiveService testRunArchiveService;
	
	@Autowired
	TestRunRepository testRunRepository;
	
	@Autowired
	TestRunDetailsRepository testRunDetailsRepository;

	@Autowired
	CollectionsRepository collectionsRepository;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(TestRunService.class);

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

	public ResponseWrapper<TestRunDto> addTestRun(TestRunDto inputTestRun) {
		ResponseWrapper<TestRunDto> responseWrapper = new ResponseWrapper<TestRunDto>();
		TestRunDto testRun = null;
		try {
			if (Objects.nonNull(inputTestRun)) {
				log.info("addTestRun" + inputTestRun);
				ToolkitErrorCodes toolkitError = validatePartnerId(inputTestRun.getCollectionId(), getPartnerId());
				if (ToolkitErrorCodes.SUCCESS.equals(toolkitError)) {

					ObjectMapper mapper = objectMapperConfig.objectMapper();
					TestRunEntity entity = mapper.convertValue(inputTestRun, TestRunEntity.class);
					String collectionId = inputTestRun.getCollectionId();
					entity.setId(RandomIdGenerator.generateUUID(
							collectionId.substring(0, Math.min(5, collectionId.length())).toLowerCase(), "", 36));
					entity.setRunDtimes(LocalDateTime.now());
					entity.setPartnerId(getPartnerId());
					entity.setCrBy(getUserBy());
					entity.setCrDtimes(LocalDateTime.now());
					entity.setUpdBy(null);
					entity.setUpdDtimes(null);
					entity.setDeleted(false);
					entity.setDelTime(null);
					TestRunEntity outputEntity = testRunRepository.save(entity);

					testRun = mapper.convertValue(outputEntity, TestRunDto.class);
					log.info("outputEntity" + outputEntity);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(toolkitError.getErrorCode());
					serviceError.setMessage(toolkitError.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
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
					"In addTestRun method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.TESTRUN_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postTestRunId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRun);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunDto> updateTestRunExecutionTime(TestRunDto inputTestRun) {
		ResponseWrapper<TestRunDto> responseWrapper = new ResponseWrapper<TestRunDto>();
		TestRunDto testRun = null;
		try {
			if (Objects.nonNull(inputTestRun)) {
				int updateRowCount = testRunRepository.updateExecutionDateById(inputTestRun.getExecutionDtimes(),
						getUserBy(), LocalDateTime.now(), inputTestRun.getId(), getPartnerId());
				if (updateRowCount > 0) {
					testRun = inputTestRun;
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
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
					"In updateTestRun method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(putTestRunId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRun);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunDetailsDto> addTestRunDetails(TestRunDetailsDto inputTestRunDetails) {
		ResponseWrapper<TestRunDetailsDto> responseWrapper = new ResponseWrapper<>();
		TestRunDetailsDto testRunDetails = null;
		try {
			if (Objects.nonNull(inputTestRunDetails)) {
				ToolkitErrorCodes toolkitError = validateRunId(inputTestRunDetails.getRunId(),
						getPartnerId());
				if (ToolkitErrorCodes.SUCCESS.equals(toolkitError)) {
					ObjectMapper mapper = objectMapperConfig.objectMapper();

					TestRunDetailsEntity entity = mapper.convertValue(inputTestRunDetails, TestRunDetailsEntity.class);
					entity.setCrBy(getUserBy());
					entity.setCrDtimes(LocalDateTime.now());
					entity.setUpdBy(null);
					entity.setUpdDtimes(null);
					entity.setDeleted(false);
					entity.setDelTime(null);
					entity.setPartnerId(getPartnerId());	
					TestRunDetailsEntity outputEntity = testRunDetailsRepository.save(entity);

					testRunDetails = mapper.convertValue(outputEntity, TestRunDetailsDto.class);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(toolkitError.getErrorCode());
					serviceError.setMessage(toolkitError.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
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
					"In addTestRunDetails method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_DETAILS_UNABLE_TO_ADD.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.TESTRUN_DETAILS_UNABLE_TO_ADD.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(postTestRunDetailsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunDetails);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String runId) {
		ResponseWrapper<TestRunDetailsResponseDto> responseWrapper = new ResponseWrapper<>();
		TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
		try {
			List<TestRunDetailsDto> testRunDetailsList = new ArrayList<TestRunDetailsDto>();
			if (Objects.nonNull(runId)) {
				TestRunEntity testRunEntity = testRunRepository.getTestRunById(runId, getPartnerId());
				if (Objects.nonNull(testRunEntity)) {
					List<TestRunDetailsEntity> testRunDetailsEntityList = testRunDetailsRepository
							.getTestRunDetails(runId, getPartnerId());
					if (Objects.nonNull(testRunDetailsEntityList) && !testRunDetailsEntityList.isEmpty()) {
						ObjectMapper mapper = objectMapperConfig.objectMapper();
						for (TestRunDetailsEntity testRunDetailsEntity : testRunDetailsEntityList) {
							TestRunDetailsDto dto = mapper.convertValue(testRunDetailsEntity, TestRunDetailsDto.class);
							testRunDetailsList.add(dto);
						}
					}
					testRunDetailsResponseDto.setCollectionId(testRunEntity.getCollectionId());
					testRunDetailsResponseDto.setRunId(testRunEntity.getId());
					testRunDetailsResponseDto.setRunDtimes(testRunEntity.getRunDtimes());
					testRunDetailsResponseDto.setExecutionDtimes(testRunEntity.getExecutionDtimes());
					testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_NOT_AVAILABLE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.TESTRUN_NOT_AVAILABLE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getTestRunDetails method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestRunDetailsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunDetailsResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<PageDto<TestRunHistoryDto>> getTestRunHistory(String collectionId, int pageNo,
			int pageSize) {
		ResponseWrapper<PageDto<TestRunHistoryDto>> responseWrapper = new ResponseWrapper<>();
		PageDto<TestRunHistoryDto> pageData = null;
		try {
			if (Objects.nonNull(collectionId)) {
				Pageable pageable = PageRequest.of(pageNo, pageSize);
				Page<TestRunHistoryEntity> page = testRunRepository.getTestRunHistoryByCollectionId(pageable,
						collectionId, getPartnerId());
				if (Objects.nonNull(page) && page.getTotalPages() > 0) {
					List<TestRunHistoryDto> testRunHistoryList = new ArrayList<>();
					if (page.hasContent()) {
						ObjectMapper mapper = objectMapperConfig.objectMapper();
						for (TestRunHistoryEntity entity : page.getContent()) {
							TestRunHistoryDto testRunHistory = mapper.convertValue(entity, TestRunHistoryDto.class);
							testRunHistoryList.add(testRunHistory);
						}
					} else {
						List<ServiceError> serviceErrorsList = new ArrayList<>();
						ServiceError serviceError = new ServiceError();
						serviceError.setErrorCode(ToolkitErrorCodes.PAGE_NOT_FOUND.getErrorCode());
						serviceError.setMessage(ToolkitErrorCodes.PAGE_NOT_FOUND.getErrorMessage());
						serviceErrorsList.add(serviceError);
						responseWrapper.setErrors(serviceErrorsList);
					}
					pageData = new PageDto<>();
					pageData.setPageSize(page.getSize());
					pageData.setPageNo(page.getNumber());
					pageData.setCurrentPageElements(page.getNumberOfElements());
					pageData.setTotalElements(page.getTotalElements());
					pageData.setTotalPages(page.getTotalPages());
					pageData.setHasPrev(page.hasPrevious());
					pageData.setHasNext(page.hasNext());
					pageData.setSort(page.getSort().toString());
					pageData.setContent(testRunHistoryList);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getTestRunHistory method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestRunHistoryId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(pageData);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunStatusDto> getTestRunStatus(String runId) {
		ResponseWrapper<TestRunStatusDto> responseWrapper = new ResponseWrapper<>();
		TestRunStatusDto testRunStatus = null;
		try {
			if (Objects.nonNull(runId) && !runId.isEmpty()) {
				ToolkitErrorCodes toolkitError = validateRunId(runId, getPartnerId());
				if (ToolkitErrorCodes.SUCCESS.equals(toolkitError)) {
					boolean resultStatus = false;
					int successCount = testRunDetailsRepository.getTestRunSuccessCount(runId, getPartnerId());
					if (successCount > 0) {
						int testcaseCount = testRunRepository.getTestCaseCount(runId);
						if (testcaseCount == successCount) {
							resultStatus = true;
						}
					}
					testRunStatus = new TestRunStatusDto();
					testRunStatus.setResultStatus(resultStatus);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(toolkitError.getErrorCode());
					serviceError.setMessage(toolkitError.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getTestRunStatus method of TestRunService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTRUN_STATUS_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.TESTRUN_STATUS_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestRunStatusId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunStatus);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private ToolkitErrorCodes validateRunId(String runId, String partnerId) {
		ToolkitErrorCodes errorCode = ToolkitErrorCodes.PARTNERID_VALIDATION_ERR;
		try {
			if (Objects.nonNull(runId)) {
				String referencePartnerid = testRunRepository.getPartnerIdByRunId(runId, partnerId);
				if (Objects.nonNull(referencePartnerid)) {
					errorCode = ToolkitErrorCodes.SUCCESS;
				}
			}
		} catch (Exception ex) {
			errorCode = ToolkitErrorCodes.PARTNERID_VALIDATION_ERR;
		}
		return errorCode;
	}

	private ToolkitErrorCodes validatePartnerId(String collectionId, String partnerId) {
		ToolkitErrorCodes errorCode = ToolkitErrorCodes.PARTNERID_VALIDATION_ERR;
		try {
			if (Objects.nonNull(collectionId)) {
				String referencePartnerid = collectionsRepository.getPartnerById(collectionId);
				if (Objects.nonNull(referencePartnerid)) {
					if (partnerId.equals(referencePartnerid)) {
						errorCode = ToolkitErrorCodes.SUCCESS;
					}
				}
			}
		} catch (Exception ex) {
			errorCode = ToolkitErrorCodes.PARTNERID_VALIDATION_ERR;
		}
		return errorCode;
	}

}
