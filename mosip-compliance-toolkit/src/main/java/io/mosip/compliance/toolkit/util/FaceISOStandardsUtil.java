package io.mosip.compliance.toolkit.util;

import io.mosip.biometrics.util.face.ImageDataType;
import io.mosip.compliance.toolkit.constants.Purposes;

public final class FaceISOStandardsUtil {
	public static boolean isValidImageCompressionType(String purpose, ImageDataType compressionType) {
		switch (Purposes.fromCode(purpose)) {
		case AUTH:
		case REGISTRATION:	
			if (compressionType == ImageDataType.JPEG2000_LOSSY || compressionType == ImageDataType.JPEG2000_LOSS_LESS) {
				return true;
			}
			break;
		}
		
		return false;
	}
}
