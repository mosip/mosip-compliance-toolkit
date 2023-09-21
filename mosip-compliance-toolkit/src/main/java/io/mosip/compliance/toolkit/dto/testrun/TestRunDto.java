package io.mosip.compliance.toolkit.dto.testrun;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunDto {

	private String id;

	private String collectionId;

	private LocalDateTime runDtimes;

	private LocalDateTime executionDtimes;

	private String runConfigurationJson;
	private String executionStatus;
	private String runStatus;
}
