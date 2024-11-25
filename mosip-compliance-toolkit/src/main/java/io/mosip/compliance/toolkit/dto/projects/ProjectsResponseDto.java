package io.mosip.compliance.toolkit.dto.projects;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ProjectsResponseDto implements Serializable {

	private static final long serialVersionUID = -3143251154335506412L;
	
	private List<ProjectDto> projects;

}
