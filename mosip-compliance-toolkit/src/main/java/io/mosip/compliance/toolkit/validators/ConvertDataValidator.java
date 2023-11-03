package io.mosip.compliance.toolkit.validators;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.sdk.ConvertFormatRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.CommonUtilError;
import io.mosip.compliance.toolkit.util.ConverterDataUtil;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.logger.spi.Logger;

public class ConvertDataValidator extends SDKValidator {
	private Gson gson = new GsonBuilder().serializeNulls().create();

	private Logger log = LoggerConfiguration.logConfig(ConvertDataValidator.class);
	
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
				
				String sourceCode = convertFormatRequestDto.getSourceFormat();
				String targetCode = convertFormatRequestDto.getTargetFormat();
				for(BIR value:birList)
				{
					byte[] responseData = value.getBdb();
					if ((targetCode.equalsIgnoreCase("IMAGE/JPEG") && ConverterDataUtil.isJPEG(responseData)) ||
							(targetCode.equalsIgnoreCase("IMAGE/PNG") && ConverterDataUtil.isPNG(responseData))) {
						validationResultDto.setStatus(AppConstants.SUCCESS);
						validationResultDto.setDescription("Convert validation is successful");
						validationResultDto.setDescriptionKey("CONVERT_DATA_VALIDATOR_001");
					}
					else
					{
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Convert validation failed for SourceCode [" + sourceCode + "] to TargetCode [" + targetCode + "]");
						validationResultDto.setDescriptionKey("CONVERT_DATA_VALIDATOR_002" + AppConstants.ARGUMENTS_DELIMITER + sourceCode + AppConstants.ARGUMENTS_SEPARATOR + targetCode);
					}					
				}
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Convert status code failed, received: " + statusCode);
				validationResultDto.setDescriptionKey("CONVERT_DATA_VALIDATOR_003" + AppConstants.ARGUMENTS_DELIMITER + statusCode);
			}
		} catch (Exception e) {
			CommonUtilError.getExceptionMessageAndSetResultStatus(validationResultDto, e, log,
					"In convertDataValidator - ");
			return validationResultDto;
		}
		return validationResultDto;
	}
}
