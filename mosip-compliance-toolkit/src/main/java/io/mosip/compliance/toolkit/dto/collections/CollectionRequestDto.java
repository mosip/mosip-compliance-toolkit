package io.mosip.compliance.toolkit.dto.collections;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CollectionRequestDto {
	private String projectId;
	private String projectType;
	private String collectionName;
}
