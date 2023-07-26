package io.mosip.compliance.toolkit.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.restassured.http.Cookie;

@Component
public class PartnerManagerHelper {

	public static final String AUTHORIZATION = "Authorization";

	private static final String APPLICATION_JSON = "application/json";

	@Value("${mosip.service.partnermanager.getparnter.url}")
	private String getPartnerUrl;

	@Autowired
	private AuthManagerHelper authManagerHelper;

	public io.restassured.response.Response getPartnerDetails(String partnerId) throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation().contentType(APPLICATION_JSON).when()
				.get(getPartnerUrl + "/" + partnerId).then().extract().response();
	}
}
