package io.mosip.compliance.toolkit.validators;

import java.nio.charset.Charset;
import java.util.Base64;

public abstract class SDKValidator extends ToolkitValidator {

	public String base64Encode(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public String base64Decode(String data) {
		return new String (Base64.getDecoder().decode(data), Charset.forName("UTF-8"));
	}
}
