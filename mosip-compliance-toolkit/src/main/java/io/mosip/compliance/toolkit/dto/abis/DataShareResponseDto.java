package io.mosip.compliance.toolkit.dto.abis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataShareResponseDto {

	private String id;
	private String version;
	private String responseTime;
	private DataShare dataShare;
	
	@Getter
	@Setter
	public static class DataShare {
		private String url;
		private long validForInMinutes;
		private long transactionsAllowed;
		private String policyId;
		private String subscriberId;
	}
}
