package io.mosip.compliance.toolkit.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.regex.Pattern;

import io.mosip.compliance.toolkit.entity.MasterTemplatesEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class ResourceManagementService {

    private static final String BLANK_SPACE = " ";

    private static final String ZIP_EXT = ".zip";

    private static final String VM_EXT = ".vm";

    private static final String DEFAULT_TEMPLATE_VERSION = "v1";

    private static final String JSON_EXT = ".json";

    private static final String UNDERSCORE = "_";

    private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SBI;

    private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SDK;

    private static final String ABIS_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.ABIS;

    @Value("${mosip.toolkit.document.scan}")
    private Boolean scanDocument;

    @Value("${mosip.toolkit.documentupload.allowed.file.size}")
    private String allowedFileSize;

    @Value("${mosip.toolkit.documentupload.allowed.file.nameLength}")
    private String allowedFileNameLength;

    /**
     * Autowired reference for {@link #VirusScanner}
     */
    @Autowired
    VirusScanner<Boolean, InputStream> virusScan;

    @Autowired
    MasterTemplatesRepository masterTemplatesRepository;

    @Value("$(mosip.toolkit.api.id.resource.file.post)")
    private String postResourceFileId;

    @Value("${mosip.kernel.objectstore.account-name}")
    private String objectStoreAccountName;

    @Qualifier("S3Adapter")
    @Autowired
    private ObjectStoreAdapter objectStore;

    private Logger log = LoggerConfiguration.logConfig(ResourceManagementService.class);

    private AuthUserDetails authUserDetails() {
        return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String getUserBy() {
        String crBy = authUserDetails().getMail();
        return crBy;
    }

    public ResponseWrapper<Boolean> uploadResourceFile(String type, String version, MultipartFile file) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        boolean status = false;
        try {
            if (validInputRequest(file) && validResourceFileInputRequest(type, version)) {
                CommonUtil.performFileValidation(file, scanDocument, false, virusScan);
                if (Objects.nonNull(type)) {
                    if ((type.equals(SBI_SCHEMA) || type.equals(SDK_SCHEMA)) && !Objects.nonNull(version)) {
                        throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode(),
                                ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
                    }
                    String fileName = file.getOriginalFilename();
                    String container = null;
                    String objectName = null;
                    switch (type) {
                        case AppConstants.MOSIP_DEFAULT:
                            if (Objects.isNull(fileName) || !fileName.endsWith(ZIP_EXT)) {
                                throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                        ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                            }
                            container = AppConstants.TESTDATA;
                            String purposeDefault = fileName.replace(AppConstants.MOSIP_DEFAULT + UNDERSCORE, "")
                                    .replace(ZIP_EXT, "");
                            if (purposeDefault.contains(AppConstants.ABIS)) {
                                objectName = AppConstants.MOSIP_DEFAULT + UNDERSCORE + purposeDefault.toUpperCase() + ZIP_EXT;
                            } else {
                                SdkPurpose sdkPurposeDefault = SdkPurpose.valueOf(purposeDefault);
                                objectName = AppConstants.MOSIP_DEFAULT + UNDERSCORE + sdkPurposeDefault.toString().toUpperCase()
                                        + ZIP_EXT;
                            }
                            break;
                        case AppConstants.SCHEMAS:
                            if (Objects.isNull(fileName) || !fileName.equals(AppConstants.TESTCASE_SCHEMA_JSON)) {
                                throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                        ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                            }
                            container = AppConstants.SCHEMAS.toLowerCase();
                            objectName = fileName;
                            break;
                        case SBI_SCHEMA:
                            if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
                                throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                        ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                            }
                            container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.SBI.toLowerCase() + "/" + version;
                            objectName = fileName;
                            break;
                        case SDK_SCHEMA:
                            if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
                                throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                        ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                            }
                            container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.SDK.toLowerCase() + "/" + version;
                            objectName = fileName;
                            break;
                        case ABIS_SCHEMA:
                            if (Objects.isNull(fileName) || !fileName.endsWith(JSON_EXT)) {
                                throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                        ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                            }
                            container = AppConstants.SCHEMAS.toLowerCase() + "/" + AppConstants.ABIS.toLowerCase() + "/" + version;
                            objectName = fileName;
                            break;
                        default:
                            throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode(),
                                    ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
                    }
                    InputStream is = file.getInputStream();
                    status = putInObjectStore(container, objectName, is);
                    is.close();
                } else {
                    String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            }
        } catch (ToolkitException ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In uploadResourceFile method of ResourceManagementService Service - " + ex.getMessage());
            String errorCode = ex.getErrorCode();
            String errorMessage = ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In uploadResourceFile method of ResourceManagementService Service - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(postResourceFileId);
        responseWrapper.setResponse(status);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    private boolean putInObjectStore(String container, String objectName, InputStream data) {
        return objectStore.putObject(objectStoreAccountName, container, null, null, objectName, data);
    }


    public ResponseWrapper<Boolean> uploadTemplate(String langCode, String templateName, String version, MultipartFile file) {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        boolean status = false;
        try {
            if (validInputRequest(file) && validTemplateFileInputRequest(templateName)) {
                CommonUtil.performFileValidation(file, scanDocument, false, virusScan);
                String fileName = file.getOriginalFilename();
                if (Objects.nonNull(langCode) && Objects.nonNull(templateName) && Objects.nonNull(version)) {
                    //check template version format
                    if (!version.matches("v\\d+")) {
                        throw new ToolkitException(ToolkitErrorCodes.TOOLKIT_TEMPLATE_INVALID_VERSION_FORMAT.getErrorCode(),
                                ToolkitErrorCodes.TOOLKIT_TEMPLATE_INVALID_VERSION_FORMAT.getErrorMessage());
                    }
                    if (Objects.isNull(fileName) || !fileName.endsWith(VM_EXT)) {
                        throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                                ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
                    }
                    MasterTemplatesEntity masterTemplatesEntity = new MasterTemplatesEntity();

                    LocalDateTime nowDate = LocalDateTime.now(ZoneOffset.UTC);
                    masterTemplatesEntity.setId(RandomIdGenerator.generateUUID("id", "", 36));
                    masterTemplatesEntity.setLangCode(langCode);
                    masterTemplatesEntity.setTemplateName(templateName);
                    masterTemplatesEntity.setCrBy(this.getUserBy());
                    masterTemplatesEntity.setCrDtimes(nowDate);
                    masterTemplatesEntity.setVersion(version);

                    InputStream inputStream = file.getInputStream();
                    byte[] bytes = inputStream.readAllBytes();
                    String template = new String(bytes, StandardCharsets.UTF_8);
                    masterTemplatesEntity.setTemplate(template);

                    masterTemplatesRepository.save(masterTemplatesEntity);
                    log.info("sessionId", "idType", "id", "saved template successfully in Db having language code :", langCode
                            , "and template name :", templateName, "and version :", version);
                    status = true;

                } else {
                    String errorCode = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode();
                    String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage();
                    responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
                }
            }
        } catch (ToolkitException ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In uploadTemplate method of ResourceManagementService Service - " + ex.getMessage());
            String errorCode = ex.getErrorCode();
            String errorMessage = ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        } catch (Exception ex) {
            log.debug("sessionId", "idType", "id", ex.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In uploadTemplate method of ResourceManagementService Service - " + ex.getMessage());
            String errorCode = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode();
            String errorMessage = ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + BLANK_SPACE + ex.getMessage();
            responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
        }
        responseWrapper.setId(postResourceFileId);
        responseWrapper.setResponse(status);
        responseWrapper.setVersion(AppConstants.VERSION);
        responseWrapper.setResponsetime(LocalDateTime.now());
        return responseWrapper;
    }

    private boolean validResourceFileInputRequest(String type, String version) {
        if (!Pattern.matches(AppConstants.REGEX_PATTERN, type)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.RESOURCE_FILE_TYPE.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in resource file type");
        }
        if (!Pattern.matches(AppConstants.VERSION_REGEX_PATTERN, version)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.VERSION.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in resource file version");
        }
        return true;
    }

    private boolean validTemplateFileInputRequest(String templateName) {
        if (!Pattern.matches(AppConstants.REGEX_PATTERN, templateName)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.TEMPLATE_NAME.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in template name");
        }
        return true;
    }

    private boolean validInputRequest(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (!Pattern.matches(AppConstants.FILE_NAME_REGEX_PATTERN, fileName)) {
            String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.FILE_NAME.getErrorCode();
            throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in file name");
        }
        Long fileSize = Long.parseLong(allowedFileSize);
        if (file.getSize() > fileSize) {
            String errorCode = ToolkitErrorCodes.INVALID_FILE_SIZE.getErrorCode()
                    + AppConstants.ARGUMENTS_DELIMITER
                    + fileSize
                    + AppConstants.ARGUMENTS_SEPARATOR
                    + "B";
            throw new ToolkitException(errorCode, "File size is not allowed more than " + fileSize + "B");
        }
        int fileNameLength = Integer.parseInt(allowedFileNameLength);
        if (fileName.length() > fileNameLength) {
            String errorCode = ToolkitErrorCodes.INVALID_FILE_NAME_LENGTH.getErrorCode()
                    + AppConstants.ARGUMENTS_DELIMITER
                    + fileNameLength
                    + AppConstants.COMMA_SEPARATOR
                    + ToolkitErrorCodes.CHARACTERS.getErrorCode();
            throw new ToolkitException(errorCode, "File name is not allowed more than " + fileNameLength + " characters");
        }
        return true;
    }
}
