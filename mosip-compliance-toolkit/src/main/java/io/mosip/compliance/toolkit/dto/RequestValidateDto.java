package io.mosip.compliance.toolkit.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RequestValidateDto implements Serializable {
	
	public String testCaseType;
	
	public String testName;

	public String testDescription;
	
	public String requestSchema;

	public String methodRequest;

}
