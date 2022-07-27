package io.mosip.compliance.toolkit.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollectionTestRunDto {

	private String collectionId;
	
	private String projectId;
	
	private String name;
	
	private LocalDateTime crDtimes;
	
	private LocalDateTime runDtimes;
}
