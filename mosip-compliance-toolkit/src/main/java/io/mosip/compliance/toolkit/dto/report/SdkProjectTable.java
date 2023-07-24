package io.mosip.compliance.toolkit.dto.report;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SdkProjectTable {

	private String projectName;
	private String projectType;
	private String purpose;
	private String specVersion;
	private String sdkHash;
}
