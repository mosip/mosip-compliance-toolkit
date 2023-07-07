package io.mosip.compliance.toolkit.service;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import io.mosip.compliance.toolkit.dto.abis.*;
import io.mosip.compliance.toolkit.entity.AbisDataShareTokenEntity;
import io.mosip.compliance.toolkit.repository.AbisDataShareTokenRepository;
import io.mosip.kernel.core.http.RequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.restassured.http.Cookie;

import javax.transaction.Transactional;

@Component
@Transactional
public class ABISDataShareService {

	private static final String PATH_SEPARATOR = "/";

	private static final String FILE = "file";

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	@Value("${mosip.toolkit.abis.datashare.token.testcaseIds:}")
	private List<String> tokenTestCases;

	private Logger log = LoggerConfiguration.logConfig(ABISDataShareService.class);

	@Autowired
	public ObjectMapperConfig objectMapperConfig;

	@Autowired
	private KeyManagerHelper keyManagerHelper;

	@Autowired
	BiometricTestDataRepository biometricTestDataRepository;

	@Autowired
	AbisDataShareTokenRepository abisDataShareTokenRepository;

	@Autowired
	TestCasesService testCasesService;

	@Value("${mosip.toolkit.api.id.abis.datashare.url.get}")
	private String getDataShareUrlId;

	@Value("${mosip.service.datashare.create.url}")
	private String createDataShareUrlString;

	@Value("${mosip.service.datashare.get.url}")
	private String getDataShareUrl;

