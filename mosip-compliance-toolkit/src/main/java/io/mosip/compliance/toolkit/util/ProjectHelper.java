package io.mosip.compliance.toolkit.util;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionsResponseDto;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.service.CollectionsService;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class ProjectHelper {

    @Value("${mosip.toolkit.api.id.partner.report.post}")
    private String partnerReportPostId;

    @Autowired
    private CollectionsService collectionsService;

    @Autowired
    private ReportService reportService;

    private Logger log = LoggerConfiguration.logConfig(ProjectHelper.class);

    public String getComplianceCollectionId(String projectId, String projectType) {
        String complianceCollectionId = null;
        ResponseWrapper<CollectionsResponseDto> getCollections = collectionsService.getCollections(projectType, projectId);
        if (getCollections.getResponse() != null) {
            CollectionsResponseDto collectionsResponseDto = getCollections.getResponse();
            List<CollectionDto> collections = collectionsResponseDto.getCollections();
            for (CollectionDto collection: collections) {
                if (collection.getCollectionType().equalsIgnoreCase(AppConstants.COMPLIANCE_COLLECTION)) {
                    complianceCollectionId = collection.getCollectionId();
                }
            }
        } else {
            log.error("sessionId", "idType", "id", "Unable to get collections for this project " + projectId);
        }
        return complianceCollectionId;
    }

    public boolean checkIfHashCanBeUpdated(String projectId, String projectType, String collectionId) {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectId(projectId);
        reportRequestDto.setProjectType(projectType);
        reportRequestDto.setCollectionId(collectionId);
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId(partnerReportPostId);
        requestWrapper.setVersion(AppConstants.VERSION);
        requestWrapper.setRequest(reportRequestDto);
        requestWrapper.setRequesttime(LocalDateTime.now());
        ResponseWrapper<Boolean> response = reportService.isReportAlreadySubmitted(requestWrapper);
        boolean reportSubmittedStatus = false;
        if (Objects.nonNull(response)) {
            reportSubmittedStatus = response.getResponse();
        }
        return reportSubmittedStatus;
    }
}
