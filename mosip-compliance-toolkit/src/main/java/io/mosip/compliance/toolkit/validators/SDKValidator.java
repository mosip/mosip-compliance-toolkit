package io.mosip.compliance.toolkit.validators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;

import java.nio.charset.Charset;
import java.util.Base64;

public abstract class SDKValidator extends ToolkitValidator {

	public String successInvalidDataDescription = "For invalid data, expected status code received:";

	public String failureInvalidDataDescription = "For invalid data, unexpected status code received:";

	public String successInvalidDataDescriptionKey = "INVALID_DATA_VALIDATOR_001" + AppConstants.ARGUMENTS_DELIMITER;

	public String failureInvalidDataDescriptionKey = "INVALID_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER;

	public String base64Encode(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public String base64Decode(String data) {
		return new String (Base64.getDecoder().decode(data), Charset.forName("UTF-8"));
	}

	public int getStatusCode(ValidationInputDto inputDto) throws Exception {
		ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodResponse(),
				ObjectNode.class);
		JsonNode mainResponse = (JsonNode) methodResponse.get("response");
		int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
		return statusCode;
	}
}
