package io.mosip.compliance.toolkit.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestDataValidationDto {
	private boolean validated = false;
	private String purpose;
	private List<String> folders;
	private List<String> probeFolders;
	private List<String> galleryFolders;
}
