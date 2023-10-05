package io.mosip.compliance.toolkit.validators;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.Modalities;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.model.QualityCheck;
import io.mosip.kernel.biometrics.model.QualityScore;

@Component
public class QualityCheckValidator extends SDKValidator {

	@Value("${mosip.toolkit.sdk.finger.qualitycheck.threshold.value}")
	private String fingerThresholdValue;

	@Value("${mosip.toolkit.sdk.face.qualitycheck.threshold.value}")
	private String faceThresholdValue;

	@Value("${mosip.toolkit.sdk.iris.qualitycheck.threshold.value}")
	private String irisThresholdValue;

	private Logger log = LoggerConfiguration.logConfig(QualityCheckValidator.class);

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ObjectNode.class);

			JsonNode mainResponse = (JsonNode) methodResponse.get("response");

			JsonNode qualityCheckResp = (JsonNode) mainResponse.get("response");

			QualityCheck qualityCheck = (QualityCheck) objectMapperConfig.objectMapper().convertValue(qualityCheckResp,
					QualityCheck.class);

			int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());

			if (statusCode >= 200 && statusCode <= 299) {
				Set<Entry<BiometricType, QualityScore>> entrySet = qualityCheck.getScores().entrySet();
				for (Entry<BiometricType, QualityScore> entry : entrySet) {
					BiometricType biometricType = entry.getKey();
					String biometricTypeStr = biometricType.value().toLowerCase();
					float score = entry.getValue().getScore();
					Map<String, String> analyticsInfo = entry.getValue().getAnalyticsInfo();
					if (Modalities.FINGER.getCode().equals(biometricTypeStr)) {
						checkScore(biometricTypeStr, inputDto, Float.parseFloat(fingerThresholdValue),
								validationResultDto, score, analyticsInfo);
					} else if (Modalities.FACE.getCode().equals(biometricTypeStr)) {
						checkScore(biometricTypeStr, inputDto, Float.parseFloat(faceThresholdValue),
								validationResultDto, score, analyticsInfo);
					} else if (Modalities.IRIS.getCode().equals(biometricTypeStr)) {
						checkScore(biometricTypeStr, inputDto, Float.parseFloat(irisThresholdValue),
								validationResultDto, score, analyticsInfo);
					} else {
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto
								.setDescription("Quality Check failed, invalid modality: " + biometricTypeStr);
						validationResultDto.setDescriptionKey(
								"QUALITY_CHECK_001" + AppConstants.ARGUMENTS_DELIMITER + biometricTypeStr);
					}
				}
			} else {
				if (inputDto.isNegativeTestCase() && (statusCode == 403 || statusCode == 406)) {
					validationResultDto.setStatus(AppConstants.SUCCESS);
					validationResultDto.setDescription("Negative Quality Check is successful");
					validationResultDto.setDescriptionKey("QUALITY_CHECK_002");
				} else {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Quality Check status code failed, received: " + statusCode);
					validationResultDto
							.setDescriptionKey("QUALITY_CHECK_003" + AppConstants.ARGUMENTS_DELIMITER + statusCode);
				}
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In QualityCheckValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}

	private void checkScore(String biometricTypeStr, ValidationInputDto inputDto, float thresholdValue,
			ValidationResultDto validationResultDto, float score, Map<String, String> analyticsInfo) {
		String resourceBundleKeyName = "";
		ObjectNode sdkScoreObj = objectMapperConfig.objectMapper().createObjectNode();
		int sdkScore = (int) score;
		sdkScoreObj.put("score", String.valueOf(sdkScore));
		validationResultDto.setExtraInfoJson(sdkScoreObj.toString());
		if (!inputDto.isNegativeTestCase()) {
			// positive test case
			if (score >= thresholdValue) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription(
						"Positive Quality Check for " + biometricTypeStr + " is successful. Score received: " + score);
				resourceBundleKeyName = "QUALITY_CHECK_004";
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Positive Quality Check for " + biometricTypeStr
						+ " failed. Score is below the threshold value. Score received: " + score);
				resourceBundleKeyName = "QUALITY_CHECK_005";
			}
		} else {
			// negative test case
			if (score < thresholdValue) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription(
						"Negative Quality Check for " + biometricTypeStr + " is successful. Score received: " + score);
				resourceBundleKeyName = "QUALITY_CHECK_006";
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Negative Quality Check for " + biometricTypeStr
						+ " failed. Score is above the threshold value. Score received: " + score);
				resourceBundleKeyName = "QUALITY_CHECK_007";
			}
		}
		StringBuffer analyticsInfoBuffer = new StringBuffer();

		if (analyticsInfo != null) {
			Iterator<Map.Entry<String, String>> itr = analyticsInfo.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				analyticsInfoBuffer.append(AppConstants.BR);
				analyticsInfoBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				analyticsInfoBuffer.append(AppConstants.ITALICS_TAG_START);
				analyticsInfoBuffer.append(entry.getKey());
				analyticsInfoBuffer.append(AppConstants.COLON);
				analyticsInfoBuffer.append(entry.getValue());
				analyticsInfoBuffer.append(AppConstants.ITALICS_TAG_END);
			}
		}
		StringBuffer resultsBuffer = new StringBuffer();
		resultsBuffer.append(resourceBundleKeyName);
		resultsBuffer.append(AppConstants.ARGUMENTS_DELIMITER);
		resultsBuffer.append(biometricTypeStr);
		resultsBuffer.append(AppConstants.ARGUMENTS_SEPARATOR);
		resultsBuffer.append(score);
		resultsBuffer.append(AppConstants.COMMA_SEPARATOR);
		if(validationResultDto != null && validationResultDto.getDescriptionKey() != null){
			validationResultDto.setDescriptionKey(validationResultDto.getDescriptionKey()
					+ AppConstants.COMMA_SEPARATOR
					+ "<br>"
					+ AppConstants.COMMA_SEPARATOR
			+ resultsBuffer.toString()
					+ analyticsInfoBuffer.toString());
		} else {
			validationResultDto.setDescriptionKey(resultsBuffer.toString() +
					analyticsInfoBuffer.toString());
		}
		}

}
