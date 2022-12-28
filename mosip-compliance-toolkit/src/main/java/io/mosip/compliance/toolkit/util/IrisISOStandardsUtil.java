package io.mosip.compliance.toolkit.util;

import io.mosip.biometrics.util.iris.IrisImageCompressionType;
import io.mosip.compliance.toolkit.constants.Purposes;

public final class IrisISOStandardsUtil {
	public static boolean isValidImageCompressionType(String purpose, IrisImageCompressionType compressionType) {
		switch (Purposes.fromCode(purpose)) {
		case AUTH:
		case REGISTRATION:
			if (compressionType == IrisImageCompressionType.JPEG_LOSSY
					|| compressionType == IrisImageCompressionType.JPEG_LOSSLESS_OR_NONE) {
				return true;
			}
			break;
		}

		return false;
	}
} 