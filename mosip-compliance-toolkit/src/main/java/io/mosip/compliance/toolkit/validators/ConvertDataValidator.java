package io.mosip.compliance.toolkit.validators;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.sdk.ConvertFormatRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.ConverterDataUtil;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.model.MatchDecision;

public class ConvertDataValidator extends SDKValidator {
	private Gson gson = new GsonBuilder().serializeNulls().create();
	
	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode methodResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodResponse(),
					ObjectNode.class);
			JsonNode mainResponse = (JsonNode) methodResponse.get("response");
			int statusCode = Integer.parseInt(mainResponse.get("statusCode").asText());
			if (statusCode >= 200 && statusCode <= 299) {
				
				JsonNode biometricRecordResp = (JsonNode) mainResponse.get("response");
				
				BiometricRecord biometricRecord = gson.fromJson(biometricRecordResp.toString(), BiometricRecord.class);
								
				List<BIR> birList = biometricRecord.getSegments();
				ObjectNode methodRequest = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getMethodRequest(),
						ObjectNode.class);
								
				ConvertFormatRequestDto convertFormatRequestDto = gson.fromJson(base64Decode(methodRequest.get("request").asText()), ConvertFormatRequestDto.class);
				
				String expectedCode = convertFormatRequestDto.getSourceFormat();
				for(BIR value:birList)
				{
					byte[] responseData = value.getBdb();
					if (expectedCode.equalsIgnoreCase ("IMAGE/JPEG") && ConverterDataUtil.isJPEG(responseData)){
						validationResultDto.setStatus(AppConstants.SUCCESS);
						validationResultDto.setDescription("Convert validation is successful");
					}
					else if (expectedCode.equalsIgnoreCase ("IMAGE/PNG") && ConverterDataUtil.isPNG(responseData)){
						validationResultDto.setStatus(AppConstants.SUCCESS);
						validationResultDto.setDescription("Convert validation is successful");
					}
					else
					{
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Convert validation failed for " + expectedCode);
					}					
				}
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Convert status code failed, received: " + statusCode);
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			return validationResultDto;
		}
		return validationResultDto;
	}
}
