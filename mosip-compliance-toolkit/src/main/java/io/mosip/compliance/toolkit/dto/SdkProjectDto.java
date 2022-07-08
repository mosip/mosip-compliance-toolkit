package io.mosip.compliance.toolkit.dto;

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
	
	private String url;
	
	private String purpose;
	
	private String partnerId;
	
	private String crBy;

	private LocalDateTime crDate;

	private String upBy;

	private LocalDateTime updDate;

}
