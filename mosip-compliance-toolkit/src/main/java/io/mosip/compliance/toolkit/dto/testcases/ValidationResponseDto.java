package io.mosip.compliance.toolkit.dto.testcases;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ValidationResponseDto {

	List<ValidationResultDto> validationsList;
}
