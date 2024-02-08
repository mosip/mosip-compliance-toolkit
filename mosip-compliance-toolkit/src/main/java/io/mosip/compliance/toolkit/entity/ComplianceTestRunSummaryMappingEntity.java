package io.mosip.compliance.toolkit.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ComplianceTestRunSummaryPK.class)
public class ComplianceTestRunSummaryMappingEntity {

    @Id
    @Column(name = "project_id")
    private String projectId;

    @Id
    @Column(name = "collection_id")
    private String collectionId;

    @Id
    @Column(name = "run_id")
    private String runId;

    @Column(name = "project_type")
    private String projectType;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "report_status")
    private String reportStatus;

    @Column(name = "partner_comments")
    private String partnerComments;

    @Column(name = "admin_comments")
    private String adminComments;

    @Column(name = "review_dttimes")
    private LocalDateTime reviewDtimes;

    @Column(name = "approve_reject_dttimes")
    private LocalDateTime approveRejectDtimes;

    @Column(name = "cr_by")
    private String crBy;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "upd_by")
    private String updBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updDtimes;

    @Column(name = "collection_type")
    private String collectionType;

    @Column(name = "collection_name")
    private String collectionName;
}
