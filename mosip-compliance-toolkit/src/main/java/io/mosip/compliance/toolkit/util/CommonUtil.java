package io.mosip.compliance.toolkit.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import org.springframework.web.multipart.MultipartFile;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;

public final class CommonUtil {
    private static final String ZIP_EXT = ".zip";

    private static Logger log = LoggerConfiguration.logConfig(CommonUtil.class);

    public static List<ServiceError> getServiceErr(String errorCode, String errorMessage) {
        List<ServiceError> serviceErrorsList = new ArrayList<>();
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode(errorCode);
        serviceError.setMessage(errorMessage);
        serviceErrorsList.add(serviceError);
        return serviceErrorsList;
    }

    public static void performFileValidation(MultipartFile file, Boolean scanDocument, Boolean isBiometricTestDataFile, VirusScanner<Boolean, InputStream> virusScan) {
        String filename = file.getOriginalFilename();

        // check if the file is null or empty
        if (Objects.isNull(file) || Objects.isNull(filename) || file.isEmpty() || file.getSize() <= 0) {
            throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorCode(),
                    ToolkitErrorCodes.INVALID_REQUEST_PARAM.getErrorMessage());
        }

        // Validate if the file has extensions
        if (!filename.contains(".")) {
            throw new ToolkitException(ToolkitErrorCodes.FILE_WITHOUT_EXTENSIONS.getErrorCode(),
                    ToolkitErrorCodes.FILE_WITHOUT_EXTENSIONS.getErrorMessage());
        }

        // Validate if there are multiple extensions
        if (filename.split("\\.").length > 2) {
            throw new ToolkitException(ToolkitErrorCodes.FILE_WITH_MULTIPLE_EXTENSIONS.getErrorCode(),
                    ToolkitErrorCodes.FILE_WITH_MULTIPLE_EXTENSIONS.getErrorMessage());
        }

        if (isBiometricTestDataFile && !filename.endsWith(ZIP_EXT)) {
            throw new ToolkitException(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode(),
                    ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
        }

        // Perform virus scanning if enabled
        if (scanDocument && !isVirusScanSuccess(file, virusScan)) {
            throw new ToolkitException(ToolkitErrorCodes.VIRUS_FOUND.getErrorCode(),
                    ToolkitErrorCodes.VIRUS_FOUND.getErrorMessage());
        }
    }

    private static boolean isVirusScanSuccess(MultipartFile file, VirusScanner<Boolean, InputStream> virusScan) {
        try {
            log.info("sessionId", "idType", "id", "In isVirusScanSuccess method of CommonUtil");
            return virusScan.scanDocument(file.getBytes());
        } catch (Exception e) {
            log.debug("sessionId", "idType", "id", e.getStackTrace());
            log.error("sessionId", "idType", "id",
                    "In isVirusScanSuccess method of CommonUtil - " + e.getMessage());
            throw new VirusScannerException(ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorCode(),
                    ToolkitErrorCodes.RESOURCE_UPLOAD_ERROR.getErrorMessage() + e.getMessage());
        }
    }
}