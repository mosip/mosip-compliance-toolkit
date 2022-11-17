package io.mosip.compliance.toolkit.util;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public class ZipFileUtil {

    public static void checkForZipBombAttack(ZipInputStream zis) throws Exception {
        //check for first entry
        ZipEntry zipEntry = null;
        int nBytes = -1;
        byte[] buffer = new byte[2048];
        double totalSizeEntry = 0;
        double THRESHOLD_RATIO = 10;
        int THRESHOLD_ENTRIES = 10000;
        int THRESHOLD_SIZE = 1000000000; // 1 GB
        int totalSizeArchive = 0;
        int totalEntryArchive = 0;

		while ((zipEntry = zis.getNextEntry()) != null) {
			totalEntryArchive++;
			while ((nBytes = zis.read(buffer)) > 0) { // Compliant
				totalSizeEntry += nBytes;
				totalSizeArchive += nBytes;
				double compressionRatio = totalSizeEntry / zipEntry.getCompressedSize();
				if (compressionRatio > THRESHOLD_RATIO) {
					// ratio between compressed and uncompressed data is highly suspicious, looks
					// like a Zip Bomb Attack
					throw new ToolkitException(ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorCode(),
							ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorMessage());
				}
			}
		}
        if (totalSizeArchive > THRESHOLD_SIZE) {
            throw new ToolkitException(ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorCode(),
                    ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorMessage());
        }

        if (totalEntryArchive > THRESHOLD_ENTRIES) {
            throw new ToolkitException(ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorCode(),
                    ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorMessage());
        }
    }

}
