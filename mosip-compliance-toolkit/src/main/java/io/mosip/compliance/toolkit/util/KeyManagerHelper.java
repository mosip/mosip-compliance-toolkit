package io.mosip.compliance.toolkit.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.validators.SBIValidator.DecryptValidatorDto;
import io.mosip.compliance.toolkit.validators.SBIValidator.DeviceValidatorDto;
import io.restassured.http.Cookie;

@Component
public class KeyManagerHelper {

	private static final String APPLICATION_JSON = "application/json";

	public static final String AUTHORIZATION = "Authorization";

	@Autowired
	private AuthManagerHelper authManagerHelper;
	
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

	public String getAppId() {
		return appId;
	}

	public String getRefId() {
		return refId;
	}
	
	public io.restassured.response.Response decryptionResponse(DecryptValidatorDto decryptValidatorDto)
			throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(decryptValidatorDto)
				.contentType(APPLICATION_JSON).when().post(keyManagerDecryptUrl).then().extract().response();
	}

	public io.restassured.response.Response trustValidationResponse(DeviceValidatorDto deviceValidatorDto)
			throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
				.contentType(APPLICATION_JSON).when().post(keyManagerTrustUrl).then().extract().response();
	}

	public io.restassured.response.Response encryptionKeyResponse() throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().contentType(APPLICATION_JSON).when()
				.get(keyManagerGetEncryptionKeyUrl).then().extract().response();
	}

}
