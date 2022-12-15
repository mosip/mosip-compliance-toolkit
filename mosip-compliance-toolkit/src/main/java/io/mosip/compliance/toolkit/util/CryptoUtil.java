package io.mosip.compliance.toolkit.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;
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

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.compliance.toolkit.validators.SBIValidator.DecryptValidatorDto;
import io.mosip.compliance.toolkit.validators.SBIValidator.DeviceValidatorDto;
import io.mosip.kernel.core.util.DateUtils;
import io.restassured.http.Cookie;

public final class CryptoUtil {
	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	public static String getEncodedHash(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException {
		Security.addProvider(new BouncyCastleProvider());
		String algorithm = "SHA-256";
		String SECURITY_PROVIDER = "BC";

		MessageDigest digest = MessageDigest.getInstance(algorithm, SECURITY_PROVIDER);
		digest.update(bytes);
		byte[] hash = digest.digest();
		return Base64.getUrlEncoder().encodeToString(hash);
	}

	public static io.restassured.response.Response getDecryptPostResponse(String authUrl, String appId, String clientId,
			String secretKey, String postUrl, DecryptValidatorDto decryptValidatorDto) throws IOException {
		Cookie.Builder builder = new Cookie.Builder("Authorization", getAuthToken(authUrl, appId, clientId, secretKey));

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(decryptValidatorDto)
				.contentType("application/json").log().all().when().post(postUrl).then().log().all().extract()
				.response();
	}

	public static io.restassured.response.Response getTrustRootPostResponse(String authUrl, String appId, String clientId,
			String secretKey, String postUrl, DeviceValidatorDto deviceValidatorDto) throws IOException {
		Cookie.Builder builder = new Cookie.Builder("Authorization", getAuthToken(authUrl, appId, clientId, secretKey));

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
				.contentType("application/json").log().all().when().post(postUrl).then().log().all().extract()
				.response();
	}

	public static String getAuthToken(String authUrl, String appId, String clientId, String secretKey)
			throws IOException {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE, appId, clientId, secretKey,
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder().url(authUrl).post(body).build();

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.header("authorization");
		}
		return "";
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
