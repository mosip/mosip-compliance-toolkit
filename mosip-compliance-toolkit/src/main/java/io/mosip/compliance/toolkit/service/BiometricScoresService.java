package io.mosip.compliance.toolkit.service;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.entity.BiometricScoresEntity;
import io.mosip.compliance.toolkit.repository.BiometricScoresRepository;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BiometricScoresService {

    @Autowired
    private BiometricScoresRepository biometricScoresRepository;

    private Logger log = LoggerConfiguration.logConfig(SbiProjectService.class);

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getPartnerId() {
        String partnerId = authUserDetails().getUsername();
        return partnerId;
    }

    private String getUserBy() {
        String crBy = authUserDetails().getMail();
        return crBy;
    }

    public void addBiometricScores(String projectId, String testRunId, String testId, String scoresJson) {
        try {
            if (projectId != null && testRunId != null && testId != null && scoresJson != null) {
                LocalDateTime crDate = LocalDateTime.now();
                BiometricScoresEntity entity = new BiometricScoresEntity();
                entity.setId(RandomIdGenerator.generateUUID(AppConstants.SBI.toLowerCase(), "", 36));
                entity.setProjectId(projectId);
                entity.setPartnerId(getPartnerId());
                entity.setScoresJson(scoresJson);
                entity.setCrDate(crDate);
                entity.setCrBy(getUserBy());
                entity.setTestRunId(testRunId);
                entity.setTestCaseId(testId);
                biometricScoresRepository.save(entity);
            } else {
                log.error("sessionId", "idType", "id",
                        "Biometric scores could not be added for this quality assessment testcase: {}", testId);
            }
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In addBiometricScores method of BiometricScoresService Service - " + ex.getMessage());
        }
    }
}
