package io.mosip.compliance.toolkit.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollectionDto {

	private String collectionId;

	private String projectId;

	private String name;

	private int testCaseCount;

	private LocalDateTime crDtimes;

	private LocalDateTime runDtimes;
	
	private String runId;

}
