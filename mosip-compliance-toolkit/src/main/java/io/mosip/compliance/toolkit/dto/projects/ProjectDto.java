package io.mosip.compliance.toolkit.dto.projects;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectDto {

	private String id;
	
	private String name;
	
	private String projectType;
	
	private long collectionsCount;
	
	private LocalDateTime crDate;

	private LocalDateTime lastRunDt;
	
	private String lastRunId;
}
