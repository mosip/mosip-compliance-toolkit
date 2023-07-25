package io.mosip.compliance.toolkit.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.kernel.core.util.DateUtils;

@Component
public class AuthManagerHelper {

	public static final String AUTHORIZATION = "Authorization";

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

	private String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

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
