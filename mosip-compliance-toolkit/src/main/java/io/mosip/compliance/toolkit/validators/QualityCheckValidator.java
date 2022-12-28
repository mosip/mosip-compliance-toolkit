package io.mosip.compliance.toolkit.validators;

import java.util.Map.Entry;
import java.util.Set;

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

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(
                    inputDto.getMethodResponse(),
                    ObjectNode.class);

            JsonNode mainResponse = (JsonNode) methodResponse.get("response");

            JsonNode qualityCheckResp = (JsonNode) mainResponse.get("response");

            QualityCheck qualityCheck = (QualityCheck) objectMapperConfig.objectMapper()
                    .convertValue(qualityCheckResp, QualityCheck.class);

            int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());

            if (statusCode >= 200 && statusCode <= 299) {
                Set<Entry<BiometricType, QualityScore>> entrySet = qualityCheck.getScores().entrySet();
                for (Entry<BiometricType, QualityScore> entry : entrySet) {
                    BiometricType biometricType = entry.getKey();
                    String biometricTypeStr = biometricType.value().toLowerCase();
                    float score = entry.getValue().getScore();
                    if (Modalities.FINGER.getCode().equals(biometricTypeStr)) {
                        checkScore(biometricTypeStr, inputDto, Float.parseFloat(fingerThresholdValue),
                                validationResultDto, score);
                    } else if (Modalities.FACE.getCode().equals(biometricTypeStr)) {
                        checkScore(biometricTypeStr, inputDto, Float.parseFloat(faceThresholdValue),
                                validationResultDto, score);
                    } else if (Modalities.IRIS.getCode().equals(biometricTypeStr)) {
                        checkScore(biometricTypeStr, inputDto, Float.parseFloat(irisThresholdValue),
                                validationResultDto, score);
                    } else {
                        validationResultDto.setStatus(AppConstants.FAILURE);
                        validationResultDto
                                .setDescription("Quality Check failed, invalid modality: " + biometricTypeStr);
                    }
                }
            } else {
                if (inputDto.isNegativeTestCase() && (statusCode == 403 || statusCode == 406)) {
                    validationResultDto.setStatus(AppConstants.SUCCESS);
                    validationResultDto.setDescription("Positive Quality Check for is successful");
                } else {
                    validationResultDto.setStatus(AppConstants.FAILURE);
                    validationResultDto.setDescription("Quality Check status code failed, received: " + statusCode);
                }
            }
        } catch (Exception e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(e.getLocalizedMessage());
            return validationResultDto;
        }
        return validationResultDto;
    }

    private void checkScore(String biometricTypeStr, ValidationInputDto inputDto, float thresholdValue,
            ValidationResultDto validationResultDto, float score) {
        if (!inputDto.isNegativeTestCase()) {
            // positive test case
            if (score >= thresholdValue) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Positive Quality Check for " + biometricTypeStr + " is successful");
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription("Positive Quality Check for " + biometricTypeStr
                        + " failed. Score is below the threshold value. Score received: " + score);
            }
        } else {
            // negative test case
            if (score < thresholdValue) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("Negative Quality Check for " + biometricTypeStr + " is successful");
            } else {
                validationResultDto.setStatus(AppConstants.FAILURE);
                validationResultDto.setDescription("Negative Quality Check for " + biometricTypeStr
                        + " failed. Score is above the threshold value. Score received: " + score);
            }
        }
    }

}
