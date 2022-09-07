package io.mosip.compliance.toolkit.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public final class ConverterDataUtil {
	/*
	*	Some Extra info about other file format with jpeg: initial of file contains these bytes
	*	BMP : 42 4D
	*	JPG : FF D8 FF EO ( Starting 2 Byte will always be same)
	* 	PNG : 89 50 4E 47
	* 	GIF : 47 49 46 38
	*  	When a JPG file uses JFIF or EXIF, The signature is different :
	*	Raw  : FF D8 FF DB
	*	JFIF : FF D8 FF E0
	*	EXIF : FF D8 FF E1
	*
 	*/
	public static Boolean isJPEG(byte[] imageData) throws Exception {
		DataInputStream ins = new DataInputStream(new BufferedInputStream (new ByteArrayInputStream (imageData)));
		try {
			if (ins.readInt() == 0xffd8ffe0) {
				return true;
			} else {
				return false;

			}
		} finally {
			ins.close();
		}
	}

	public static Boolean isPNG(byte[] imageData) throws Exception {
		DataInputStream ins = new DataInputStream(new BufferedInputStream (new ByteArrayInputStream (imageData)));
		try {
			if (ins.readInt() == 0x89504e47) {
				return true;
			} else {
				return false;

			}
		} finally {
			ins.close();
		}
	}
} 