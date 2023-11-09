package io.mosip.compliance.toolkit.dto.report;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SbiProjectTable {

	private String projectName;
	private String projectType;
	private String purpose;
	private String specVersion;
	private String deviceType;
	private String deviceSubType;
	private String sbiHash;
	private String deviceMake;
	private String deviceModel;
	private String deviceSerialNo;
	private String deviceProvider;
	private String deviceProviderId;
	private List<String> deviceImages;
	private String website;
}
