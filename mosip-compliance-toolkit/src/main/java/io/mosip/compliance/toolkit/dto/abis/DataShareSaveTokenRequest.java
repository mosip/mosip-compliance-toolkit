package io.mosip.compliance.toolkit.dto.abis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataShareSaveTokenRequest {

	private String partnerId;
	private String ctkTestCaseId;
	private String ctkTestRunId;
	private String token;
}
