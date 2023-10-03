package io.mosip.compliance.toolkit.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

	@Value("${mosip.toolkit.sbi.qualitycheck.finger.sdk.urls}")
	private String fingerSdkUrlsJsonStr;

	@Value("${mosip.toolkit.sbi.qualitycheck.face.sdk.urls}")
	private String faceSdkUrlsJsonStr;

	@Value("${mosip.toolkit.sbi.qualitycheck.iris.sdk.urls}")
	private String irisSdkUrlsJsonStr;

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
			StringBuffer codes = new StringBuffer();
			Map<String, Boolean> testCaseSuccessfulMap = new HashMap<String, Boolean>();
			String sdkUrls = getSdkUrlsJsonString(inputDto);
			ArrayNode sdkUrlsArr = (ArrayNode) objectMapperConfig.objectMapper().readValue(sdkUrls, ArrayNode.class);
			for (JsonNode item : sdkUrlsArr) {
				// first check if init is successful
				String sdkUrl = item.get("url").asText();
				String healthUrl = item.get("healthUrl").asText();
				String sdkName = item.get("name").asText();
				boolean includeInResults = item.get("includeInResults").asBoolean();
				boolean isSdkServiceAccessible = this.callSdkHealthUrl(healthUrl);
				// if yes, then try calling quality check
				String validatorMsg = "";
				if (isSdkServiceAccessible) {
					validationResultDto = this.performQualityCheck(sdkUrl, inputDto);
					if (!validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
						if (includeInResults) {
							testCaseSuccessfulMap.put(sdkUrl, Boolean.TRUE);
						}
					} else {
						if (includeInResults) {
							testCaseSuccessfulMap.put(sdkUrl, Boolean.FALSE);
						}
					}
					messages.append(
							AppConstants.BR + sdkName + AppConstants.COLON + validationResultDto.getDescription());
					validatorMsg = validationResultDto.getDescriptionKey();
				} else {
					if (includeInResults) {
						testCaseSuccessfulMap.put(sdkUrl, Boolean.FALSE);
					}
					messages.append(AppConstants.BR + sdkName + AppConstants.COLON + "Unable to connect");
					validatorMsg = "<br>,BIOMETRIC_QUALITY_CHECK_003";
				}
				codes.append(AppConstants.BR);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(AppConstants.BR);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(AppConstants.BOLD_TAG_START);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(sdkName);
				if (!includeInResults) {
					codes.append(AppConstants.COMMA_SEPARATOR);
					codes.append("&nbsp;( ");
					codes.append(AppConstants.COMMA_SEPARATOR);
					codes.append("BIOMETRIC_QUALITY_CHECK_004");
					codes.append(AppConstants.COMMA_SEPARATOR);
					codes.append(" ) ");
				}
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(AppConstants.COLON);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(AppConstants.BOLD_TAG_END);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(AppConstants.BR);
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append("____________________________________________________________");
				codes.append(AppConstants.COMMA_SEPARATOR);
				codes.append(validatorMsg);
				codes.append(AppConstants.COMMA_SEPARATOR);
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
				validationResultDto.setDescriptionKey("BIOMETRIC_QUALITY_CHECK_002," + codes.toString());
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(
						"BiometricsQualityCheckValidator validations are not successful." + messages.toString());
				validationResultDto.setDescriptionKey("BIOMETRIC_QUALITY_CHECK_001," + codes.toString());
			}
		} catch (ToolkitException e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In BiometricsQualityCheckValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In BiometricsQualityCheckValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private String getSdkUrlsJsonString(ValidationInputDto inputDto)
			throws JsonProcessingException, JsonMappingException {
		ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
				ObjectNode.class);
		String modality = extraInfo.get("modality").asText();
		String sdkUrlsJsonStr = "";
		if (Modalities.FACE.getCode().equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = faceSdkUrlsJsonStr;
		} else if (Modalities.FINGER.getCode().equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = fingerSdkUrlsJsonStr;
		} else if (Modalities.IRIS.getCode().equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = irisSdkUrlsJsonStr;
		} else {
			throw new ToolkitException(ToolkitErrorCodes.INVALID_MODALITY.getErrorCode(),
					ToolkitErrorCodes.INVALID_MODALITY.getErrorMessage());
		}
		return sdkUrlsJsonStr;
	}

	private boolean callSdkHealthUrl(String sdkUrl) throws Exception {
		OkHttpClient client = new OkHttpClient();
		boolean isHealthCheckSuccessful = false;
		Response restCallResponse = null;
		try {

			Request request = new Request.Builder().url(sdkUrl).get().build();
			restCallResponse = client.newCall(request).execute();
			if (restCallResponse.isSuccessful()) {
				isHealthCheckSuccessful = true;
			}
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
		} catch (Exception e) {
			// e.printStackTrace();
			if (restCallResponse != null && restCallResponse.body() != null) {
				restCallResponse.body().close();
			}
			isHealthCheckSuccessful = false;
			return isHealthCheckSuccessful;
		}
		return isHealthCheckSuccessful;

	}

	private ValidationResultDto performQualityCheck(String sdkUrl, ValidationInputDto inputDto) throws Exception {
		ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
				.readValue(inputDto.getMethodResponse(), ObjectNode.class);

		List<ValidationResultDto> validationResultDtoList = new ArrayList<>();

		// STEP 1: extract "bioValue" from the "sbi" capture /racpture response
		JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
		if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {

			BiometricType biometricType = null;
			for (final JsonNode biometricNode : arrBiometricNodes) {
				//STEP 1: get bioValue and other attributes
				JsonNode dataNode = biometricNode.get(DECODED_DATA);
				String specVersion = biometricNode.get("specVersion").asText();
				String purpose = dataNode.get(PURPOSE).asText();
				boolean isAuth = false;
				if ("Auth".equalsIgnoreCase(purpose)) {
					isAuth = true;
				}
				String bioType = dataNode.get(BIO_TYPE).asText();
				String bioSubType = "";
				if (dataNode.get(BIO_SUBTYPE) != null) {
					bioSubType = dataNode.get(BIO_SUBTYPE).asText();
				}
				String qualityScore = dataNode.get("qualityScore").asText();
				long qualityScoreLong = Long.parseLong(qualityScore);
				biometricType = BiometricType.fromValue(bioType);
				String bioValue = extractBioValue(biometricNode);
				// STEP 2: create BIR from the "bioValue"
				byte[] bdb = CommonUtil.decodeURLSafeBase64(bioValue);
				BIR probeBir = birBuilder.buildBIR(bdb, bioType, bioSubType, qualityScoreLong, isAuth, specVersion);
				List<io.mosip.kernel.biometrics.entities.BIR> birsForProbe = new ArrayList<>();
				birsForProbe.add(probeBir);
				// STEP 3: call "check-quality" to get qualityScore for the probeBir
				ValidationResultDto validationResult = this.callSdkCheckQualityUrl(sdkUrl, birsForProbe, biometricType);
				validationResultDtoList.add(validationResult);
			}
			// STEP 4: calculate aggregate score and messages;
			ValidationResultDto finalResult = new ValidationResultDto();
			finalResult.setStatus(AppConstants.SUCCESS);
			StringBuffer keyBuffer = new StringBuffer();
			List<String> descriptionList = new ArrayList<>();
			for (ValidationResultDto item : validationResultDtoList) {
				if (item.getStatus().equals(AppConstants.FAILURE)) {
					finalResult.setStatus(AppConstants.FAILURE);
				}
				descriptionList.add(item.getDescription());
				keyBuffer.append(AppConstants.BR);
				keyBuffer.append(AppConstants.COMMA_SEPARATOR);
				keyBuffer.append(item.getDescriptionKey());
			}
			finalResult.setDescription(descriptionList.toString());
			finalResult.setDescriptionKey(keyBuffer.toString());

			return finalResult;
		}
		ValidationResultDto failureResult = new ValidationResultDto();
		failureResult.setStatus(AppConstants.FAILURE);
		return failureResult;

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
			// System.out.println(requestJson);
			RequestDto inputDto = new RequestDto();
			inputDto.setVersion(AppConstants.VERSION);
			inputDto.setRequest(StringUtil.base64Encode(requestJson));

			String requestBody = gson.toJson(inputDto);
			// System.out.println(requestBody);
			MediaType mediaType = MediaType.parse(AppConstants.APPLICATION_JSON_CHARSET_UTF_8);
			RequestBody body = RequestBody.create(mediaType, requestBody);

			if (sdkUrl.endsWith("/")) {
				sdkUrl = sdkUrl + MethodName.CHECK_QUALITY.getCode();
			} else {
				sdkUrl = sdkUrl + "/" + MethodName.CHECK_QUALITY.getCode();
			}
			log.info("sessionId", "idType", "id", "SdkCheckQualityUrl " + sdkUrl);
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
			// e.printStackTrace();
			throw e;
		}
		validationResultDto.setStatus(AppConstants.FAILURE);
		return validationResultDto;
	}
}