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
@Table(name = "test_run_details", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
@IdClass(TestRunDetailsPK.class)
public class TestRunPartialDetailsEntity {
	
	@Id
	@Column(name = "run_id")
	private String runId;
	
	@Id
	@Column(name = "testcase_id")
	private String testcaseId;

	@Id
	@Column(name = "method_id")
	private String methodId;
	
	@Column(name = "method_url")
	private String methodUrl;
	
	@Column(name = "execution_status")
	private String executionStatus;
	
	@Column(name = "result_status")
	private String resultStatus;
	
	@Column(name = "test_data_source")
	private String testDataSource;
	
	@Column(name = "partner_id")
	private String partnerId;

	@Column(name = "org_name")
	private String orgName;
	
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
