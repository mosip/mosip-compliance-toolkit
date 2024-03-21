package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.CustomTemplateDto;
import io.mosip.compliance.toolkit.dto.PartnerProfileDto;
import io.mosip.compliance.toolkit.entity.CustomTemplatesEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.CustomTemplatesRepository;
import io.mosip.compliance.toolkit.repository.PartnerProfileRepository;
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
import java.util.Objects;
import java.util.Optional;

import static io.mosip.compliance.toolkit.constants.AppConstants.YES;

@Service
public class ConsentService {

    @Autowired
    ResourceCacheService resourceCacheService;

    @Autowired
    PartnerProfileRepository partnerProfileRepository;

    @Autowired
    ObjectMapperConfig objectMapperConfig;

    @Autowired
    CustomTemplatesRepository customTemplatesRepository;

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

    public ResponseWrapper<CustomTemplateDto> getConsentTemplate(String langCode, String templateName) {
        ResponseWrapper<CustomTemplateDto> responseWrapper = new ResponseWrapper<>();
        try {
            if (Objects.nonNull(langCode) && Objects.nonNull(templateName)) {
                Optional<CustomTemplatesEntity> optionalEntity = customTemplatesRepository.getTemplate(langCode, templateName);
                if (optionalEntity.isPresent()) {
                    CustomTemplateDto customTemplateDto = (CustomTemplateDto) objectMapperConfig.objectMapper()
                            .convertValue(optionalEntity.get(), new TypeReference<CustomTemplateDto>() {
                            });
                    responseWrapper.setResponse(customTemplateDto);
                } else {
                    String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            } else {
                String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
                String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
            }
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In getConsentTemplate method of ConsentService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_ERR.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<Boolean> isConsentGiven(String templateName) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        boolean isConsentGiven = false;
        try {
            if (Objects.nonNull(templateName)) {
                String partnerId = getPartnerId();

                Optional<PartnerProfileEntity> optionalEntity = partnerProfileRepository.findByPartnerId(partnerId);
                if (optionalEntity.isPresent() && optionalEntity.get().getConsentGiven().equals(YES)) {
                    LocalDateTime latestTemplateTimestamp = customTemplatesRepository.getLatestTemplateTimeStamp(templateName);
                    if (Objects.nonNull(latestTemplateTimestamp) && optionalEntity.get().getConsentGivenDtimes().isAfter(latestTemplateTimestamp)) {
                        isConsentGiven = true;
                    }
                }
                responseWrapper.setResponse(isConsentGiven);
            } else {
                String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
                String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
            }
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In isConsentGiven method of ConsentService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_CONSENT_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_CONSENT_ERR.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getPartnerConsentId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<PartnerProfileDto> setConsent() {
        ResponseWrapper<PartnerProfileDto> responseWrapper = new ResponseWrapper<>();
        try {
            String partnerId = getPartnerId();
            String orgName = resourceCacheService.getOrgName(partnerId);

            LocalDateTime nowDate = LocalDateTime.now();
            PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
            partnerProfileEntity.setPartnerId(partnerId);
            partnerProfileEntity.setOrgName(orgName);
            partnerProfileEntity.setConsentGiven(YES);
            partnerProfileEntity.setConsentGivenDtimes(nowDate);

            Optional<PartnerProfileEntity> optionalEntity = partnerProfileRepository.findByPartnerId(partnerId);
            if (optionalEntity.isPresent()) {
                partnerProfileEntity.setId(optionalEntity.get().getId());
                partnerProfileEntity.setUpdBy(this.getUserBy());
                partnerProfileEntity.setUpdDtimes(nowDate);
                partnerProfileEntity.setCrBy(optionalEntity.get().getCrBy());
                partnerProfileEntity.setCrDtimes(optionalEntity.get().getCrDtimes());
            } else {
                partnerProfileEntity.setId(RandomIdGenerator.generateUUID("id", "", 36));
                partnerProfileEntity.setCrBy(this.getUserBy());
                partnerProfileEntity.setCrDtimes(nowDate);
            }

            PartnerProfileEntity respEntity = partnerProfileRepository.save(partnerProfileEntity);
            log.info("sessionId", "idType", "id", "saving partner consent data for partner id : ", partnerId);

            PartnerProfileDto partnerProfileDto = (PartnerProfileDto) objectMapperConfig.objectMapper()
                    .convertValue(respEntity, new TypeReference<PartnerProfileDto>() {
                    });
            responseWrapper.setResponse(partnerProfileDto);
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In setConsent method of ConsentService - " + e.getMessage());
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
