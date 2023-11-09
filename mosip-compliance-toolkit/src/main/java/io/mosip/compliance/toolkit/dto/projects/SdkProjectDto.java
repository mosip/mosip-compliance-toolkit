package io.mosip.compliance.toolkit.dto.projects;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SdkProjectDto {

	private String id;
	
	private String name;
	
	private String projectType;

	private String sdkVersion;
	
	private String url;

	private String sdkHash;

	private String websiteUrl;

	private String bioTestDataFileName;
	
	private String purpose;
	
	private String partnerId;

	private String orgName;
	
	private String crBy;

	private LocalDateTime crDate;

	private String upBy;

	private LocalDateTime updDate;

}
