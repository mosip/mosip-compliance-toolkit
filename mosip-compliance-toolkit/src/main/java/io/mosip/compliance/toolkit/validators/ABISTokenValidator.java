package io.mosip.compliance.toolkit.validators;

import java.util.Optional;

import io.mosip.compliance.toolkit.util.CommonErrorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.entity.AbisDataShareTokenEntity;
import io.mosip.compliance.toolkit.repository.AbisDataShareTokenRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ABISTokenValidator extends ToolkitValidator {

	private Logger log = LoggerConfiguration.logConfig(ABISTokenValidator.class);

	@Autowired
	AbisDataShareTokenRepository abisDataShareTokenRepository;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper().readValue(inputDto.getExtraInfoJson(),
					ObjectNode.class);
			String testcaseId = extraInfo.get("testcaseId").asText();
			String testRunId = extraInfo.get("testRunId").asText();
			String partnerId = getPartnerId();
			log.info("sessionId", "idType", "id", "ABISTokenValidator validateResponse() started with testcaseId {},testRunId {}, partnerId {}", testcaseId,
					testRunId, partnerId);
			Optional<AbisDataShareTokenEntity> dbEntity = abisDataShareTokenRepository.findTokenForTestRun(partnerId,
					testcaseId, testRunId);
			if (dbEntity.isPresent()) {
				String resultInDb = dbEntity.get().getResult();
				log.info("sessionId", "idType", "id", "resultInDb {}", resultInDb);
				if (AppConstants.SUCCESS.equals(resultInDb)) {
					validationResultDto.setStatus(AppConstants.SUCCESS);
					validationResultDto.setDescription(
							"Token validation is successful. Verified that ABIS is not generating new tokens for every insert request.");
					validationResultDto.setDescriptionKey("ABIS_TOKEN_VALIDATOR_001");

				}
				if (AppConstants.FAILURE.equals(resultInDb)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription(
							"Token validation failed, ABIS is generating NEW tokens for every insert request.");
					validationResultDto.setDescriptionKey("ABIS_TOKEN_VALIDATOR_002");
				}
				//delete the row
				//abisDataShareTokenRepository.deleteById(dbEntity.get());
			} else {

				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(
						"Token validation failed, since no matching data is available in table for testcase id: "
								+ testcaseId + ", testRunId: " + testRunId + " and partnerId: " + partnerId);
				validationResultDto.setDescriptionKey("ABIS_TOKEN_VALIDATOR_003" + AppConstants.ARGUMENTS_DELIMITER
						+ testcaseId + AppConstants.ARGUMENTS_SEPARATOR + testRunId
						+ AppConstants.ARGUMENTS_SEPARATOR + partnerId);
			}
		} catch (Exception e) {
			CommonErrorUtil.getExceptionMessageAndSetResultStatus(validationResultDto, e, log,
					"In ABISTokenValidator - ");
			return validationResultDto;
		}
		return validationResultDto;
	}

}
