package io.mosip.compliance.toolkit.validators;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;
import io.mosip.kernel.biometrics.model.Decision;
import io.mosip.kernel.biometrics.model.MatchDecision;

public class MatchValidator extends SDKValidator {

	private Gson gson = new GsonBuilder().serializeNulls().create();

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ObjectNode.class);

			JsonNode mainResponse = (JsonNode) methodResponse.get("response");

			JsonNode matchResp = (JsonNode) mainResponse.get("response");

			MatchDecision[] matchDecisionsArr = gson.fromJson(matchResp.toString(), MatchDecision[].class);

			int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());

			if (statusCode >= 200 && statusCode <= 299) {
				List<Map<BiometricType, Decision>> matchDecisions = Arrays.asList(matchDecisionsArr).stream()
						.map(matchDecision -> matchDecision.getDecisions()).collect(Collectors.toList());

				for (Map<BiometricType, Decision> matchDecision : matchDecisions) {
					for (Entry<BiometricType, Decision> entry : matchDecision.entrySet()) {
						BiometricType biometricType = entry.getKey();
						String biometricTypeStr = biometricType.value().toLowerCase();
						String matchValue = entry.getValue().getMatch().name();
						if (!inputDto.isNegativeTestCase()) {
							if (Match.MATCHED.toString().equals(matchValue)) {
								validationResultDto.setStatus(AppConstants.SUCCESS);
								validationResultDto
										.setDescription("Positive Match for " + biometricTypeStr + " is successful");
							} else {
								validationResultDto.setStatus(AppConstants.FAILURE);
								validationResultDto.setDescription("Positive Match for " + biometricTypeStr
										+ " failed. Received match decision: " + matchValue);
							}
						} else {
							if (Match.NOT_MATCHED.toString().equals(matchValue)) {
								validationResultDto.setStatus(AppConstants.SUCCESS);
								validationResultDto
										.setDescription("Negative Match for " + biometricTypeStr + " is successful");
							} else {
								validationResultDto.setStatus(AppConstants.FAILURE);
								validationResultDto.setDescription("Negative Match for " + biometricTypeStr
										+ " failed. Received match decision: " + matchValue);
							}
						}
					}
				}
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Quality Check status code failed, received: " + statusCode);
			}
		} catch (

		Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}

}
