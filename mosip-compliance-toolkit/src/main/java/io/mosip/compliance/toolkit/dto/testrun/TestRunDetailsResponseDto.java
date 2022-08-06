package io.mosip.compliance.toolkit.dto.testrun;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunDetailsResponseDto {

	private String collectionId;

	private String runId;

	private LocalDateTime runDtimes;

	private LocalDateTime executionDtimes;

	private List<TestRunDetailsDto> testRunDetailsList;

}
