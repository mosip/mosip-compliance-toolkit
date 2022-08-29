package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValidationResultDto  implements Serializable{

	private static final long serialVersionUID = -2691996851671032032L;

	public String validatorName;

	public String validatorDescription;

	public String status;
	
	public String description;
	
	String resultsMap;
}
