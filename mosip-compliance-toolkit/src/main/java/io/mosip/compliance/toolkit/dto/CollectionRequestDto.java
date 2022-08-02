package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class CollectionRequestDto {
	private String type;
	private CollectionDto collection;
}