	@Value("${mosip.service.datashare.policy.id}")
	private String dataSharePolicyId;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseWrapper<DataShareResponseWrapperDto> createDataShareUrl(DataShareRequestDto dataShareRequestDto) {
		ResponseWrapper<DataShareResponseWrapperDto> responseWrapper = new ResponseWrapper<>();
		DataShareResponseWrapperDto wrapperResponseDto = new DataShareResponseWrapperDto();
		try {
			String mainFolderName = ProjectTypes.ABIS.getCode();
			String zipFileName = mainFolderName;
			if (dataShareRequestDto.getAbisProjectModality() != null
					&& !"".equals(dataShareRequestDto.getAbisProjectModality().trim())
					&& !"All".equalsIgnoreCase(dataShareRequestDto.getAbisProjectModality())) {
				zipFileName = mainFolderName + "_"
						+ dataShareRequestDto.getAbisProjectModality().toUpperCase().replaceAll(" ", "_");
			}
			log.info("zipFileName: {}", zipFileName);
			// step 1 - for the given testcase Id, read the cbeff xml from selected testdata
			// file
			byte[] cbeffFileBytes = null;
			InputStream objectStoreStream = testCasesService
					.getPartnerTestDataStream(dataShareRequestDto.getBioTestDataName(), getPartnerId(), mainFolderName);
			cbeffFileBytes = getCbeffData(objectStoreStream, mainFolderName, dataShareRequestDto.getTestcaseId(),
					dataShareRequestDto.getCbeffFileSuffix());
			wrapperResponseDto.setTestDataSource(dataShareRequestDto.getBioTestDataName());
			//If no testdata is availble for the testcase, take from MOSIP_DEFAULT_ABIS_XXXX zip file
			if (Objects.isNull(objectStoreStream) || Objects.isNull(cbeffFileBytes)) {
				objectStoreStream = testCasesService.getDefaultTestDataStream(zipFileName);
				cbeffFileBytes = getCbeffData(objectStoreStream, mainFolderName, dataShareRequestDto.getTestcaseId(),
						dataShareRequestDto.getCbeffFileSuffix());
				wrapperResponseDto.setTestDataSource(AppConstants.MOSIP_DEFAULT);
			}
			//If no testdata is availble for the testcase, take from MOSIP_DEFAULT_ABIS zip file
			if (Objects.isNull(objectStoreStream) || Objects.isNull(cbeffFileBytes)) {
				objectStoreStream = testCasesService.getDefaultTestDataStream(mainFolderName);
				cbeffFileBytes = getCbeffData(objectStoreStream, mainFolderName, dataShareRequestDto.getTestcaseId(),
						dataShareRequestDto.getCbeffFileSuffix());
				wrapperResponseDto.setTestDataSource(AppConstants.MOSIP_DEFAULT);
			}
			log.info("cbeffFileBytes: {}", cbeffFileBytes);
			// step 2 - get the auth token
			Cookie.Builder builder = new Cookie.Builder(KeyManagerHelper.AUTHORIZATION,
					keyManagerHelper.getAuthToken());
			// step 3 - get the data share url
			// for ABIS3017, we need to send incorrect partner id to simulate decryption
			// failure
			String partnerIdForDataShare = dataShareRequestDto.getIncorrectPartnerId();
			if (partnerIdForDataShare == null || partnerIdForDataShare.isBlank()) {
				partnerIdForDataShare = getPartnerId();
			}
			String dataShareFullCreateUrl = createDataShareUrlString + PATH_SEPARATOR + dataSharePolicyId
					+ PATH_SEPARATOR + partnerIdForDataShare;
			log.info("Calling dataShareFullCreateUrl: {}", dataShareFullCreateUrl);

			io.restassured.response.Response dataShareResp = given().cookie(builder.build()).relaxedHTTPSValidation()
					.multiPart(FILE, "cbeff.xml", cbeffFileBytes, MULTIPART_FORM_DATA).contentType(MULTIPART_FORM_DATA)
					.when().post(dataShareFullCreateUrl).then().extract().response();

			DataShareResponseDto dataShareResponseDto = objectMapperConfig.objectMapper()
					.readValue(dataShareResp.getBody().asString(), DataShareResponseDto.class);
			log.info("dataShare Url: {}", dataShareResponseDto.getDataShare().getUrl());
			// step 4: update the url to shareable url
			String internalUrl = dataShareResponseDto.getDataShare().getUrl();
			String[] splits = internalUrl.split("/");
			String dataShareFullGetUrl = getDataShareUrl + PATH_SEPARATOR + dataSharePolicyId + PATH_SEPARATOR
					+ partnerIdForDataShare;
			log.info("Setting dataShareFullGetUrl: {}", dataShareFullGetUrl);
			String shareableUrl = dataShareFullGetUrl;
			if (splits.length > 0) {
				String urlKey = splits[splits.length - 1];
				shareableUrl = dataShareFullGetUrl + PATH_SEPARATOR + urlKey;
			}
			if (tokenTestCases != null && tokenTestCases.contains(dataShareRequestDto.getTestcaseId())) {
				shareableUrl += "?ctkTestCaseId=" + dataShareRequestDto.getTestcaseId() + "&ctkTestRunId="
						+ dataShareRequestDto.getTestRunId();

			}
			log.info("shareableUrl: {}", shareableUrl);
			dataShareResponseDto.getDataShare().setUrl(shareableUrl);
			wrapperResponseDto.setDataShareResponseDto(dataShareResponseDto);

		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getDataShareUrl method of ABISQueueService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.ABIS_DATA_SHARE_URL_EXCEPTION.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.ABIS_DATA_SHARE_URL_EXCEPTION.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getDataShareUrlId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(wrapperResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private byte[] getCbeffData(InputStream objectStoreStream, String mainFolderName, String testcaseId, int cbeffFileSuffix)
			throws IOException, Exception {
		byte[] probeFileBytes = null;
		String fileName = "cbeff.xml";
		if (cbeffFileSuffix > 0) {
			fileName = "cbeff" + cbeffFileSuffix + ".xml";
		}
		if (Objects.nonNull(objectStoreStream)) {
			objectStoreStream.reset();
			probeFileBytes = testCasesService.getXmlDataFromZipFile(objectStoreStream, mainFolderName, testcaseId, fileName);
		}
		return probeFileBytes;
	}

	public ResponseWrapper<Boolean> expireDataShareUrl(DataShareExpireRequest requestDto) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		try {
			// step 1 - get the auth token
			Cookie.Builder builder = new Cookie.Builder(KeyManagerHelper.AUTHORIZATION,
					keyManagerHelper.getAuthToken());
			// step 2 - invoke the data share url
			log.info("Calling dataShareUrl: {}", requestDto.getUrl());
			log.info("getTransactionsAllowed: {}", requestDto.getTransactionsAllowed());
			boolean urlExpired = false;
			for (int i = 0; i < requestDto.getTransactionsAllowed() + 1; i++) {
				if (!urlExpired) {
					io.restassured.response.Response dataShareResp = given().cookie(builder.build())
							.relaxedHTTPSValidation().when().get(requestDto.getUrl()).then().extract().response();

					String resp = dataShareResp.getBody().asString();
					log.info("resp: {}", resp);
					if (resp != null) {
						try {
							DataShareResponseDto dataShareResponseDto = objectMapperConfig.objectMapper()
									.readValue(resp, DataShareResponseDto.class);
							if (dataShareResponseDto != null && dataShareResponseDto.getDataShare() == null) {
								urlExpired = true;
							}
						} catch (Exception e) {
							// ignore, it is a valid case
						}
					}
				}
			}
			responseWrapper.setResponse(Boolean.valueOf(urlExpired));
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getDataShareUrl method of ABISQueueService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.ABIS_EXPIRE_DATA_SHARE_URL_EXCEPTION.getErrorCode());
			serviceError.setMessage(
					ToolkitErrorCodes.ABIS_EXPIRE_DATA_SHARE_URL_EXCEPTION.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getDataShareUrlId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<String> saveDataShareToken(RequestWrapper<DataShareSaveTokenRequest> requestWrapper) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		AbisDataShareTokenEntity abisDataShareTokenEntity = new AbisDataShareTokenEntity();
		abisDataShareTokenEntity.setPartnerId(requestWrapper.getRequest().getPartnerId());
		abisDataShareTokenEntity.setTestCaseId(requestWrapper.getRequest().getCtkTestCaseId());
		abisDataShareTokenEntity.setTestRunId(requestWrapper.getRequest().getCtkTestRunId());
		abisDataShareTokenEntity.setToken(requestWrapper.getRequest().getToken());
		try {
			Optional<AbisDataShareTokenEntity> savedEntity = abisDataShareTokenRepository.findByAllIds(
					requestWrapper.getRequest().getPartnerId(),
					requestWrapper.getRequest().getCtkTestCaseId(),
					requestWrapper.getRequest().getCtkTestRunId());
			if (savedEntity.isPresent()) {
				int updatedRows = 0;
				if (savedEntity.get().getToken().equals(requestWrapper.getRequest().getToken())) {
					updatedRows = abisDataShareTokenRepository.updateResultByToken(AppConstants.SUCCESS,
							savedEntity.get().getToken());
					responseWrapper.setResponse(AppConstants.SUCCESS);
				} else {
					updatedRows = abisDataShareTokenRepository.updateResultByToken(AppConstants.FAILURE,
							savedEntity.get().getToken());
					responseWrapper.setResponse(AppConstants.SUCCESS);
				}
			} else {
				abisDataShareTokenRepository.save(abisDataShareTokenEntity);
			}
		} catch (Exception ex) {
			responseWrapper.setResponse(AppConstants.FAILURE);
			ServiceError serviceError = new ServiceError();
			serviceError.setMessage(ex.getLocalizedMessage());
			responseWrapper.setErrors(Collections.singletonList(serviceError));
		}
		return responseWrapper;
	}

}
