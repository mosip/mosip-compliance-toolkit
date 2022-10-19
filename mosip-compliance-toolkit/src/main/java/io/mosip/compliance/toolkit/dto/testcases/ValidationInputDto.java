package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValidationInputDto  implements Serializable {

	private static final long serialVersionUID = 4182125236181639716L;

	public String testCaseType;
	
	public String testName;

	public String specVersion;

	public String testDescription;
	
	@JsonProperty("isNegativeTestcase")
	public boolean isNegativeTestCase;
	
	public String responseSchema;

	public String methodResponse;

	public String methodRequest;

	public String methodName;

	public String extraInfoJson;
	
	public List<ValidatorDefDto> validatorDefs;
}
