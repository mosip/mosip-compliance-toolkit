package io.mosip.compliance.toolkit.repository;

import java.util.List;

import io.mosip.compliance.toolkit.entity.ComplianceReportSummaryEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("ComplianceTestRunSummaryRepository")
public interface ComplianceTestRunSummaryRepository
        extends BaseRepository<ComplianceTestRunSummaryEntity, ComplianceTestRunSummaryPK> {

    @Query("SELECT new io.mosip.compliance.toolkit.entity.ComplianceReportSummaryEntity(" +
            "cts.projectId AS project_id, " +
            "cts.collectionId AS collection_id, " +
            "cts.runId AS run_id, " +
            "cts.projectType AS project_type, " +
            "cts.partnerId AS partner_id, " +
            "cts.orgName AS org_name, " +
            "cts.reportStatus AS report_status, " +
            "cts.partnerComments AS partner_comments, " +
            "cts.adminComments AS admin_comments, " +
            "cts.reviewDtimes AS review_dttimes, " +
            "cts.approveRejectDtimes AS approve_reject_dttimes, " +
            "cts.crBy AS cr_by, " +
            "cts.crDtimes AS cr_dtimes, " +
            "cts.updBy AS upd_by, " +
            "cts.updDtimes AS upd_dtimes, " +
            "c.collectionType AS collection_type, " +
            "c.name AS collection_name) " +
            "FROM ComplianceTestRunSummaryEntity cts, CollectionEntity c " +
            "WHERE cts.collectionId = c.id " +
            "AND cts.reportStatus = ?1 " +
            "AND cts.isDeleted <> 'true' " +
            "ORDER BY cts.crDtimes DESC")
    public List<ComplianceReportSummaryEntity> findAllByReportStatus(String reportStatus);

    @Query("SELECT new io.mosip.compliance.toolkit.entity.ComplianceReportSummaryEntity(" +
            "cts.projectId AS project_id, " +
            "cts.collectionId AS collection_id, " +
            "cts.runId AS run_id, " +
            "cts.projectType AS project_type, " +
            "cts.partnerId AS partner_id, " +
            "cts.orgName AS org_name, " +
            "cts.reportStatus AS report_status, " +
            "cts.partnerComments AS partner_comments, " +
            "cts.adminComments AS admin_comments, " +
            "cts.reviewDtimes AS review_dttimes, " +
            "cts.approveRejectDtimes AS approve_reject_dttimes, " +
            "cts.crBy AS cr_by, " +
            "cts.crDtimes AS cr_dtimes, " +
            "cts.updBy AS upd_by, " +
            "cts.updDtimes AS upd_dtimes, " +
            "c.collectionType AS collection_type, " +
            "c.name AS collection_name) " +
            "FROM ComplianceTestRunSummaryEntity cts, CollectionEntity c " +
            "WHERE cts.collectionId = c.id " +
            "AND cts.partnerId = ?1 " +
            "AND cts.reportStatus<>'draft' " +
            "AND cts.isDeleted <> 'true' " +
            "ORDER BY cts.crDtimes DESC")
    public List<ComplianceReportSummaryEntity> findAllBySubmittedReportsPartnerId(String partnerId);

}
