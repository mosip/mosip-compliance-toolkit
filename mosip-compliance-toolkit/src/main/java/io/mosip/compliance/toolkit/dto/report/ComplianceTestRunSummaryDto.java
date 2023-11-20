package io.mosip.compliance.toolkit.dto.report;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ComplianceTestRunSummaryDto {
	
	private String partnerId;
	private String orgName;
	private String projectType;
	private String projectId;
	private String collectionId;
	private String runId;
	private String reportStatus;
	private String partnerComments;
	private String adminComments;
	private LocalDateTime reviewDtimes;
	private LocalDateTime approveRejectDtimes;
	private LocalDateTime updDtimes;
	private String collectionName;
	private String projectName;
	private String updBy;
}
