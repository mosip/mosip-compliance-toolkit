package io.mosip.compliance.toolkit.dto.testrun;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunHistoryDto {
	
	private String runId;
	
	private LocalDateTime lastRunTime;
	
	private int testCaseCount;
	
	private int passCasesCount;
	
	private int failCasesCount;
}
