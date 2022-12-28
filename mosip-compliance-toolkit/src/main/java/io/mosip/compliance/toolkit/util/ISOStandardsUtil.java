package io.mosip.compliance.toolkit.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.jnbis.api.model.Bitmap;
import org.jnbis.internal.WsqDecoder;

import io.mosip.compliance.toolkit.constants.Purposes;

public final class ISOStandardsUtil {
	public static boolean isValidImageType(String purpose, byte[] inImageData) throws Exception {
		switch (Purposes.fromCode(purpose)) {
		case AUTH:
			if (isJP2000(inImageData) || isWSQ(inImageData)) {
				return true;
			}
			break;
		case REGISTRATION:	
			if (isJP2000(inImageData)) {
				return true;
			}
			break;
		}
		
		return false;
	}
	
	/**
	 * Some Extra info about other file format with jpeg: initial of file contains
	 * these bytes BMP : 42 4D JPG : FF D8 FF EO ( Starting 2 Byte will always be
	 * same) PNG : 89 50 4E 47 GIF : 47 49 46 38 When a JPG file uses JFIF or EXIF,
	 * The signature is different : Raw : FF D8 FF DB JFIF : FF D8 FF E0 EXIF : FF
	 * D8 FF E1 WSQ : check with marker from WsqDecoder JP2000: 6a 70 32 68 // JP2
	 * Header
	 */
	public static boolean isJP2000(byte[] imageData) throws Exception {
		boolean isValid = false;
		DataInputStream ins = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(imageData)));
		try {
			while (true) {
				if (ins.readInt() == 0x6a703268) {
					isValid = true;
					break;
				}
			}
		} finally {
			ins.close();
		}
		return isValid;
	}

	public static boolean isWSQ(byte[] imageData) throws Exception {

		try {
			WsqDecoder decoder = new WsqDecoder();
			Bitmap bitmap = decoder.decode(imageData);
			if (bitmap != null && bitmap.getPixels() != null && bitmap.getPixels().length > 0) {
				return true;
			} else {
				return false;

			}
		} finally {
		}
	}
} 