package io.mosip.compliance.toolkit.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class AbisProjectDto {

	private String id;
	
	private String name;
	
	private String projectType;
	
	private String url;
	
	private String username;
	
	private String password;
	
	private String queueName;
	
	private String partnerId;
	
	private String crBy;

	private LocalDateTime crDate;

	private String upBy;

	private LocalDateTime updDate;

}
