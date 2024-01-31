package io.mosip.compliance.toolkit.dto.report;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class BiometricScores {

	private String sdkName;
	private List<FingerBioScoresTable> fingerTables;

	@Getter
	@Setter
	@Data
	public static class FingerBioScoresTable {
		private String ageGroup;
		private boolean isChildAgeGroup;
		private List<FingerBioScoresRow> rows;

		@Getter
		@Setter
		@Data
		public static class FingerBioScoresRow {
			private String bioScoreRange;
			private String maleChildScore;
			private String femaleChildScore;
			private String maleLabourerScore;
			private String maleNonLabourerScore;
			private String femaleLabourerScore;
			private String femaleNonLabourerScore;
		}
		
	}		
	
}
