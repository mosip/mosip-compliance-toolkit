package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SdkRequestDto implements Serializable {

	private static final long serialVersionUID = 4260065231982826451L;
	
	public String methodName;
	
	public String testcaseId;
	
	public List<String> modalities;
	
	public String bioTestDataName;
	
	public String birsForProbe;
	
}
