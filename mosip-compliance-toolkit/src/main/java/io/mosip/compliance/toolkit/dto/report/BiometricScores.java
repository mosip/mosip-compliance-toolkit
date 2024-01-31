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
	private List<FaceBioScoresTable> faceTables;

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
			private String maleChildScore = "0";
			private String femaleChildScore = "0";
			private String maleLabourerScore = "0";
			private String maleNonLabourerScore = "0";
			private String femaleLabourerScore = "0";
			private String femaleNonLabourerScore = "0";
		}
		
	}

	@Getter
	@Setter
	@Data
	public static class FaceBioScoresTable {
		private String ageGroup;
		private List<FaceBioScoresRow> rows;

		@Getter
		@Setter
		@Data
		public static class FaceBioScoresRow {
			private String bioScoreRange;
			private String maleAsianScore = "0";
			private String femaleAsianScore = "0";
			private String maleAfricanScore = "0";
			private String femaleAfricanScore = "0";
			private String maleEuropeanScore = "0";
			private String femaleEuropeanScore = "0";
		}

	}

}
