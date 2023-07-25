package io.mosip.compliance.toolkit.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.restassured.http.Cookie;

@Component
public class DataShareHelper {

	public static final String AUTHORIZATION = "Authorization";

	private static final String FILE = "file";

	private static final String MULTIPART_FORM_DATA = "multipart/form-data";

	@Autowired
	private AuthManagerHelper authManagerHelper;

	public io.restassured.response.Response getDataShareUrl(byte[] cbeffFileBytes, String dataShareFullCreateUrl)
			throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());

		return given().cookie(builder.build()).relaxedHTTPSValidation()
				.multiPart(FILE, "cbeff.xml", cbeffFileBytes, MULTIPART_FORM_DATA).contentType(MULTIPART_FORM_DATA)
				.when().post(dataShareFullCreateUrl).then().extract().response();
	}

	public io.restassured.response.Response callDataShareUrl(String urlToBeInvoked) throws IOException {
		Cookie.Builder builder = new Cookie.Builder(AUTHORIZATION, authManagerHelper.getAuthToken());
		return given().cookie(builder.build()).relaxedHTTPSValidation().when().get(urlToBeInvoked).then().extract()
				.response();

	}
}
