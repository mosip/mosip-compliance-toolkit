package io.mosip.compliance.toolkit.dto.testrun;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class TestCaseSummaryDto {

	private String testId;

	private String testCaseType;

	private String specVersion;

	private String methodRequest;

	private String methodResponse;

	private String resultStatus;

	private String resultDescription;

}
