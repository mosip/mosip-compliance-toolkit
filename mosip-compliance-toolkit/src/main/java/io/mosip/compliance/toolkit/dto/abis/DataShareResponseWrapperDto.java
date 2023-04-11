package io.mosip.compliance.toolkit.dto.abis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DataShareResponseWrapperDto {

	private DataShareResponseDto dataShareResponseDto;
	private String testDataSource;
}
