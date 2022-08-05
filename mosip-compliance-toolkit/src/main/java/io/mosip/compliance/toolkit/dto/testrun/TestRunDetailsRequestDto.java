package io.mosip.compliance.toolkit.dto.testrun;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunDetailsRequestDto {

	private String runId;

	private List<TestRunDetailsDto> testRunDetailsList;
}
