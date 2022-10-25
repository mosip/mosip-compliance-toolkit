package io.mosip.compliance.toolkit.util;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestDataValidationUtil {
	private boolean validated = false;
	private String purpose;
	private List<String> folders;
	private List<String> probeFolders;
}
