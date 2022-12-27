package io.mosip.compliance.toolkit.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.Modalities;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.sdk.CheckQualityRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.RequestDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.BIRBuilder;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class BiometricsQualityCheckValidator extends ISOStandardsValidator {

	private Logger log = LoggerConfiguration.logConfig(BiometricsQualityCheckValidator.class);

	@Value("#{'${mosip.toolkit.sbi.qualitycheck.finger.sdk.urls}'.split(',')}")
	private List<String> fingerSdkUrls;

	@Value("#{'${mosip.toolkit.sbi.qualitycheck.face.sdk.urls}'.split(',')}")
	private List<String> faceSdkUrls;

	@Value("#{'${mosip.toolkit.sbi.qualitycheck.iris.sdk.urls}'.split(',')}")
	private List<String> irisSdkUrls;

	private Gson gson = new GsonBuilder().create();

	@Autowired
	private QualityCheckValidator sdkQualityCheckValidator;

	@Autowired
	private BIRBuilder birBuilder;

	@Autowired
	ObjectMapper objectMapper;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			StringBuffer messages = new StringBuffer();
			Map<String, Boolean> testCaseSuccessfulMap = new HashMap<String, Boolean>();
			List<String> sdkUrls = getSdkUrlsList(inputDto);
			for (String sdkUrl : sdkUrls) {
				// first check if init is successful
				boolean isInitSuccessful = this.callSdkInitUrl(sdkUrl);
				// if yes, then try calling quality check
				if (isInitSuccessful) {
					validationResultDto = this.performQualityCheck(sdkUrl, inputDto);
					if (!validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
						testCaseSuccessfulMap.put(sdkUrl, Boolean.TRUE);
						messages.append("<br>" + sdkUrl + ": " + validationResultDto.getDescription());
					} else {
						testCaseSuccessfulMap.put(sdkUrl, Boolean.FALSE);
						messages.append("<br>" + sdkUrl + ": " + validationResultDto.getDescription());
					}
				} else {
					testCaseSuccessfulMap.put(sdkUrl, Boolean.FALSE);
					messages.append("<br>" + sdkUrl + ": Unable to connect");
				}
			}
			Boolean areAllQualityChecksSuccessful = Boolean.TRUE;
			for (Boolean status : testCaseSuccessfulMap.values()) {
				if (status == Boolean.FALSE) {
					areAllQualityChecksSuccessful = Boolean.FALSE;
				}
			}
			if (areAllQualityChecksSuccessful.booleanValue()) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription(
						"BiometricsQualityCheckValidator validations are successful." + messages.toString());
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(
						"BiometricsQualityCheckValidator validations are not successful." + messages.toString());
			}
		} catch (ToolkitException e) {
			log.error("sessionId", "idType", "id", "In BiometricsQualityCheckValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("sessionId", "idType", "id", "In BiometricsQualityCheckValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private List<String> getSdkUrlsList(ValidationInputDto inputDto)
			throws JsonProcessingException, JsonMappingException {
		ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
				ObjectNode.class);
		String modality = extraInfo.get("modality").asText();
		List<String> sdkUrls = new ArrayList<>();
		if (Modalities.FACE.getCode().equalsIgnoreCase(modality)) {
			sdkUrls = faceSdkUrls;
		} else if (Modalities.FINGER.getCode().equalsIgnoreCase(modality)) {
			sdkUrls = fingerSdkUrls;
		} else if (Modalities.IRIS.getCode().equalsIgnoreCase(modality)) {
			sdkUrls = irisSdkUrls;
		} else {
			throw new ToolkitException(ToolkitErrorCodes.INVALID_MODALITY.getErrorCode(),
					ToolkitErrorCodes.INVALID_MODALITY.getErrorMessage());
		}
		return sdkUrls;
	}

	private boolean callSdkInitUrl(String sdkUrl) throws Exception {
		OkHttpClient client = new OkHttpClient();
		boolean isInitSuccessful = false;
		Response restCallResponse = null;
		try {
			// create request for init
			ObjectNode rootNode = objectMapper.createObjectNode();
			ObjectNode childNode = objectMapper.createObjectNode();
			rootNode.set("initParams", childNode);
			String requestJson = gson.toJson(rootNode);

			RequestDto inputDto = new RequestDto();
			inputDto.setVersion(AppConstants.VERSION);
			inputDto.setRequest(StringUtil.base64Encode(requestJson));

			String requestBody = gson.toJson(inputDto);

			MediaType mediaType = MediaType.parse(AppConstants.APPLICATION_JSON_CHARSET_UTF_8);
			RequestBody body = RequestBody.create(mediaType, requestBody);

			if (sdkUrl.endsWith("/")) {
				sdkUrl = sdkUrl + MethodName.INIT.getCode();
			} else {
				sdkUrl = sdkUrl + "/" + MethodName.INIT.getCode();
			}
			log.info(sdkUrl);
			Request request = new Request.Builder().url(sdkUrl).post(body).build();

			restCallResponse = client.newCall(request).execute();

			if (restCallResponse.isSuccessful()) {
				JSONObject jsonObject = new JSONObject(restCallResponse.body().string());
				jsonObject = jsonObject.getJSONObject("response");
				log.info(jsonObject.toString());
				isInitSuccessful = true;
			}
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
			isInitSuccessful = false;
			return isInitSuccessful;
		}
		return isInitSuccessful;

	}

	private ValidationResultDto performQualityCheck(String sdkUrl, ValidationInputDto inputDto) throws Exception {
		ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
				.readValue(inputDto.getMethodResponse(), ObjectNode.class);

		ValidationResultDto validationResultDto = new ValidationResultDto();
		List<io.mosip.kernel.biometrics.entities.BIR> birsForProbe = new ArrayList<>();

		// STEP 1: extract "bioValue" from the "sbi" capture /racpture response
		JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
		if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {

			BiometricType biometricType = null;
			for (final JsonNode biometricNode : arrBiometricNodes) {

				JsonNode dataNode = biometricNode.get(DECODED_DATA);
				String specVersion = biometricNode.get("specVersion").asText();
				String bioValue = null;

				String purpose = dataNode.get(PURPOSE).asText();
				String bioType = dataNode.get(BIO_TYPE).asText();
				String bioSubType = "";
				if (dataNode.get(BIO_SUBTYPE) != null) {
					bioSubType = dataNode.get(BIO_SUBTYPE).asText();
				}
				String qualityScore = dataNode.get("qualityScore").asText();
				long qualityScoreLong = Long.parseLong(qualityScore);
				biometricType = BiometricType.fromValue(bioType);
				boolean isAuth = false;
				switch (Purposes.fromCode(purpose)) {
				case AUTH:
					isAuth = true;
					// for authentication, the "bioValue" is encrypted, so decrypt it first
					bioValue = getDecryptedBioValue(biometricNode.get(THUMB_PRINT).asText(),
							biometricNode.get(SESSION_KEY).asText(), KEY_SPLITTER, dataNode.get(TIME_STAMP).asText(),
							dataNode.get(TRANSACTION_ID).asText(), dataNode.get(BIO_VALUE).asText());
					break;
				case REGISTRATION:
					// for registration, the "bioValue" is encoded only
					bioValue = dataNode.get(BIO_VALUE).asText();
					break;
				default:
					throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(),
							ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
				}
				// STEP 2: create BIR from the "bioValue"
				byte[] bdb = CommonUtil.decodeURLSafeBase64(bioValue);
				BIR probeBir = birBuilder.buildBIR(bdb, bioType, bioSubType, qualityScoreLong, isAuth, specVersion);
				birsForProbe.add(probeBir);
			}
			// STEP 3: call "check-quality" to get qualityScore for the probeBir
			return this.callSdkCheckQualityUrl(sdkUrl, birsForProbe, biometricType);
		}
		validationResultDto.setStatus(AppConstants.FAILURE);
		return validationResultDto;

	}

	private ValidationResultDto callSdkCheckQualityUrl(String sdkUrl,
			List<io.mosip.kernel.biometrics.entities.BIR> birsForProbe, BiometricType biometricType) throws Exception {
		Response restCallResponse = null;
		OkHttpClient client = new OkHttpClient();
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			// convert BIRS to Biometric Record
			BiometricRecord biometricRecord = new BiometricRecord();
			biometricRecord.setSegments(birsForProbe);

			List<BiometricType> bioTypeList = new ArrayList<>();
			bioTypeList.add(biometricType);

			CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
			checkQualityRequestDto.setSample(biometricRecord);
			checkQualityRequestDto.setModalitiesToCheck(bioTypeList);
			// TODO: set flags
			checkQualityRequestDto.setFlags(null);
			String requestJson = gson.toJson(checkQualityRequestDto);

			RequestDto inputDto = new RequestDto();
			inputDto.setVersion(AppConstants.VERSION);
			inputDto.setRequest(StringUtil.base64Encode(requestJson));

			String requestBody = gson.toJson(inputDto);

			MediaType mediaType = MediaType.parse(AppConstants.APPLICATION_JSON_CHARSET_UTF_8);
			RequestBody body = RequestBody.create(mediaType, requestBody);

			if (sdkUrl.endsWith("/")) {
				sdkUrl = sdkUrl + MethodName.CHECK_QUALITY.getCode();
			} else {
				sdkUrl = sdkUrl + "/" + MethodName.CHECK_QUALITY.getCode();
			}
			log.info(sdkUrl);
			Request request = new Request.Builder().url(sdkUrl).post(body).build();

			restCallResponse = client.newCall(request).execute();
			if (restCallResponse.isSuccessful()) {
				String resp = restCallResponse.body().string();
				log.debug("Quality Check Response {} ", resp);
				// Perform quality check validation
				ValidationInputDto newInputDto = new ValidationInputDto();
				newInputDto.setMethodName(MethodName.CHECK_QUALITY.getCode());
				newInputDto.setNegativeTestCase(false);
				newInputDto.setMethodRequest(requestBody);
				newInputDto.setMethodResponse(resp);
				return sdkQualityCheckValidator.validateResponse(newInputDto);
			}
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
		} catch (Exception e) {
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
			e.printStackTrace();
			throw e;
		}
		validationResultDto.setStatus(AppConstants.FAILURE);
		return validationResultDto;
	}
}
