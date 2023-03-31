package io.mosip.compliance.toolkit.dto.abis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class InsertRequestDto {

	private String id;
	private String version;
	private String requestId;
	private String requesttime;
	private String referenceId;
	private String referenceURL;
}
