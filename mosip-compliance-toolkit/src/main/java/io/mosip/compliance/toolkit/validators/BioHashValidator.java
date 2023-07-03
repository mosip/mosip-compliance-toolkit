package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.HashUtil;
import io.mosip.kernel.core.logger.spi.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BioHashValidator extends SBIValidator {
    private Logger log = LoggerConfiguration.logConfig(ISOStandardsValidator.class);
    @Autowired
    HashUtil hashUtil;

    @Override
    public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
        boolean validHash = false;
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setValidatorName("BioHashValidator");
        validationResultDto.setValidatorDescription("Validates the content of reponse hash");
        String methodResponse = responseDto.getMethodResponse();
        String extraInfoJson = responseDto.getExtraInfoJson();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode methodResponseJson = objectMapper.readTree(methodResponse);
            ObjectMapper extraInfo = new ObjectMapper();
            JsonNode extraInfoJsonJson = extraInfo.readTree(extraInfoJson);
            String previousHash = extraInfoJsonJson.get("previousHash").asText();
            ArrayNode biometricArray = (ArrayNode) methodResponseJson.get("biometrics");
            for (int i = 0; i < biometricArray.size(); i++) {
                String responseHash = biometricArray.get(i).get("hash").asText();
                JsonNode dataDecodedJson = biometricArray.get(i).get("dataDecoded");
                ObjectMapper objectMapper1 = new ObjectMapper();
                String biovalue = dataDecodedJson.get("bioValue").asText();
                String generatedHash = "";
                if (i == 0) {
                    generatedHash = hashUtil.generateHash(previousHash, biovalue);
                } else {
                    generatedHash = hashUtil.generateHash(biometricArray.get(i - 1).get("hash").asText(),
                            biovalue);
                }
                if (generatedHash.equals(responseHash)) {
                    validHash = true;
                } else {
                    validHash = false;
                }
            }
            if (validHash) {
                validationResultDto.setDescription("Hash validation is successful");
                validationResultDto.setDescriptionKey("HASH_VALIDATOR_001");
                validationResultDto.setStatus(AppConstants.SUCCESS);
            } else {
                validationResultDto.setValidatorDescription("Hash validation is unsuccessful");
                validationResultDto.setDescriptionKey("HASH_VALIDATOR_002");
                validationResultDto.setStatus(AppConstants.FAILURE);
            }
        } catch (Exception ex) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescriptionKey("HASH_VALIDATOR_003" + AppConstants.ARGUMENTS_DELIMITER + ex.getLocalizedMessage());
            validationResultDto.setDescription("Hash validation failure due to: " + ex.getLocalizedMessage());
        }
        return validationResultDto;
    }
}
