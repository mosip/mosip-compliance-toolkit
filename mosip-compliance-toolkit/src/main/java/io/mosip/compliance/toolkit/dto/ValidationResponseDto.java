package io.mosip.compliance.toolkit.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValidationResponseDto  implements Serializable{

	public String validatorName;

	public String validatorDescription;

	String status;
	
	String description;
}
