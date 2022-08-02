package io.mosip.compliance.toolkit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TestcasesCollectionResponseDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2951881784167927358L;

	private List<TestcaseCollectionDto> testcaseCollectionDto;

}
