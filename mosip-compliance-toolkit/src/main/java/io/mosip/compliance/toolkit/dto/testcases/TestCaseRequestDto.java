package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TestCaseRequestDto implements Serializable {
	private static final long serialVersionUID = -3663309109332703904L;
	
	@NotNull(message = "Values code can not be null")
    @Size(min=1, message = "Minimum one entry required")
	private List<TestCaseDto> testCases;
}
