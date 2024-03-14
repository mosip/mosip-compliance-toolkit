package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.VelocityEngineConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntityPK;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.PartnerProfileRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.mosip.compliance.toolkit.constants.AppConstants.NO;
import static io.mosip.compliance.toolkit.constants.AppConstants.YES;

@Component
public class UserProfileService {

    @Autowired
    ResourceCacheService resourceCacheService;

    @Autowired
    PartnerProfileRepository partnerProfileRepository;

    @Autowired
    ObjectMapperConfig objectMapperConfig;

    @Value("$(mosip.toolkit.api.id.biometric.consent.get)")
    private String getPartnerConsentId;

    @Value("$(mosip.toolkit.api.id.biometric.consent.post)")
    private String postPartnerConsentId;

    private Logger log = LoggerConfiguration.logConfig(UserProfileService.class);

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

    public ResponseWrapper<String> getConsentTemplate() throws Exception {
        ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
        try {
            log.info("sessionId", "idType", "id", "Fetching biometric consent template.");
            VelocityEngine engine = VelocityEngineConfig.getVelocityEngine();
            VelocityContext velocityContext = new VelocityContext();
            StringWriter stringWriter = new StringWriter();
            engine.mergeTemplate("templates/" + "biometricsConsent.vm", StandardCharsets.UTF_8.name(), velocityContext, stringWriter);
            String consentTemplate = stringWriter.toString();
            responseWrapper.setResponse(consentTemplate);
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getConsentTemplate method of UserProfileService - " + e.getMessage());
            throw new ToolkitException(ToolkitErrorCodes.PARTNER_CONSENT_TEMPLATE_ERR.getErrorCode(),
                    ToolkitErrorCodes.PARTNER_CONSENT_TEMPLATE_ERR.getErrorMessage() + e.getMessage());
        }
        responseWrapper.setId(getPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<PartnerConsentDto> savePartnerConsent(PartnerConsentDto partnerConsentDto) {
        ResponseWrapper<PartnerConsentDto> responseWrapper = new ResponseWrapper<>();
        try {
            String partnerId = getPartnerId();
            String orgName = resourceCacheService.getOrgName(partnerId);

            PartnerProfileEntityPK pk = new PartnerProfileEntityPK();
            pk.setPartnerId(partnerId);
            pk.setOrgName(orgName);

            LocalDateTime nowDate = LocalDateTime.now();
            PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
            partnerProfileEntity.setPartnerId(partnerId);
            partnerProfileEntity.setOrgName(orgName);

            Optional<PartnerProfileEntity> optionalEntity = partnerProfileRepository.findById(pk);
            if (optionalEntity.isPresent()) {
                partnerProfileEntity.setUpdBy(this.getUserBy());
                partnerProfileEntity.setUpdDtimes(nowDate);
                partnerProfileEntity.setCrBy(optionalEntity.get().getCrBy());
                partnerProfileEntity.setCrDtimes(optionalEntity.get().getCrDtimes());
            } else {
                partnerProfileEntity.setCrBy(this.getUserBy());
                partnerProfileEntity.setCrDtimes(nowDate);
            }

            PartnerProfileEntity entity = setPartnerConsent(partnerProfileEntity, optionalEntity, partnerConsentDto);

            PartnerProfileEntity respEntity = partnerProfileRepository.save(entity);
            log.info("sessionId", "idType", "id", "saving partner consent data for partner id : ", partnerId);

            PartnerConsentDto dto = (PartnerConsentDto)  objectMapperConfig.objectMapper()
                    .convertValue(respEntity, new TypeReference<PartnerConsentDto>() {
            });
            responseWrapper.setResponse(dto);
        } catch (Exception ex) {
            log.info("sessionId", "idType", "id",
                    "Exception in savePartnerConsent method " + ex.getLocalizedMessage());
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In savePartnerConsent method of UserProfileService - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.PARTNER_CONSENT_UNABLE_TO_ADD.getErrorCode();
            String errorMessage = ToolkitErrorCodes.PARTNER_CONSENT_UNABLE_TO_ADD.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(postPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    private PartnerProfileEntity setPartnerConsent(PartnerProfileEntity partnerProfileEntity,
                                                   Optional<PartnerProfileEntity> optionalEntity, PartnerConsentDto partnerConsentDto) {
        PartnerProfileEntity entity = partnerProfileEntity;
        if (partnerConsentDto != null) {
            if (partnerConsentDto.getConsentForSdkAbisBiometrics().equals(YES)) {
                entity.setConsentForSdkAbisBiometrics(YES);
                if (optionalEntity != null && optionalEntity.isPresent()) {
                    entity.setConsentForSbiBiometrics(optionalEntity.get().getConsentForSbiBiometrics());
                } else {
                    entity.setConsentForSbiBiometrics(NO);
                }
            }
            if (partnerConsentDto.getConsentForSbiBiometrics().equals(YES)) {
                entity.setConsentForSbiBiometrics(YES);
                if (optionalEntity != null && optionalEntity.isPresent()) {
                    entity.setConsentForSdkAbisBiometrics(optionalEntity.get().getConsentForSdkAbisBiometrics());
                } else {
                    entity.setConsentForSdkAbisBiometrics(NO);
                }
            }
        }
        return entity;
    }

    public ResponseWrapper<PartnerConsentDto> getPartnerConsent() {
        ResponseWrapper<PartnerConsentDto> responseWrapper = new ResponseWrapper<>();
        try {
            String partnerId = getPartnerId();
            String orgName = resourceCacheService.getOrgName(partnerId);
            LocalDateTime nowDate = LocalDateTime.now();

            PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
            partnerConsentDto.setPartnerId(partnerId);
            partnerConsentDto.setOrgName(orgName);

            PartnerProfileEntityPK pk = new PartnerProfileEntityPK();
            pk.setPartnerId(partnerId);
            pk.setOrgName(orgName);
            Optional<PartnerProfileEntity> optionalEntity = partnerProfileRepository.findById(pk);
            if (optionalEntity.isPresent()) {
                partnerConsentDto.setConsentForSdkAbisBiometrics(optionalEntity.get().getConsentForSdkAbisBiometrics());
                partnerConsentDto.setConsentForSbiBiometrics(optionalEntity.get().getConsentForSbiBiometrics());
                partnerConsentDto.setUpdBy(optionalEntity.get().getUpdBy());
                partnerConsentDto.setUpdDtimes(optionalEntity.get().getUpdDtimes());
                partnerConsentDto.setCrBy(optionalEntity.get().getCrBy());
                partnerConsentDto.setCrDtimes(optionalEntity.get().getCrDtimes());
            } else {
                partnerConsentDto.setCrBy(this.getUserBy());
                partnerConsentDto.setCrDtimes(nowDate);
            }
            responseWrapper.setResponse(partnerConsentDto);
        } catch (Exception ex) {
            log.info("sessionId", "idType", "id",
                    "Exception in isConsentGiven method " + ex.getLocalizedMessage());
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In isConsentGiven method of UserProfileService - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.PARTNER_CONSENT_STATUS_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.PARTNER_CONSENT_STATUS_ERR.getErrorMessage() + " " + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

}