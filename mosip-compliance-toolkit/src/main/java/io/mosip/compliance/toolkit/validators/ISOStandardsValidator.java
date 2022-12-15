package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.ImageDataType;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerImageCompressionType;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.biometrics.util.iris.IrisImageCompressionType;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.DeviceTypes;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.CryptoUtil;
import io.mosip.compliance.toolkit.util.FaceISOStandardsUtil;
import io.mosip.compliance.toolkit.util.FingerISOStandardsUtil;
import io.mosip.compliance.toolkit.util.ISOStandardsUtil;
import io.mosip.compliance.toolkit.util.IrisISOStandardsUtil;
import io.mosip.compliance.toolkit.util.StringUtil;

@Component
public class ISOStandardsValidator extends SBIValidator {
	public static final String KEY_SPLITTER = "#KEY_SPLITTER#";
	public static final String ISO19794_5_2011 = "ISO19794_5_2011";
	public static final String ISO19794_6_2011 = "ISO19794_6_2011";
	public static final String ISO19794_4_2011 = "ISO19794_4_2011";

	public static final String BIO = "bio";
	public static final String DECODED_DATA = "dataDecoded";
	public static final String THUMB_PRINT = "thumbprint";
	public static final String SESSION_KEY = "sessionKey";

	public static final String BIO_VALUE = "bioValue";
	public static final String TIME_STAMP = "timestamp";
	public static final String TRANSACTION_ID = "transactionId";
	public static final String PURPOSE = "purpose";
	public static final String BIO_TYPE = "bioType";
	public static final String BIO_SUBTYPE = "bioSubType";

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
						bioValue = dataNode.get(BIO_VALUE).asText();
						break;
					default:
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

	protected ValidationResultDto isValidISOTemplate(String purpose, String bioType, String bioValue) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceTypes deviceTypeCode = DeviceTypes.fromCode(bioType);

		if (bioValue != null) {
			switch (deviceTypeCode) {
			case FINGER:
				validationResultDto = isValidFingerISOTemplate(purpose, bioValue);
				break;
			case IRIS:
				validationResultDto = isValidIrisISOTemplate(purpose, bioValue);
				break;
			case FACE:
				validationResultDto = isValidFaceISOTemplate(purpose, bioValue);
				break;
			default:
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription(
						"ISOStandardsValidator failure - " + "with Message - " + " invalid bioType = " + bioType);
				break;
			}
		} else {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " isValidISOTemplate 'bioValue' is Empty or Null");
		}
		return validationResultDto;
	}

	private ValidationResultDto isValidFingerISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.FINGER.getCode());
		requestDto.setVersion(ISO19794_4_2011);

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		FingerBDIR bdir;
		try {
			bdir = FingerDecoder.getFingerBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FINGER_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		FingerImageCompressionType compressionType = bdir.getRepresentation().getRepresentationHeader()
				.getCompressionType();
		if (!FingerISOStandardsUtil.isValidImageCompressionType(purpose, compressionType)) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " Invalid Image Compression Type for Finger Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS]");
			return validationResultDto;
		}

		byte[] inImageData = bdir.getRepresentation().getRepresentationBody().getImageData().getImage();		
		try {
			if (!ISOStandardsUtil.isValidImageType(purpose, inImageData))
			{
				validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
						+ " isValidISOTemplate is not valid for image type JP2000 and WSQ");
				return validationResultDto;
			}
		} catch (Exception e) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " isValidImageType is not valid " + e.getLocalizedMessage());
			return validationResultDto;
		}
		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}

	private ValidationResultDto isValidIrisISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.IRIS.getCode());
		requestDto.setVersion(ISO19794_6_2011);

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		IrisBDIR bdir;
		try {
			bdir = IrisDecoder.getIrisBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_IRIS_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		IrisImageCompressionType compressionType = bdir.getRepresentation().getRepresentationHeader()
				.getImageInformation().getCompressionType();
		if (!IrisISOStandardsUtil.isValidImageCompressionType(purpose, compressionType)) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " Invalid Image Compression Type for Iris Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS]");
			return validationResultDto;
		}

		byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
		try {
			if (!ISOStandardsUtil.isValidImageType(purpose, inImageData))
			{
				validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
						+ " isValidISOTemplate is not valid for image type JP2000 and WSQ");
				return validationResultDto;
			}
		} catch (Exception e) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " isValidImageType is not valid " + e.getLocalizedMessage());
			return validationResultDto;
		}
		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}

	private ValidationResultDto isValidFaceISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.FACE.getCode());
		requestDto.setVersion(ISO19794_5_2011);

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64(bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		FaceBDIR bdir;
		try {
			bdir = FaceDecoder.getFaceBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FACE_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException(errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		ImageDataType compressionType = bdir.getRepresentation().getRepresentationHeader().getImageInformation()
				.getImageDataType();
		if (!FaceISOStandardsUtil.isValidImageCompressionType(purpose, compressionType)) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " Invalid Image Compression Type for Face Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS]");
			return validationResultDto;
		}

		byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
		try {
			if (!ISOStandardsUtil.isValidImageType(purpose, inImageData))
			{
				validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
						+ " isValidISOTemplate is not valid for image type JP2000 and WSQ");
				return validationResultDto;
			}
		} catch (Exception e) {
			validationResultDto.setDescription("ISOStandardsValidator failure - " + "with Message - "
					+ " isValidImageType is not valid " + e.getLocalizedMessage());
			return validationResultDto;
		}
		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}
}
