package io.mosip.compliance.toolkit.dto;

import java.util.List;

import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestRunHistoryResponseDto {

	private List<TestRunHistoryDto> testRunHistory;

	private PageableData pageData;
}
