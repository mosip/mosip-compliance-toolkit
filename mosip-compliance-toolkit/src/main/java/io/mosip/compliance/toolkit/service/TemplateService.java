package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.entity.MasterTemplatesEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class TemplateService {

    @Autowired
    ObjectMapperConfig objectMapperConfig;

    @Autowired
    MasterTemplatesRepository masterTemplatesRepository;

    @Value("$(mosip.toolkit.api.id.template.get)")
    private String getTemplateId;

    private Logger log = LoggerConfiguration.logConfig(TemplateService.class);

    public ResponseWrapper<MasterTemplatesDto> getTemplate(String langCode, String templateName, String version) {
        ResponseWrapper<MasterTemplatesDto> responseWrapper = new ResponseWrapper<>();
        try {
            if (Objects.nonNull(langCode) && Objects.nonNull(version) && Objects.nonNull(templateName)) {
                log.info("sessionId", "idType", "id", "fetching template for the language code :", langCode
                        , " and template name :", templateName, " and templateVersion :", version);
                Optional<MasterTemplatesEntity> optionalEntity = masterTemplatesRepository.getTemplate(langCode, templateName, version);
                if (optionalEntity.isPresent()) {
                    MasterTemplatesDto masterTemplatesDto = (MasterTemplatesDto) objectMapperConfig.objectMapper()
                            .convertValue(optionalEntity.get(), new TypeReference<MasterTemplatesDto>() {
                    });
                    responseWrapper.setResponse(masterTemplatesDto);
                } else {
                    log.info("sessionId", "idType", "id", "template is not available for language code :", langCode
                            , " and template name : ", templateName);
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
            log.error("sessionId", "idType", "id", "In getTemplate method of TemplateService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_ERR.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getTemplateId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    public ResponseWrapper<String> getLatestTemplateVersion(String templateName) {
        ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
        try {
            log.info("sessionId", "idType", "id", "fetching latest template version for :", templateName);
            String latestTemplateVersion = masterTemplatesRepository.getLatestTemplateVersion(templateName);
            if (Objects.nonNull(latestTemplateVersion)) {
                //check template version format
                if (latestTemplateVersion.matches("v\\d+")) {
                    responseWrapper.setResponse(latestTemplateVersion);
                } else {
                    log.info("sessionId", "idType", "id", "template version format is invalid for templateName - ", templateName);
                    String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_INVALID_VERSION_FORMAT.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_INVALID_VERSION_FORMAT.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            } else {
                log.info("sessionId", "idType", "id", "could not fetch latest template version, template not available");
                String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorCode();
                String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_NOT_AVAILABLE_ERR.getErrorMessage();
                responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
            }
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id", "In getLatestTemplateVersion method of TemplateService - " + e.getMessage());
            String errorCode = ToolkitErrorCodes.TOOLKIT_TEMPLATE_VERSION_ERR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.TOOLKIT_TEMPLATE_VERSION_ERR.getErrorMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(getTemplateId);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }
}


