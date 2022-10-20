package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValidateRequestSchemaDto implements Serializable {

	private static final long serialVersionUID = 4260065231982826451L;

	public String testCaseType;
	
	public String testName;

	public String specVersion;

	public String testDescription;
	
	public String requestSchema;

	public String methodRequest;

}
