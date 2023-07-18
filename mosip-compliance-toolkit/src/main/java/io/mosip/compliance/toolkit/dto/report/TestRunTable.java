package io.mosip.compliance.toolkit.dto.report;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunTable {

	private String testCaseId;
	private String testCaseName;
	private String resultStatus;

}