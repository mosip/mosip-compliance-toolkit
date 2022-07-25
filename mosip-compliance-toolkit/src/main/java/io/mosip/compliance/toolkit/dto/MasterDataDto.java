package io.mosip.compliance.toolkit.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class MasterDataDto {
	
	private String name;
	
	private String description;
	
	private List<MasterDataValueDto> data;
}
