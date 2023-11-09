package io.mosip.compliance.toolkit.dto.report;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PartnerTable {

	private String orgName;
	private String address;
	private String phoneNumber;
	private String email;
}
