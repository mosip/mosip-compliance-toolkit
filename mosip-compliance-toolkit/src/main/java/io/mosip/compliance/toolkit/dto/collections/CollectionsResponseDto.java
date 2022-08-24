package io.mosip.compliance.toolkit.dto.collections;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CollectionsResponseDto {

	List<CollectionDto> collections;
}
