package io.mosip.compliance.toolkit.dto.report;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ReportRequestDto {

	private String projectType;
	private String projectId;
	private String collectionId;
	private String testRunId;
	private String adminComments;
	private String partnerComments;
}