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
@Table(name = "test_run_details_archive", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
@IdClass(TestRunDetailsPK.class)
public class TestRunDetailsArchiveEntity {
	
	@Id
	@Column(name = "run_id")
	private String runId;

	@Id
	@Column(name = "testcase_id")
	private String testcaseId;

	@Column(name = "method_request")
	private String methodRequest;

	@Column(name = "method_response")
	private String methodResponse;

	@Column(name = "result_status")
	private String resultStatus;

	@Column(name = "result_description")
	private String resultDescription;

	@Column(name = "partner_id")
	private String partnerId;

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
