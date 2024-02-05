package io.mosip.compliance.toolkit.dto.report;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BiometricScores {

	private String sdkName;
	private List<BiometricScoresTable> tables;

	@Getter
	@Setter
	@Data
	public static class BiometricScoresTable {
		private String ageGroup;
		private boolean isChildAgeGroup;
		private List<BiometricScoresRow> rows;

		@Getter
		@Setter
		@Data
		public static class BiometricScoresRow {
			private String bioScoreRange;
			private String maleChildScore = "0";
			private String femaleChildScore = "0";
			private Map<String, String> maleScores;
			private Map<String, String> femaleScores;
		}
	}

}
