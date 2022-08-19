package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PageableData {

	private int pageNo;

	private int pageSize;

	private int numberOfElements;

	private boolean isFirst;

	private boolean isLast;

	private boolean hasNext;

	private boolean hasPrev;

	private String sort;

}
