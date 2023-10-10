package io.mosip.compliance.toolkit.dto.testrun;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunDetailsDto {

	private String runId;

	private String testcaseId;

	private String methodId;

	private String methodUrl;

	private String methodRequest;

	private String methodResponse;

	private String resultStatus;

	private String resultDescription;
	
	private String testDataSource;

}
