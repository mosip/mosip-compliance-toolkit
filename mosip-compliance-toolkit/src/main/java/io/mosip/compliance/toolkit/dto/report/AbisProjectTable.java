package io.mosip.compliance.toolkit.dto.report;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class AbisProjectTable {

	private String projectName;
	private String projectType;
	private String specVersion;
	private String abisHash;
	private String website;
}
