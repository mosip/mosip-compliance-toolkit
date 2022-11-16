package io.mosip.compliance.toolkit.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public class ZipFileUtil {
	double THRESHOLD_RATIO = 10;
	int THRESHOLD_ENTRIES = 10000;
	int THRESHOLD_SIZE = 1000000000; // 1 GB

	public byte[] getZipEntryBytes(ZipInputStream zis, long entryCompressedSize) throws IOException {
		byte[] zipEntryBytes = null;
		int nBytes = -1;
		byte[] buffer = new byte[2048];
		int totalSizeEntry = 0;

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while ((nBytes = zis.read(buffer)) > 0) { // Compliant
			out.write(buffer, 0, nBytes);
			totalSizeEntry += nBytes;
			checkCompressionRatio(totalSizeEntry, entryCompressedSize);
		}
		zipEntryBytes = out.toByteArray();
		out.close();
		return zipEntryBytes;
	}

	public int getZipEntrySize(ZipInputStream zis, long entryCompressedSize) throws IOException {
		int nBytes = -1;
		byte[] buffer = new byte[2048];
		int totalSizeEntry = 0;

		while ((nBytes = zis.read(buffer)) > 0) { // Compliant
			totalSizeEntry += nBytes;
			checkCompressionRatio(totalSizeEntry, entryCompressedSize);
		}
		return totalSizeEntry;
	}

	private void checkCompressionRatio(int totalSizeEntry, long entryCompressedSize) {
		double compressionRatio = totalSizeEntry / entryCompressedSize;
		if (compressionRatio > THRESHOLD_RATIO) {
			// ratio between compressed and uncompressed data is highly suspicious, looks
			// like a Zip Bomb Attack
			throw new ToolkitException(ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorCode(),
					ToolkitErrorCodes.ZIP_HIGH_COMPRESSION_RATIO_ERROR.getErrorMessage());
		}
	}

	public void checkZipFileSize(int totalSizeArchive) {
		if (totalSizeArchive > THRESHOLD_SIZE) {
			throw new ToolkitException(ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorCode(),
					ToolkitErrorCodes.ZIP_SIZE_TOO_LARGE_ERROR.getErrorMessage());
		}
	}

	public void checkZipEntryCount(int totalEntryCountArchive) {
		if (totalEntryCountArchive > THRESHOLD_ENTRIES) {
			throw new ToolkitException(ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorCode(),
					ToolkitErrorCodes.ZIP_ENTRIES_TOO_MANY_ERROR.getErrorMessage());
		}
	}

}
