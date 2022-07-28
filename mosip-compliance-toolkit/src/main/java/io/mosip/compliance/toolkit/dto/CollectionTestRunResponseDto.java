package io.mosip.compliance.toolkit.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollectionTestRunResponseDto {

	List<CollectionTestRunDto> collectionsSummaryList;
}
