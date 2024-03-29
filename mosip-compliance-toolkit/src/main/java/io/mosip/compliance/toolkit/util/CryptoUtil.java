package io.mosip.compliance.toolkit.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class CryptoUtil {

	public static String getEncodedHash(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		String algorithm = "SHA-256";
		String SECURITY_PROVIDER = "BC";

		MessageDigest digest = MessageDigest.getInstance(algorithm, SECURITY_PROVIDER);
		digest.update(bytes);
		byte[] hash = digest.digest();
		return Base64.getUrlEncoder().encodeToString(hash);
	}

	// Function to return the XOR
	// of the given strings
	public static byte[] getXOR(String a, String b) {
		byte[] aBytes = a.getBytes();
		byte[] bBytes = b.getBytes();
		// Lengths of the given strings
		int aLen = aBytes.length;
		int bLen = bBytes.length;

		// Make both the strings of equal lengths
		// by inserting 0s in the beginning
		if (aLen > bLen) {
			bBytes = prependZeros(bBytes, aLen - bLen);
		} else if (bLen > aLen) {
			aBytes = prependZeros(aBytes, bLen - aLen);
		}

		// Updated length
		int len = Math.max(aLen, bLen);
		byte[] xorBytes = new byte[len];

		// To store the resultant XOR
		for (int i = 0; i < len; i++) {
			xorBytes[i] = (byte) (aBytes[i] ^ bBytes[i]);
		}
		return xorBytes;
	}

	// Function to insert n 0s in the
	// beginning of the given string
	public static byte[] prependZeros(byte[] str, int n) {
		byte[] newBytes = new byte[str.length + n];
		int i = 0;
		for (; i < n; i++) {
			newBytes[i] = 0;
		}

		for (int j = 0; i < newBytes.length; i++, j++) {
			newBytes[i] = str[j];
		}

		return newBytes;
	}

	public static byte[] getLastBytes(byte[] xorBytes, int lastBytesNum) {
		assert (xorBytes.length >= lastBytesNum);
		return Arrays.copyOfRange(xorBytes, xorBytes.length - lastBytesNum, xorBytes.length);
	}

	public static byte[] decodeHex(String hexData) throws DecoderException {
		return Hex.decodeHex(hexData);
	}

	public static byte[] hexStringToByteArray(String thumbprint) {
		int len = thumbprint.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(thumbprint.charAt(i), 16) << 4)
					+ Character.digit(thumbprint.charAt(i + 1), 16));
		}
		return data;
	}

	public static byte[] concatByteArrays(byte[] thumbprint, byte[] sessionkey, byte[] keySplitter, byte[] data) {
		ByteBuffer result = ByteBuffer
				.allocate(thumbprint.length + sessionkey.length + keySplitter.length + data.length);
		result.put(thumbprint);
		result.put(sessionkey);
		result.put(keySplitter);
		result.put(data);
		return result.array();
	}
}
