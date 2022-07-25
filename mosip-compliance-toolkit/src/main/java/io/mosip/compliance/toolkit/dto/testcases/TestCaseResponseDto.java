package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TestCaseResponseDto implements Serializable {
	private static final long serialVersionUID = -3032190434692790028L;

	@NotNull(message = "Values code can not be null")
    @Size(min=1, message = "Minimum one entry required")
	private Map<String, String> testCases;
}
