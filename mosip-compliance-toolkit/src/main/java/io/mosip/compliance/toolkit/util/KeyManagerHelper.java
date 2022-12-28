package io.mosip.compliance.toolkit.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.compliance.toolkit.validators.SBIValidator.DecryptValidatorDto;
import io.mosip.compliance.toolkit.validators.SBIValidator.DeviceValidatorDto;
import io.mosip.kernel.core.util.DateUtils;
import io.restassured.http.Cookie;

@Component
public class KeyManagerHelper {

	private static final String APPLICATION_JSON = "application/json";

	private static final String AUTHORIZATION = "Authorization";

	@Value("${mosip.service.auth.appid}")
	private String getAuthAppId;

	@Value("${mosip.service.auth.clientid}")
	private String getAuthClientId;

	@Value("${mosip.service.auth.secretkey}")
	private String getAuthSecretKey;

	@Value("${mosip.service.authmanager.url}")
	private String getAuthManagerUrl;

	@Value("${mosip.service.keymanager.decrypt.appid}")
	private String appId;

	@Value("${mosip.service.keymanager.decrypt.refid}")
	private String refId;

	@Value("${mosip.service.keymanager.verifyCertificateTrust.url}")
	protected String keyManagerTrustUrl;

	@Value("${mosip.service.keymanager.decrypt.url}")
	private String keyManagerDecryptUrl;

	@Value("${mosip.service.keymanager.encryption.key.url}")
	private String keyManagerGetEncryptionKeyUrl;

	private String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

	public String getAppId() {
		return appId;
	}

	public String getRefId() {
		return refId;
	}

	public io.restassured.response.Response decryptionResponse(DecryptValidatorDto decryptValidatorDto)
			throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(decryptValidatorDto)
				.contentType(APPLICATION_JSON).when().post(keyManagerDecryptUrl).then().extract().response();
	}

	public io.restassured.response.Response trustValidationResponse(DeviceValidatorDto deviceValidatorDto)
			throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
				.contentType(APPLICATION_JSON).when().post(keyManagerTrustUrl).then().extract().response();
	}

	public io.restassured.response.Response encryptionKeyResponse() throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().contentType(APPLICATION_JSON).when()
				.get(keyManagerGetEncryptionKeyUrl).then().extract().response();
	}

	public String getAuthToken() throws IOException {
		OkHttpClient client = new OkHttpClient();
		String authToken = "";
		String requestBody = String.format(AUTH_REQ_TEMPLATE, getAuthAppId, getAuthClientId, getAuthSecretKey,
				DateUtils.getUTCCurrentDateTime());

		MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(mediaType, requestBody);
		Request request = new Request.Builder().url(getAuthManagerUrl).post(body).build();

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			authToken = response.header("authorization");
		}
		if (response != null && response.body() != null) {
			response.body().close();
		}
		return authToken;
	}

}
