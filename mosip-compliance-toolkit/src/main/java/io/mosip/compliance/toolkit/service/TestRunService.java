package io.mosip.compliance.toolkit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.dto.testrun.*;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.*;
import io.mosip.kernel.core.http.RequestWrapper;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
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
import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.compliance.toolkit.entity.TestRunHistoryEntity;
import io.mosip.compliance.toolkit.entity.TestRunPartialDetailsEntity;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.TestRunDetailsRepository;
import io.mosip.compliance.toolkit.repository.TestRunRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import static io.mosip.compliance.toolkit.constants.AppConstants.*;
import static io.mosip.compliance.toolkit.validators.SBIValidator.*;

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

	@Value("${mosip.toolkit.api.id.testrun.delete}")
	private String deleteTestRunId;

	@Value("${mosip.toolkit.testrun.archive.offset}")
	private int archiveOffset;

	@Value("${mosip.toolkit.testrun.sbi.rcapture.response.encrypt}")
	private boolean encryptData;

	@Autowired
	TestRunArchiveService testRunArchiveService;

	@Autowired
	TestRunRepository testRunRepository;

	@Autowired
	TestRunDetailsRepository testRunDetailsRepository;

	@Autowired
	ResourceCacheService resourceCacheService;

	@Autowired
	CollectionsRepository collectionsRepository;

	@Autowired
	KeyManagerHelper keyManagerHelper;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private static final String BLANK_STRING = "";

	private Logger log = LoggerConfiguration.logConfig(TestRunService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public String getPartnerId() {
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
				log.info("sessionId", "idType", "id", "addTestRun" + inputTestRun);
				ToolkitErrorCodes toolkitError = validatePartnerId(inputTestRun.getCollectionId(), getPartnerId());
				if (ToolkitErrorCodes.SUCCESS.equals(toolkitError)) {

					ObjectMapper mapper = objectMapperConfig.objectMapper();
					TestRunEntity entity = mapper.convertValue(inputTestRun, TestRunEntity.class);
					String collectionId = inputTestRun.getCollectionId();
					entity.setId(RandomIdGenerator.generateUUID(
							collectionId.substring(0, Math.min(5, collectionId.length())).toLowerCase(), "", 36));
					entity.setRunDtimes(inputTestRun.getRunDtimes());
					entity.setPartnerId(getPartnerId());
					entity.setOrgName(resourceCacheService.getOrgName(getPartnerId()));
					entity.setExecutionStatus(AppConstants.INCOMPLETE);
					entity.setRunStatus(AppConstants.FAILURE);
					entity.setCrBy(getUserBy());
					entity.setCrDtimes(LocalDateTime.now());
					entity.setUpdBy(null);
					entity.setUpdDtimes(null);
					entity.setDeleted(false);
					entity.setDelTime(null);
					TestRunEntity outputEntity = testRunRepository.save(entity);

					testRun = mapper.convertValue(outputEntity, TestRunDto.class);
					log.info("sessionId", "idType", "id", "outputEntity" + outputEntity);
				} else {
					handleToolkitError(toolkitError, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_BODY, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_UNABLE_TO_ADD,
					"In addTestRun method of TestRunService Service - ");
		}
		responseWrapper.setId(postTestRunId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRun);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private void handleToolkitError(ToolkitErrorCodes toolkitError, ResponseWrapper<?> responseWrapper) {
		String errorCode = toolkitError.getErrorCode();
		String errorMessage = toolkitError.getErrorMessage();
		responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
	}

	private void logAndSetError(Exception ex, ResponseWrapper<?> responseWrapper, ToolkitErrorCodes toolkitErrorCodes,
			String logMessage) {
		log.debug("sessionId", "idType", "id", ex.getStackTrace());
		log.error("sessionId", "idType", "id", logMessage + ex.getMessage());
		String errorCode = toolkitErrorCodes.getErrorCode();
		String errorMessage = toolkitErrorCodes.getErrorMessage() + " " + ex.getMessage();
		responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
	}

	public ResponseWrapper<TestRunDto> updateTestRunExecutionTime(TestRunDto inputTestRun) {
		ResponseWrapper<TestRunDto> responseWrapper = new ResponseWrapper<TestRunDto>();
		TestRunDto testRun = null;
		try {
			if (Objects.nonNull(inputTestRun)) {
				int updateRowCount = testRunRepository.updateTestRunById(inputTestRun.getExecutionDtimes(),
						inputTestRun.getExecutionStatus(), inputTestRun.getRunStatus(), getUserBy(),
						LocalDateTime.now(), inputTestRun.getId(), getPartnerId());
				if (updateRowCount > 0) {
					testRun = inputTestRun;
				} else {
					handleToolkitError(ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_BODY, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_UNABLE_TO_UPDATE,
					"In updateTestRun method of TestRunService Service - ");
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
				ToolkitErrorCodes toolkitError = validateRunId(inputTestRunDetails.getRunId(), getPartnerId());
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
					entity.setOrgName(resourceCacheService.getOrgName(getPartnerId()));
					TestRunDetailsEntity outputEntity = new TestRunDetailsEntity();
					if (encryptData && entity.getMethodId() != null && entity.getMethodId().contains(RCAPTURE)) {
						TestRunDetailsEntity testRunDetailsEntity = encryptData(entity);
						outputEntity = testRunDetailsRepository.save(testRunDetailsEntity);
					} else {
						outputEntity = testRunDetailsRepository.save(entity);
					}
					testRunDetails = mapper.convertValue(outputEntity, TestRunDetailsDto.class);
				} else {
					handleToolkitError(toolkitError, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_BODY, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_DETAILS_UNABLE_TO_ADD,
					"In addTestRunDetails method of TestRunService Service - ");
		}
		responseWrapper.setId(postTestRunDetailsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunDetails);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunDetailsResponseDto> getTestRunDetails(String partnerId, String runId,
			boolean fullDetails) {
		ResponseWrapper<TestRunDetailsResponseDto> responseWrapper = new ResponseWrapper<>();
		TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
		try {
			List<TestRunDetailsDto> testRunDetailsList = new ArrayList<TestRunDetailsDto>();
			if (Objects.nonNull(runId) || Objects.nonNull(partnerId)) {
				TestRunEntity testRunEntity = testRunRepository.getTestRunById(runId, partnerId);
				if (Objects.nonNull(testRunEntity)) {
					if (!fullDetails) {
						List<TestRunPartialDetailsEntity> testRunPartialDetailsEntityList = testRunDetailsRepository
								.getTestRunPartialDetails(runId, partnerId);
						if (Objects.nonNull(testRunPartialDetailsEntityList)
								&& !testRunPartialDetailsEntityList.isEmpty()) {
							ObjectMapper mapper = objectMapperConfig.objectMapper();
							for (TestRunPartialDetailsEntity testRunPartialDetailsEntity : testRunPartialDetailsEntityList) {
								TestRunDetailsDto dto = mapper.convertValue(testRunPartialDetailsEntity,
										TestRunDetailsDto.class);
								testRunDetailsList.add(dto);
							}
						}
					} else {
						List<TestRunDetailsEntity> testRunDetailsEntityList = testRunDetailsRepository
								.getTestRunDetails(runId, partnerId);
						if (Objects.nonNull(testRunDetailsEntityList) && !testRunDetailsEntityList.isEmpty()) {
							ObjectMapper mapper = objectMapperConfig.objectMapper();
							for (TestRunDetailsEntity testRunDetailsEntity : testRunDetailsEntityList) {
								TestRunDetailsDto dto = new TestRunDetailsDto();
								if (encryptData && testRunDetailsEntity.getMethodId() != null && testRunDetailsEntity.getMethodId().contains(RCAPTURE)) {
									TestRunDetailsEntity entity = decryptData(testRunDetailsEntity);
									dto = mapper.convertValue(entity, TestRunDetailsDto.class);
								} else {
									dto = mapper.convertValue(testRunDetailsEntity, TestRunDetailsDto.class);
								}
								testRunDetailsList.add(dto);
							}
						}
					}

					testRunDetailsResponseDto.setCollectionId(testRunEntity.getCollectionId());
					testRunDetailsResponseDto.setRunId(testRunEntity.getId());
					testRunDetailsResponseDto.setRunDtimes(testRunEntity.getRunDtimes());
					testRunDetailsResponseDto.setExecutionDtimes(testRunEntity.getExecutionDtimes());
					testRunDetailsResponseDto.setRunStatus(testRunEntity.getRunStatus());
					testRunDetailsResponseDto.setExecutionStatus(testRunEntity.getExecutionStatus());
					testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
				} else {
					handleToolkitError(ToolkitErrorCodes.TESTRUN_NOT_AVAILABLE, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_PARAM, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE,
					"In getTestRunDetails method of TestRunService Service - ");
		}
		responseWrapper.setId(getTestRunDetailsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunDetailsResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<TestRunDetailsDto> getMethodDetails(String partnerId, String runId, String testcaseId,
			String methodId) {
		ResponseWrapper<TestRunDetailsDto> responseWrapper = new ResponseWrapper<>();
		TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
		try {
			if (Objects.nonNull(runId) || Objects.nonNull(partnerId) || Objects.nonNull(testcaseId)
					|| Objects.nonNull(methodId)) {
				TestRunEntity testRunEntity = testRunRepository.getTestRunById(runId, partnerId);
				if (Objects.nonNull(testRunEntity)) {
					TestRunDetailsEntity testRunDetailsEntity = testRunDetailsRepository.getMethodDetails(runId,
							partnerId, testcaseId, methodId);
					if (Objects.nonNull(testRunDetailsEntity)) {
						ObjectMapper mapper = objectMapperConfig.objectMapper();
						if (encryptData && testRunDetailsEntity.getMethodId() != null && testRunDetailsEntity.getMethodId().contains(RCAPTURE)) {
							TestRunDetailsEntity entity = decryptData(testRunDetailsEntity);
							testRunDetailsDto = mapper.convertValue(entity, TestRunDetailsDto.class);
						} else {
							testRunDetailsDto = mapper.convertValue(testRunDetailsEntity, TestRunDetailsDto.class);
						}
					}
				} else {
					handleToolkitError(ToolkitErrorCodes.TESTRUN_NOT_AVAILABLE, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_PARAM, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE,
					"In getMethodDetails method of TestRunService Service - ");
		}
		responseWrapper.setId(getTestRunDetailsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunDetailsDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private TestRunDetailsEntity encryptData(TestRunDetailsEntity testRunDetailsEntity) {
		try {
			if (testRunDetailsEntity.getMethodResponse() != null) {
				ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
						.readValue(testRunDetailsEntity.getMethodResponse(), ObjectNode.class);

				final JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
				if (arrBiometricNodes.isArray()) {
					for (final JsonNode biometricNode : arrBiometricNodes) {
						String dataInfo = biometricNode.get(DATA).asText();
						if (dataInfo != null && !dataInfo.equals(BLANK_STRING)) {
							String biometricData = getPayload(dataInfo);
							ObjectNode biometricDataNode = (ObjectNode) objectMapperConfig.objectMapper()
									.readValue(biometricData, ObjectNode.class);
							String timeStamp = biometricDataNode.get(TIME_STAMP).asText();
							String transactionId = biometricDataNode.get(TRANSACTION_ID).asText();
							String encryptedData = getEncryptedData(timeStamp, transactionId, dataInfo);
							((ObjectNode) biometricNode).put(DATA, encryptedData);
							((ObjectNode) biometricNode).put(TIME_STAMP, timeStamp);
							((ObjectNode) biometricNode).put(TRANSACTION_ID, transactionId);
							((ObjectNode) biometricNode).put(IS_ENCRYPTED, true);
						}
					}
					captureInfoResponse.set(BIOMETRICS, arrBiometricNodes);
					testRunDetailsEntity.setMethodResponse(objectMapperConfig.objectMapper().writeValueAsString(captureInfoResponse));
				}
			} else {
				log.debug("sessionId", "idType", "id", "Method response is null for testcase :", testRunDetailsEntity.getTestcaseId());
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In encryptBioValue method of TestRunService - " + e.getMessage());
		}
		return testRunDetailsEntity;
	}

	private TestRunDetailsEntity decryptData(TestRunDetailsEntity testRunDetailsEntity) {
		try {
			if (testRunDetailsEntity.getMethodResponse() != null) {
				ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
						.readValue(testRunDetailsEntity.getMethodResponse(), ObjectNode.class);

				final JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
				if (arrBiometricNodes.isArray()) {
					for (final JsonNode biometricNode : arrBiometricNodes) {
						String dataInfo = biometricNode.get(DATA).asText();
						if (dataInfo != null && !dataInfo.equals(BLANK_STRING) && isEncrypted(biometricNode)) {
							String timeStamp = biometricNode.get(TIME_STAMP).asText();
							String transactionId = biometricNode.get(TRANSACTION_ID).asText();
							String decryptedData = getDecryptedData(timeStamp, transactionId, dataInfo);
							((ObjectNode) biometricNode).put(DATA, decryptedData);
							// Remove fields TIME_STAMP,TRANSACTION_ID,IS_ENCRYPTED
							((ObjectNode) biometricNode).remove(TIME_STAMP);
							((ObjectNode) biometricNode).remove(TRANSACTION_ID);
							((ObjectNode) biometricNode).remove(IS_ENCRYPTED);
						}
					}
				}
				captureInfoResponse.set(BIOMETRICS, arrBiometricNodes);
				testRunDetailsEntity.setMethodResponse(objectMapperConfig.objectMapper().writeValueAsString(captureInfoResponse));
			} else {
				log.debug("sessionId", "idType", "id", "Method response is null for testcase :", testRunDetailsEntity.getTestcaseId());
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In decryptValue method of TestRunService - " + e.getMessage());
		}
		return testRunDetailsEntity;
	}

	public String getEncryptedData(String timestamp, String transactionId, String data) {

		byte[] xorResult = CryptoUtil.getXOR(timestamp, transactionId);
		byte[] aadBytes = CryptoUtil.getLastBytes(xorResult, 16);
		byte[] ivBytes = CryptoUtil.getLastBytes(xorResult, 12);

		LocalDateTime requestTime = LocalDateTime.now();
		RequestWrapper<EncryptDataRequestDto> encryptRequestWrapper = new RequestWrapper<>();
		encryptRequestWrapper.setRequesttime(requestTime);

		EncryptDataRequestDto encryptRequest = new EncryptDataRequestDto();
		encryptRequest.setApplicationId(keyManagerHelper.getAppId());
		encryptRequest.setReferenceId(keyManagerHelper.getRefId());
		encryptRequest.setData(StringUtil.base64UrlEncode(data));
		encryptRequest.setSalt(StringUtil.base64UrlEncode(ivBytes));
		encryptRequest.setAad(StringUtil.base64UrlEncode(aadBytes));
		encryptRequest.setTimeStamp(requestTime);
		encryptRequestWrapper.setRequest(encryptRequest);

		try {
			ResponseWrapper<EncryptDataResponseDto> encryptDataResponseDto = keyManagerHelper.dataEncryptionResponse(encryptRequestWrapper);

			if ((encryptDataResponseDto.getErrors() != null && encryptDataResponseDto.getErrors().size() > 0)
					|| (encryptDataResponseDto.getResponse().getData() == null)) {
				throw new ToolkitException(ToolkitErrorCodes.DATA_ENCRYPT_ERROR.getErrorCode(),
						ToolkitErrorCodes.DATA_ENCRYPT_ERROR.getErrorMessage());
			} else {
				return encryptDataResponseDto.getResponse().getData();
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In getEncryptedData method of TestRunService - " + e.getMessage());
			throw new ToolkitException(ToolkitErrorCodes.DATA_ENCRYPT_ERROR.getErrorCode(),
					ToolkitErrorCodes.DATA_ENCRYPT_ERROR.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	public String getDecryptedData(String timestamp, String transactionId, String encryptedData) {

		byte[] xorResult = CryptoUtil.getXOR(timestamp, transactionId);
		byte[] aadBytes = CryptoUtil.getLastBytes(xorResult, 16);
		byte[] ivBytes = CryptoUtil.getLastBytes(xorResult, 12);

		LocalDateTime requestTime = LocalDateTime.now();
		RequestWrapper<DecryptDataRequestDto> decryptRequestWrapper = new RequestWrapper<>();
		decryptRequestWrapper.setRequesttime(requestTime);

		DecryptDataRequestDto decryptRequest = new DecryptDataRequestDto();
		decryptRequest.setApplicationId(keyManagerHelper.getAppId());
		decryptRequest.setReferenceId(keyManagerHelper.getRefId());
		decryptRequest.setData(encryptedData);
		decryptRequest.setSalt(StringUtil.base64UrlEncode(ivBytes));
		decryptRequest.setAad(StringUtil.base64UrlEncode(aadBytes));
		decryptRequest.setTimeStamp(requestTime);
		decryptRequestWrapper.setRequest(decryptRequest);

		try {
			ResponseWrapper<DecryptDataResponseDto> decryptDataResponseDto = keyManagerHelper.dataDecryptionResponse(decryptRequestWrapper);

			if ((decryptDataResponseDto.getErrors() != null && decryptDataResponseDto.getErrors().size() > 0)
					|| (decryptDataResponseDto.getResponse().getData() == null)) {
				throw new ToolkitException(ToolkitErrorCodes.DATA_DECRYPT_ERROR.getErrorCode(),
						ToolkitErrorCodes.DATA_DECRYPT_ERROR.getErrorMessage());
			} else {
				return StringUtil.base64Decode(decryptDataResponseDto.getResponse().getData());
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In getDecryptedData method of TestRunService - " + e.getMessage());
			throw new ToolkitException(ToolkitErrorCodes.DATA_DECRYPT_ERROR.getErrorCode(),
					ToolkitErrorCodes.DATA_DECRYPT_ERROR.getErrorMessage() + e.getLocalizedMessage());
		}
	}

	protected boolean isEncrypted(JsonNode biometricNode) {
		if (biometricNode.has(IS_ENCRYPTED) && biometricNode.get(IS_ENCRYPTED).asBoolean()) {
			return true;
		}
		return false;
	}

	protected String getPayload(String jwtInfo) throws JoseException, IOException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(jwtInfo);
		String payload = jws.getEncodedPayload();
		return StringUtil.toUtf8String(StringUtil.base64UrlDecode(payload));
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
						handleToolkitError(ToolkitErrorCodes.PAGE_NOT_FOUND, responseWrapper);
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
					handleToolkitError(ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_PARAM, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_DETAILS_NOT_AVAILABLE,
					"In getTestRunHistory method of TestRunService Service - ");
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
					int successCount = testRunRepository.getTestRunSuccessCount(runId, getPartnerId());
					if (successCount > 0) {
						resultStatus = true;
					}
					testRunStatus = new TestRunStatusDto();
					testRunStatus.setResultStatus(resultStatus);
				} else {
					handleToolkitError(toolkitError, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_PARAM, responseWrapper);
			}

		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_STATUS_NOT_AVAILABLE,
					"In getTestRunStatus method of TestRunService Service - ");
		}
		responseWrapper.setId(getTestRunStatusId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testRunStatus);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<Boolean> deleteTestRun(String runId) {
		boolean deleteStatus = false;
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		try {
			if (Objects.nonNull(runId) && !runId.isEmpty()) {
				TestRunEntity entity = testRunRepository.getTestRunById(runId, getPartnerId());
				if (Objects.nonNull(entity)) {
					deleteStatus = false;
					testRunDetailsRepository.deleteById(runId, getPartnerId());
					testRunRepository.deleteById(runId, getPartnerId());
					deleteStatus = true;
				} else {
					handleToolkitError(ToolkitErrorCodes.TESTRUN_NOT_AVAILABLE, responseWrapper);
				}
			} else {
				handleToolkitError(ToolkitErrorCodes.INVALID_REQUEST_PARAM, responseWrapper);
			}
		} catch (Exception ex) {
			logAndSetError(ex, responseWrapper, ToolkitErrorCodes.TESTRUN_DELETE_ERROR,
					"In deleteTestRun method of TestRunService Service - ");
		}
		responseWrapper.setId(deleteTestRunId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(deleteStatus);
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
			handleValidateToolkitError(ex, "In validateRunId method of TestRunService Service - ");
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
			handleValidateToolkitError(ex, "In validatePartnerId method of TestRunService Service - ");
		}
		return errorCode;
	}

	private void handleValidateToolkitError(Exception ex, String logMessage) {
		log.debug("sessionId", "idType", "id", ex.getStackTrace());
		log.error("sessionId", "idType", "id", logMessage + ex.getMessage());
	}
}
