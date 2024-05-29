package io.mosip.compliance.toolkit.util;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.ComplianceTestRunSummaryRepository;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Component
public class ProjectHelper {

    @Value("${mosip.toolkit.api.id.partner.report.post}")
    private String partnerReportPostId;

    @Autowired
    private CollectionsRepository collectionsRepository;

    @Autowired
    ComplianceTestRunSummaryRepository complianceTestRunSummaryRepository;

    private Logger log = LoggerConfiguration.logConfig(ProjectHelper.class);

    public boolean checkIfHashCanBeUpdated(String projectId, String projectType, String partnerId) {
        String complianceCollectionId = getComplianceCollectionId(projectId, projectType, partnerId);
        if (complianceCollectionId != null && !"".equals(complianceCollectionId)) {
            ComplianceTestRunSummaryPK pk = new ComplianceTestRunSummaryPK();
            pk.setPartnerId(partnerId);
            pk.setProjectId(projectId);
            pk.setCollectionId(complianceCollectionId);
            Optional<ComplianceTestRunSummaryEntity> optionalEntity = complianceTestRunSummaryRepository.findById(pk);
            if (optionalEntity.isPresent() && projectType.equals(optionalEntity.get().getProjectType())) {
                return AppConstants.REPORT_STATUS_DRAFT.equals(optionalEntity.get().getReportStatus());
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
