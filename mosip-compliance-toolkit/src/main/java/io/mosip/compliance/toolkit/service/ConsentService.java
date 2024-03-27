package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.entity.PartnerConsentEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.compliance.toolkit.repository.PartnerConsentRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

import static io.mosip.compliance.toolkit.constants.AppConstants.TERMS_AND_CONDTIONS_TEMPLATE;
import static io.mosip.compliance.toolkit.constants.AppConstants.YES;

@Service
public class ConsentService {

    @Autowired
    ResourceCacheService resourceCacheService;

    @Autowired
    PartnerConsentRepository partnerConsentRepository;

    @Autowired
    ObjectMapperConfig objectMapperConfig;

    @Autowired
    MasterTemplatesRepository masterTemplatesRepository;

    @Value("$(mosip.toolkit.api.id.biometric.consent.get)")
    private String getPartnerConsentId;

    @Value("$(mosip.toolkit.api.id.biometric.consent.post)")
    private String postPartnerConsentId;

    private Logger log = LoggerConfiguration.logConfig(ConsentService.class);

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

    public ResponseWrapper<Boolean> isConsentGiven(String version) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        boolean isConsentGiven = false;
        String templateName = TERMS_AND_CONDTIONS_TEMPLATE;
        try {
            String partnerId = getPartnerId();
            log.info("sessionId", "idType", "id", "fetching consent status from db for partner :", partnerId);
            Optional<PartnerConsentEntity> optionalEntity = partnerConsentRepository.findByPartnerId(partnerId);
            if (optionalEntity.isPresent()) {
                PartnerConsentEntity entity = optionalEntity.get();
                log.info("sessionId", "idType", "id", "fetching latest template timestamp from Db");
                LocalDateTime latestTemplateTimestamp = masterTemplatesRepository.getTimestampForTemplateVersion(version, templateName);

                if (Objects.nonNull(latestTemplateTimestamp)) {
                    // checking whether if partner ConsentGivenDtimes is after the latest template timestamp
                    if (entity.getConsentGivenDtimes().isAfter(latestTemplateTimestamp)) {
                        isConsentGiven = true;
                    }
                } else {
                    log.info("sessionId", "idType", "id", "could not fetch latest template timestamp, template not available");
                    String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            }
            responseWrapper.setResponse(isConsentGiven);
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id", "In isConsentGiven method of ConsentService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_CONSENT_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_CONSENT_ERR.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<PartnerConsentDto> setConsent() {
        ResponseWrapper<PartnerConsentDto> responseWrapper = new ResponseWrapper<>();
        try {
            String partnerId = getPartnerId();
            String orgName = resourceCacheService.getOrgName(partnerId);

            LocalDateTime nowDate = LocalDateTime.now(ZoneOffset.UTC);
            PartnerConsentEntity partnerConsentEntity = new PartnerConsentEntity();
            partnerConsentEntity.setPartnerId(partnerId);
            partnerConsentEntity.setOrgName(orgName);
            partnerConsentEntity.setConsentGiven(YES);
            partnerConsentEntity.setConsentGivenDtimes(nowDate);

            Optional<PartnerConsentEntity> optionalEntity = partnerConsentRepository.findByPartnerId(partnerId);
            if (optionalEntity.isPresent()) {
                PartnerConsentEntity entity = optionalEntity.get();
                partnerConsentEntity.setId(entity.getId());
                partnerConsentEntity.setUpdBy(this.getUserBy());
                partnerConsentEntity.setUpdDtimes(nowDate);
                partnerConsentEntity.setCrBy(entity.getCrBy());
                partnerConsentEntity.setCrDtimes(entity.getCrDtimes());
            } else {
                partnerConsentEntity.setId(RandomIdGenerator.generateUUID("id", "", 36));
                partnerConsentEntity.setCrBy(this.getUserBy());
                partnerConsentEntity.setCrDtimes(nowDate);
            }
            PartnerConsentEntity respEntity = partnerConsentRepository.save(partnerConsentEntity);
            log.info("sessionId", "idType", "id", "saving partner consent data for partner id : ", partnerId);

            PartnerConsentDto partnerConsentDto = (PartnerConsentDto) objectMapperConfig.objectMapper()
                    .convertValue(respEntity, new TypeReference<PartnerConsentDto>() {
            });
            responseWrapper.setResponse(partnerConsentDto);
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id", "In setConsent method of ConsentService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_CONSENT_UNABLE_TO_ADD.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_CONSENT_UNABLE_TO_ADD.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(postPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

}


