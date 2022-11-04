package io.mosip.compliance.toolkit.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Base64;

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
	
}
