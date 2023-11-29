package io.mosip.compliance.toolkit.dto.report;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ReportDataDto {

	private String projectType;
	private String statusText;
	private String origin;
	private PartnerTable partnerDetails;
	private SbiProjectTable sbiProjectDetailsTable;
	private SdkProjectTable sdkProjectDetailsTable;
	private AbisProjectTable abisProjectDetailsTable;
	private String testRunStartTime;
	private int reportExpiryPeriod;
	private String reportValidityDate;
	private List<TestRunTable> testRunDetailsList;
	private String collectionName;
	private String timeTakenByTestRun;
	private int totalTestCasesCount;
	private int countOfPassedTestCases;
	private int countOfFailedTestCases;
}
