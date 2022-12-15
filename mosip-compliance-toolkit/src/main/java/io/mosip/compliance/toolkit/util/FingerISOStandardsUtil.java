package io.mosip.compliance.toolkit.util;

import io.mosip.biometrics.util.finger.FingerImageCompressionType;
import io.mosip.compliance.toolkit.constants.Purposes;

public final class FingerISOStandardsUtil {
	public static boolean isValidImageCompressionType(String purpose, FingerImageCompressionType compressionType) {
		switch (Purposes.fromCode(purpose)) {
		case AUTH:
		case REGISTRATION:
			if (compressionType == FingerImageCompressionType.JPEG_2000_LOSSY
					|| compressionType == FingerImageCompressionType.JPEG_2000_LOSS_LESS
					|| compressionType == FingerImageCompressionType.WSQ) {
				return true;
			}
			break;
		}

		return false;
	}
}