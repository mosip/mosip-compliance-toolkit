package io.mosip.compliance.toolkit.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Component
@Entity
@Table(name = "compliance_testrun_summary", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
@IdClass(ComplianceTestRunSummaryPK.class)
public class ComplianceTestRunSummaryEntity {

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
	
	@Column(name = "report_data_json")
	private String reportDataJson;

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

	@Column(name = "is_deleted")
	private boolean isDeleted;

	@Column(name = "del_dtimes")
	private LocalDateTime delTime;
}
