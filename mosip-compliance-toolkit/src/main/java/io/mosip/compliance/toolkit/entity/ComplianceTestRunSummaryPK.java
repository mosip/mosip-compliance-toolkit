package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceTestRunSummaryPK implements Serializable {
	private static final long serialVersionUID = 1L;

	private String projectId;

	private String collectionId;

	private String runId;

}
