package io.mosip.compliance.toolkit.validators;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.compliance.toolkit.validators.ToolkitValidatorHelper.DeviceValidatorDto;
import io.mosip.kernel.core.util.DateUtils;
import io.restassured.http.Cookie;
import lombok.Data;

public abstract class ToolkitValidatorHelper{
	@Value("${ida.auth.appid}")
	private String getAuthAppId;

	@Value("${ida.auth.clientid}")
	private String getAuthClientId;

	@Value("${ida.auth.secretkey}")
	private String getAuthSecretKey;

	@Value("${ida.validation.url}")
	private String getValidationUrl;

	@Value("${keymanager.verifyCertificateTrust.url}")
	private String getKeyManagerVerifyCertificateTrustUrl;

	private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	private static final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";

	private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

	@Autowired
	ObjectMapper objectMapper;

	protected String getCertificateData(String certificateInfo) {
		return BEGIN_CERTIFICATE + certificateInfo + END_CERTIFICATE;
	}

	protected io.restassured.response.Response getPostResponse(String authUrl, String postUrl, DeviceValidatorDto deviceValidatorDto) throws IOException {
		Cookie.Builder builder = new Cookie.Builder("Authorization", getAuthToken(authUrl));
		
		return given().cookie(builder.build()).relaxedHTTPSValidation()
				.body(deviceValidatorDto).contentType("application/json").log().all().when()
				.post(postUrl).then().log().all().extract().response();
	}
	
	protected String getAuthToken(String url) throws IOException {
		OkHttpClient client = new OkHttpClient();
		String requestBody = String.format(AUTH_REQ_TEMPLATE, getAuthAppId, getAuthClientId, getAuthSecretKey,
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder().url(url).post(body).build();

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response.header("authorization");
		}
		return "";
	}

	protected String getCurrentDateAndTimeForAPI() {
		String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
		LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
		String currentTime = time.format(dateFormat);
		return currentTime;
	}

	@Data
	protected class DeviceTrustRequestDto {

		String certificateData;
		String partnerDomain;
	}

	@Data
	protected class DeviceValidatorDto {

		String id;
		Object metadata;
		DeviceTrustRequestDto request;
		String requesttime;
		String version;
	}

	@Data
	protected class DeviceValidatorResponseDto {
		String id;
		Object metadata;
		DeviceValidatorResponse response;
		String responsetime;
		String version;
		List<ErrorDto> errors;
	}

	@Data
	public class DeviceValidatorResponse {
		String status;
	}

	@Data
	protected class DeviceValidatorRequestDto {
		String deviceCode;
		String deviceServiceVersion;
		DeviceValidatorDigitalIdDto digitalId;
		String purpose;
		String timeStamp;
	}
	
	@Data
	protected class DeviceValidatorDigitalIdDto {
		String dateTime;
		String deviceSubType;
		String dp;
		String dpId;
		String make;
		String model;
		String serialNo;
		String type;
	}
	
	@Data
	protected class ErrorDto {
		String errorCode;
		String message;
	}
}
