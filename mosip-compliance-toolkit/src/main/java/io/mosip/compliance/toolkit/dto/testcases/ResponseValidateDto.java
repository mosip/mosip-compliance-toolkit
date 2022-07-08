package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ResponseValidateDto  implements Serializable {

	public String testCaseType;
	
	public String testName;

	public String testDescription;
	
	public String responseSchema;

	public String methodResponse;

	public String methodRequest;

	public List<ValidatorDefDto> validatorDefs;
}
