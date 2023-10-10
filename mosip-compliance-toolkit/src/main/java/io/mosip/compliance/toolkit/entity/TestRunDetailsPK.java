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
public class TestRunDetailsPK implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4344124764663365738L;

	private String runId;
	
	private String testcaseId;

	private String methodId;
}
