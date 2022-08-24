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
	
	private String partnerId;
	
	private String crBy;

	private LocalDateTime crDate;

}
