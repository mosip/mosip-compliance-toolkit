package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BiometricTestDataDto {

	private String id;

	private String name;

	private String type;

	private String purpose;

	private String partnerId;

	private String orgName;

	private String fileId;
	
	private String crDate;

}
