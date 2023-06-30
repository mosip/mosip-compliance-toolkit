package io.mosip.compliance.toolkit.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashUtil {
    public String generateHash(String previousHash, String bioValue) throws Exception {
        String hash = null;
        try {
            byte[] previousDataByteArr;
            byte[] previousBioDataHash;
            if (previousHash == null || previousHash.trim().length() == 0) {
                previousDataByteArr = ("").getBytes();
                previousBioDataHash = generateHash(previousDataByteArr);
            } else {
                previousBioDataHash = decodeHex(previousHash);
            }
            byte[] decodedBytes = base64Decode(bioValue);
            byte[] currentBioDataHash = generateHash(decodedBytes);
            byte[] finalBioDataHash = new byte[currentBioDataHash.length + previousBioDataHash.length];
            System.arraycopy(previousBioDataHash, 0, finalBioDataHash, 0, previousBioDataHash.length);
            System.arraycopy(currentBioDataHash, 0, finalBioDataHash, previousBioDataHash.length,
                    currentBioDataHash.length);
            hash = toHex(generateHash(finalBioDataHash));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return hash;
    }

    public byte[] base64Decode(String bioValue) {
        String base64String = bioValue.replace('-', '+').replace('_', '/');
        int paddingLength = 4 - (base64String.length() % 4);
        base64String += "=".repeat(paddingLength);
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        return decodedBytes;
    }

    public String toHex(byte[] bytes) {
        return Hex.encodeHexString(bytes).toUpperCase();
    }

    private final String HASH_ALGORITHM_NAME = "SHA-256";

    public byte[] generateHash(final byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM_NAME);
        return messageDigest.digest(bytes);
    }

    public byte[] decodeHex(String hexData) throws DecoderException {
        return Hex.decodeHex(hexData);
    }
}
