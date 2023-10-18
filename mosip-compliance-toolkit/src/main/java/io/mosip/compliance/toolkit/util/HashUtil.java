package io.mosip.compliance.toolkit.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Base64;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.EmptyCheckUtils;

public class HashUtil {

	private static Logger log = LoggerConfiguration.logConfig(HashUtil.class);

	private static final String HASH_ALGORITHM_NAME = "SHA-256";

	public static String generateHash(String previousHash, byte[] decodedBioValue) throws Exception {
		String hash = null;
		try {
			byte[] previousBioDataHash = null;
			if (previousHash == null || previousHash.trim().length() == 0 || "".equals(previousHash)) {
				byte[] previousDataByteArr = "".getBytes(StandardCharsets.UTF_8);
				previousBioDataHash = generateHash(previousDataByteArr);
			} else {
				previousBioDataHash = decodeHex(previousHash);
			}
			byte[] currentDataByteArr = decodedBioValue;
			// Here Byte Array
			byte[] currentBioDataHash = generateHash(currentDataByteArr);
			byte[] finalBioDataHash = new byte[currentBioDataHash.length + previousBioDataHash.length];
			System.arraycopy(previousBioDataHash, 0, finalBioDataHash, 0, previousBioDataHash.length);
			System.arraycopy(currentBioDataHash, 0, finalBioDataHash, previousBioDataHash.length,
					currentBioDataHash.length);
			hash = toHex(generateHash(finalBioDataHash));
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id", "Hash generation Error: " + ex.getLocalizedMessage());
		}
		return hash;
	}

	public static byte[] base64UrlDecode(String data) {
		if (EmptyCheckUtils.isNullEmpty(data)) {
			return null;
		}
		return Base64.getUrlDecoder().decode(data);
	}

	public static String toHex(byte[] bytes) {
		return Hex.encodeHexString(bytes).toUpperCase();
	}

	public static byte[] generateHash(final byte[] bytes) throws Exception {
//		MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_NAME);
//		return messageDigest.digest(bytes);
		Security.addProvider(new BouncyCastleProvider());
		String SECURITY_PROVIDER = "BC";

		MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM_NAME, SECURITY_PROVIDER);
		digest.reset();
		digest.update(bytes);
		byte[] hash = digest.digest();
		return hash;
	}

	public static byte[] decodeHex(String hexData) throws DecoderException {
		return Hex.decodeHex(hexData);
	}

}
