package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.CryptoUtil;
import io.mosip.compliance.toolkit.util.StringUtil;

@Component
public class AuthBioUtilValidator extends BioUtilValidator {
	public static final String KEY_SPLITTER = "#KEY_SPLITTER#";

	@Value("${mosip.service.keymanager.decrypt.appid}")
	private String appId;

	@Value("${mosip.service.keymanager.decrypt.refid}")
	private String refId;

	@Value("${mosip.service.keymanager.decrypt.url}")
	private String decryptUrl;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String responseJson = inputDto.getMethodResponse();

			ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper().readValue(responseJson,
					ObjectNode.class);

			JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
			if (!arrBiometricNodes.isNull() && arrBiometricNodes.isArray()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					JsonNode dataNode = biometricNode.get(DECODED_DATA);

					String purpose = dataNode.get(PURPOSE).asText();
					String bioType = dataNode.get(BIO_TYPE).asText();
					String bioValue = null;
					switch (Purposes.fromCode(purpose)) {
					case AUTH:
						bioValue = getDecryptBioValue(biometricNode.get(THUMB_PRINT).asText(),
								biometricNode.get(SESSION_KEY).asText(), KEY_SPLITTER,
								dataNode.get(TIME_STAMP).asText(), dataNode.get(TRANSACTION_ID).asText(),
								dataNode.get(BIO_VALUE).asText());
						break;
					case REGISTRATION:
						throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(),
								ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
					}
					validationResultDto = isValidISOTemplate(purpose, bioType, bioValue);
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE)) {
						break;
					}
				}
			}
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private String getDecryptBioValue(String thumbprint, String sessionKey, String keySplitter, String timestamp,
			String transactionId, String encryptedData) {
		byte[] xorResult = CryptoUtil.getXOR(timestamp, transactionId);
		byte[] aadBytes = CryptoUtil.getLastBytes(xorResult, 16);
		byte[] ivBytes = CryptoUtil.getLastBytes(xorResult, 12);

		byte[] thumbprintArr = CryptoUtil.hexStringToByteArray(thumbprint);
		byte[] sessionKeyArr = StringUtil.base64UrlDecode(sessionKey);
		byte[] keySplitterArr = StringUtil.toUtf8ByteArray(KEY_SPLITTER);
		byte[] dataArr = StringUtil.base64UrlDecode(encryptedData);

		String encodedData = StringUtil
				.base64UrlEncode(CryptoUtil.concatByteArrays(thumbprintArr, sessionKeyArr, keySplitterArr, dataArr));

		String requesttime = getCurrentDateAndTimeForAPI();
		DecryptValidatorDto decryptValidatorDto = new DecryptValidatorDto();
		decryptValidatorDto.setRequesttime(requesttime);

		DecryptRequestDto decryptRequest = new DecryptRequestDto();
		decryptRequest.setApplicationId(appId);
		decryptRequest.setReferenceId(refId);
		decryptRequest.setData(encodedData);
		decryptRequest.setSalt(StringUtil.base64UrlEncode(ivBytes));
		decryptRequest.setAad(StringUtil.base64UrlEncode(aadBytes));
		decryptRequest.setTimeStamp(requesttime);
		decryptValidatorDto.setRequest(decryptRequest);

		try {
			io.restassured.response.Response postResponse = CryptoUtil.getDecryptPostResponse(getAuthManagerUrl,
					getAuthAppId, getAuthClientId, getAuthSecretKey, decryptUrl, decryptValidatorDto);

			DecryptValidatorResponseDto decryptValidatorResponseDto = objectMapperConfig.objectMapper()
					.readValue(postResponse.getBody().asString(), DecryptValidatorResponseDto.class);

			if ((decryptValidatorResponseDto.getErrors() != null && decryptValidatorResponseDto.getErrors().size() > 0)
					|| (decryptValidatorResponseDto.getResponse().getData() == null)) {
				throw new ToolkitException(ToolkitErrorCodes.AUTH_BIO_VALUE_DECRYPT_ERROR.getErrorCode(),
						ToolkitErrorCodes.AUTH_BIO_VALUE_DECRYPT_ERROR.getErrorMessage());
			} else {
				return decryptValidatorResponseDto.getResponse().getData();
			}
		} catch (Exception e) {
			throw new ToolkitException(ToolkitErrorCodes.AUTH_BIO_VALUE_DECRYPT_ERROR.getErrorCode(),
					ToolkitErrorCodes.AUTH_BIO_VALUE_DECRYPT_ERROR.getErrorMessage() + e.getLocalizedMessage());
		}
	}
}
