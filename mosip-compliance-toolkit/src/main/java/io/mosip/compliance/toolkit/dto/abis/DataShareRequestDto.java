package io.mosip.compliance.toolkit.dto.abis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataShareRequestDto {

	private String testcaseId;
	private String bioTestDataName;
	private String purpose;
	private String methodName;
}
