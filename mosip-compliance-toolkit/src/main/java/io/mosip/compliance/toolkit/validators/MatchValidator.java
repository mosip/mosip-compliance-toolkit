package io.mosip.compliance.toolkit.validators;

import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.Match;

import java.util.Map;

public class MatchValidator extends MatchBaseValidator {

	@Override
	protected void setMatchResults(ValidationInputDto inputDto, Map<Integer, Map<BiometricType, Boolean>> resultsMap, Integer galleryIndex,
						   BiometricType biometricType, String matchValue) {
		if (!inputDto.isNegativeTestCase()) {
			if (Match.MATCHED.toString().equals(matchValue)) {
				setResults(resultsMap, galleryIndex, biometricType, Boolean.TRUE);
			} else {
				setResults(resultsMap, galleryIndex, biometricType, Boolean.FALSE);
			}
		} else {
			if (Match.NOT_MATCHED.toString().equals(matchValue)) {
				setResults(resultsMap, galleryIndex, biometricType, Boolean.TRUE);
			} else {
				setResults(resultsMap, galleryIndex, biometricType, Boolean.FALSE);
			}
		}
	}
}
