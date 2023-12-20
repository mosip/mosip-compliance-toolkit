package io.mosip.compliance.toolkit.dto.projects;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SbiProjectDto {

	private String id;
	
	private String name;
	
	private String projectType;
	
	private String sbiVersion;
	
	private String purpose;
	
	private String deviceType;
	
	private String deviceSubType;

	private boolean isAndroid;

	private String deviceImage1;

	private String deviceImage2;

	private String deviceImage3;

	private String deviceImage4;

	private String sbiHash;

	private String websiteUrl;
	
	private String partnerId;

	private String orgName;
	
	private String crBy;

	private LocalDateTime crDate;

}
