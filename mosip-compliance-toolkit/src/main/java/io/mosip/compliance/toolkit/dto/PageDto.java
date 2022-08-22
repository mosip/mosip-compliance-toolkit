package io.mosip.compliance.toolkit.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PageDto<T> {

	private int pageNo;

	private int pageSize;

	private int currentPageElements;

	private int totalPages;

	private long totalElements;

	private boolean hasNext;

	private boolean hasPrev;

	private String sort;

	private List<T> content;

}
