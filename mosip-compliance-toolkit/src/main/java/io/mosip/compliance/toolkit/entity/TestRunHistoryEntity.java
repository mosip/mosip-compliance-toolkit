package io.mosip.compliance.toolkit.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class TestRunHistoryEntity {

	@Id
	@Column(name = "id")
	private String runId;
	
	@Column(name = "last_run_time")
	private LocalDateTime lastRunTime;
	
	@Column(name = "testcase_count")
	private long testCaseCount;
	
	@Column(name = "passcase_count")
	private long passCasesCount;
	
	@Column(name = "failcase_count")
	private long failCasesCount; 
}
