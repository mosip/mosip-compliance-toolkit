package io.mosip.compliance.toolkit.util;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class ProjectHelper {

    @Value("${mosip.toolkit.api.id.partner.report.post}")
    private String partnerReportPostId;

    @Autowired
    private CollectionsRepository collectionsRepository;

    @Autowired
    private ReportService reportService;

    private Logger log = LoggerConfiguration.logConfig(ProjectHelper.class);

    public boolean checkIfHashCanBeUpdated(String projectId, String projectType, String partnerId) {
        String complianceCollectionId = getComplianceCollectionId(projectId, projectType, partnerId);
        if (complianceCollectionId != null && !"".equals(complianceCollectionId)) {
            ReportRequestDto reportRequestDto = new ReportRequestDto();
            reportRequestDto.setProjectId(projectId);
            reportRequestDto.setProjectType(projectType);
            reportRequestDto.setCollectionId(complianceCollectionId);
            RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
            requestWrapper.setId(partnerReportPostId);
            requestWrapper.setVersion(AppConstants.VERSION);
            requestWrapper.setRequest(reportRequestDto);
            requestWrapper.setRequesttime(LocalDateTime.now());
            ResponseWrapper<Boolean> response = reportService.isReportAlreadySubmitted(requestWrapper);
            if (Objects.nonNull(response) && response.getResponse()) {
                return false;
            }
        }
        return true;
    }

    private String getComplianceCollectionId(String projectId, String projectType, String partnerId) {
        String complianceCollectionId = null;
        if (AppConstants.SBI.equalsIgnoreCase(projectType)) {
            complianceCollectionId = collectionsRepository.getSbiComplianceCollectionId(projectId, AppConstants.COMPLIANCE_COLLECTION, partnerId);
        } else if (AppConstants.SDK.equalsIgnoreCase(projectType)) {
            complianceCollectionId = collectionsRepository.getSdkComplianceCollectionId(projectId, AppConstants.COMPLIANCE_COLLECTION, partnerId);
        } else if (AppConstants.ABIS.equalsIgnoreCase(projectType)) {
            complianceCollectionId = collectionsRepository.getAbisComplianceCollectionId(projectId, AppConstants.COMPLIANCE_COLLECTION, partnerId);
        }
        return complianceCollectionId;
    }
}
