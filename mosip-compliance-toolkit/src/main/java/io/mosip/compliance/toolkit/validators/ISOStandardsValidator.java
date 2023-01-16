package io.mosip.compliance.toolkit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.Modality;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.face.FaceISOStandardsValidator;
import io.mosip.biometrics.util.face.FaceQualityBlock;
import io.mosip.biometrics.util.face.LandmarkPoints;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerCertificationBlock;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.finger.FingerISOStandardsValidator;
import io.mosip.biometrics.util.finger.FingerQualityBlock;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.biometrics.util.iris.IrisISOStandardsValidator;
import io.mosip.biometrics.util.iris.IrisImageCompressionType;
import io.mosip.biometrics.util.iris.IrisQualityBlock;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.DeviceTypes;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.CryptoUtil;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class ISOStandardsValidator extends SBIValidator {

	@Autowired
	private KeyManagerHelper keyManagerHelper;

	private Logger log = LoggerConfiguration.logConfig(ISOStandardsValidator.class);

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
						bioValue = getDecryptedBioValue(biometricNode.get(THUMB_PRINT).asText(),
								biometricNode.get(SESSION_KEY).asText(), KEY_SPLITTER,
								dataNode.get(TIME_STAMP).asText(), dataNode.get(TRANSACTION_ID).asText(),
								dataNode.get(BIO_VALUE).asText());
						log.info("sessionId", "idType", "id", "auth bioValue - " + bioValue);
						log.debug("sessionId", "idType", "id", "auth bioValue - " + bioValue);
						break;
					case REGISTRATION:
						bioValue = dataNode.get(BIO_VALUE).asText();
						break;
					default:
						throw new ToolkitException(ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(),
								ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
					}
					validationResultDto = doISOValidations(purpose, bioType, bioValue);
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

	public String getDecryptedBioValue(String thumbprint, String sessionKey, String keySplitter, String timestamp,
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
		decryptRequest.setApplicationId(keyManagerHelper.getAppId());
		decryptRequest.setReferenceId(keyManagerHelper.getRefId());
		decryptRequest.setData(encodedData);
		decryptRequest.setSalt(StringUtil.base64UrlEncode(ivBytes));
		decryptRequest.setAad(StringUtil.base64UrlEncode(aadBytes));
		decryptRequest.setTimeStamp(requesttime);
		decryptValidatorDto.setRequest(decryptRequest);

		try {
			io.restassured.response.Response postResponse = keyManagerHelper.decryptionResponse(decryptValidatorDto);

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

	protected ValidationResultDto doISOValidations(String purpose, String bioType, String bioValue) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceTypes deviceTypeCode = DeviceTypes.fromCode(bioType);

		if (bioValue != null) {
			switch (deviceTypeCode) {
			case FINGER:
				validationResultDto = doFingerISOValidations(purpose, bioValue);
				break;
			case IRIS:
				validationResultDto = doIrisISOValidations(purpose, bioValue);
				break;
			case FACE:
				validationResultDto = doFaceISOValidations(purpose, bioValue);
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

	private ValidationResultDto doFingerISOValidations(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		StringBuilder message = new StringBuilder("ISOStandardsValidator failure - " + "with Message - ");
		boolean isValid = true;

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.FINGER.getCode());
		requestDto.setVersion(ISO19794_4_2011);

		byte[] bioData = null;
		try {
			bioData = CommonUtil.decodeURLSafeBase64(bioValue);
			requestDto.setInputBytes(bioData);
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

		if (!FingerISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
			message.append("<BR>Invalid Format Identifier for Finger Modality, allowed values[0x46495200], input value["
					+ Long.toHexString(bdir.getFormatIdentifier()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
			message.append("<BR>Invalid Version Number for Finger Modality, allowed values[0x30323000], input value["
					+ Long.toHexString(bdir.getVersionNumber()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
				bdir.getRecordLength())) {
			message.append("<BR>Invalid Record Length for Finger Modality, input value["
					+ Integer.toHexString(bioData != null ? bioData.length : 0) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidNoOfRepresentations(bdir.getNoOfRepresentations())) {
			message.append("<BR>Invalid No Of Representations for Finger Modality, allowed values[0x0001], input value["
					+ Integer.toHexString(bdir.getNoOfRepresentations()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
			message.append(
					"<BR>Invalid Certification Flag for Finger Modality, allowed values[0x00, 0x01], input value["
							+ Integer.toHexString(bdir.getCertificationFlag()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidNoOfFingerPresent(bdir.getNoOfFingerPresent())) {
			message.append("<BR>Invalid No Of Finger Present for Finger Modality, allowed values[0x01], input value["
					+ Integer.toHexString(bdir.getNoOfFingerPresent()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidRepresentationLength(bdir.getRepresentationsLength())) {
			message.append(
					"<BR>Invalid Representation Length for Finger Modality, allowed values between[0x00000029 And 0xFFFFFFEF], input value["
							+ Long.toHexString(bdir.getRecordLength()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
				bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(), bdir.getCaptureMinute(),
				bdir.getCaptureSecond(), bdir.getCaptureMilliSecond())) {
			message.append("<BR>Invalid CaptureDateTime for Finger Modality, The capture date and time field shall \r\n"
					+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
					+ "Universal Time (UTC). The capture date \r\n"
					+ "and time field shall consist of 9 bytes., input value[" + bdir.getCaptureDateTime() + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance()
				.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
			message.append("<BR>Invalid Capture Device Technology Identifier for Finger Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance()
				.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
			message.append("<BR>Invalid Capture Device Vendor Identifier for Finger Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceVendorIdentifier()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidCaptureDeviceType(bdir.getCaptureDeviceVendorIdentifier(),
				bdir.getCaptureDeviceTypeIdentifier())) {
			message.append("<BR>Invalid Capture Device Type Identifier for Finger Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTypeIdentifier()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
			message.append("<BR>Invalid No Of Quality Blocks value for Finger Modality, input value["
					+ Integer.toHexString(bdir.getNoOfQualityBlocks()) + "]");
			isValid = false;
		}

		if (bdir.getNoOfQualityBlocks() > 0) {
			for (FingerQualityBlock qualityBlock : bdir.getQualityBlocks()) {
				if (!FingerISOStandardsValidator.getInstance().isValidQualityScore(qualityBlock.getQualityScore())) {
					message.append("<BR>Invalid Quality Score value for Finger Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityScore()) + "]");
					isValid = false;
				}

				if (!FingerISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Identifier for Finger Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmIdentifier()) + "]");
					isValid = false;
				}

				if (!FingerISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmVendorIdentifier(qualityBlock.getQualityAlgorithmVendorIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Vendor Identifier for Finger Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmVendorIdentifier()) + "]");
					isValid = false;
				}
			}
		}

		if (!FingerISOStandardsValidator.getInstance()
				.isValidNoOfCertificationBlocks(bdir.getNoOfCertificationBlocks())) {
			message.append("<BR>Invalid No Of Certification Blocks for Finger Modality, input value["
					+ Integer.toHexString(bdir.getNoOfCertificationBlocks()) + "]");
			isValid = false;
		}

		if (bdir.getNoOfCertificationBlocks() > 0) {
			for (FingerCertificationBlock fingerCertificationBlock : bdir.getCertificationBlocks()) {
				if (!FingerISOStandardsValidator.getInstance()
						.isValidCertificationAuthorityID(fingerCertificationBlock.getCertificationAuthorityID())) {
					message.append("<BR>Invalid Certification AuthorityID for Finger Modality, input value["
							+ Integer.toHexString(fingerCertificationBlock.getCertificationAuthorityID()) + "]");
					isValid = false;
				}

				if (!FingerISOStandardsValidator.getInstance().isValidCertificationSchemeIdentifier(
						fingerCertificationBlock.getCertificationSchemeIdentifier())) {
					message.append("<BR>Invalid Certification Scheme Identifier for Finger Modality, input value["
							+ Integer.toHexString(fingerCertificationBlock.getCertificationSchemeIdentifier()) + "]");
					isValid = false;
				}
			}
		}

		if (!FingerISOStandardsValidator.getInstance().isValidFingerPosition(purpose, bdir.getFingerPosition())) {
			message.append("<BR>Invalid Finger Position Value for Finger Modality, input value["
					+ Integer.toHexString(bdir.getFingerPosition()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidRepresentationsNo(bdir.getRepresentationNo())) {
			message.append(
					"<BR>Invalid Representations No Value for Finger Modality, allowed values[0x00 till 0x0F], input value["
							+ Integer.toHexString(bdir.getRepresentationNo()) + "]");
			isValid = false;
		}

		// Used to check the image based on PIXELS_PER_INCH or PIXELS_PER_CM
		int scaleUnitsType = bdir.getScaleUnits();
		if (!FingerISOStandardsValidator.getInstance().isValidScaleUnits(scaleUnitsType)) {
			message.append(
					"<BR>Invalid Scale Unit Type Value for Finger Modality, allowed values[0x01, 0x02], input value["
							+ Integer.toHexString(scaleUnitsType) + "]");
			isValid = false;
		}

		int scanSpatialSamplingRateHorizontal = bdir.getCaptureDeviceSpatialSamplingRateHorizontal();
		if (!FingerISOStandardsValidator.getInstance()
				.isValidScanSpatialSamplingRateHorizontal(scanSpatialSamplingRateHorizontal)) {
			message.append(
					"<BR>Invalid Device Scan Spatial Sampling Rate Horizontal for Finger Modality, allowed values[0x01F4(500), 0x03E8(10000)], input value["
							+ Integer.toHexString(scanSpatialSamplingRateHorizontal) + "]");
			isValid = false;
		}

		int scanSpatialSamplingRateVertical = bdir.getCaptureDeviceSpatialSamplingRateVertical();
		if (!FingerISOStandardsValidator.getInstance()
				.isValidScanSpatialSamplingRateVertical(scanSpatialSamplingRateVertical)) {
			validationResultDto.setDescription(
					"<BR>Invalid Device Scan Spatial Sampling Rate Vertical for Finger Modality, allowed values[0x01F4(500), 0x03E8(10000)], input value["
							+ Integer.toHexString(scanSpatialSamplingRateVertical) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageSpatialSamplingRateHorizontal(
				scanSpatialSamplingRateHorizontal, bdir.getImageSpatialSamplingRateHorizontal())) {
			message.append(
					"<BR>Invalid Image Spatial SamplingRate Horizontal for Finger Modality, allowed values[0x01F4(500), 0x03E8(10000)] And less than or equal to "
							+ Integer.toHexString(scanSpatialSamplingRateHorizontal) + ", input value["
							+ Integer.toHexString(bdir.getImageSpatialSamplingRateHorizontal()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageSpatialSamplingRateVertical(
				scanSpatialSamplingRateVertical, bdir.getImageSpatialSamplingRateVertical())) {
			message.append(
					"<BR>Invalid Device Scan Spatial Sampling Rate Vertical for Finger Modality, allowed values[0x01F4(500), 0x03E8(10000)] And less than or equal to "
							+ Integer.toHexString(scanSpatialSamplingRateVertical) + ", input value["
							+ Integer.toHexString(bdir.getImageSpatialSamplingRateVertical()) + "]");
			isValid = false;
		}

		byte[] inImageData = bdir.getImage();
		if (!FingerISOStandardsValidator.getInstance().isValidBitDepth(inImageData, bdir.getBitDepth())) {
			message.append(
					"<BR>Invalid Image Bit Depth Value for Finger Modality, allowed values[0x08(Grayscale)], input value["
							+ Integer.toHexString(bdir.getBitDepth()) + "]");
			isValid = false;
		}

		int compressionType = bdir.getCompressionType();
		if (!FingerISOStandardsValidator.getInstance().isValidImageCompressionType(purpose, compressionType)) {
			message.append(
					"<BR>Invalid Image Compression Type for Finger Modality, allowed values[JPEG_2000_LOSSY/WSQ(AUTH), JPEG_2000_LOSS_LESS(Registration)], input value[Purpose("
							+ purpose + "), CompressionType(" + Integer.toHexString(compressionType) + ")]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageImpressionType(bdir.getImpressionType())) {
			message.append(
					"<BR>Invalid Image Compression Type for Finger Modality, allowed values[JPEG_2000_LOSSY/WSQ(AUTH), JPEG_2000_LOSS_LESS(Registration)], input value[]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageHorizontalLineLength(purpose, inImageData,
				bdir.getLineLengthHorizontal())) {
			message.append(
					"<BR>Invalid Image Horizontal Line Length for Finger Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getLineLengthHorizontal()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageVerticalLineLength(purpose, inImageData,
				bdir.getLineLengthVertical())) {
			message.append(
					"<BR>Invalid Image Vertical Line Length for Finger Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getLineLengthVertical()) + "]");
			isValid = false;
		}

		if (!FingerISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
			message.append(
					"<BR>Invalid Image Data Length for Finger Modality, allowed values[0x00000001, 0xFFFFFFFF], input value["
							+ Long.toHexString(bdir.getImageLength()) + "]");
			isValid = false;
		}

		try {
			if (!FingerISOStandardsValidator.getInstance().isValidImageData(purpose, Modality.Finger, inImageData)) {
				message.append(
						"<BR>Invalid Image Data for Finger Modality, allowed values[JPEG_2000_LOSSY/WSQ(Auth), JPEG_2000_LOSS_LESS(Registration)]");
				isValid = false;
			}
		} catch (Exception e) {
			message.append(
					"<BR>Invalid Image Data for Finger Modality, allowed values[JPEG_2000_LOSSY/WSQ(Auth), JPEG_2000_LOSS_LESS(Registration)] "
							+ e.getLocalizedMessage());
			isValid = false;
		}

		bdir = null;

		if (!isValid) {
			validationResultDto.setDescription(message.toString());
			message = null;
			return validationResultDto;
		}

		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}

	private ValidationResultDto doIrisISOValidations(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		StringBuilder message = new StringBuilder("ISOStandardsValidator failure - " + "with Message - ");
		boolean isValid = true;

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.IRIS.getCode());
		requestDto.setVersion(ISO19794_6_2011);

		byte[] bioData = null;
		try {
			bioData = CommonUtil.decodeURLSafeBase64(bioValue);
			requestDto.setInputBytes(bioData);
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

		if (!IrisISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
			message.append("<BR>Invalid Format Identifier for Iris Modality, allowed values[0x49495200], input value["
					+ Long.toHexString(bdir.getFormatIdentifier()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
			message.append("<BR>Invalid Version Number for Iris Modality, allowed values[0x30323000], input value["
					+ Long.toHexString(bdir.getVersionNumber()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
				bdir.getRecordLength())) {
			message.append("<BR>Invalid Record Length for Iris Modality, input value["
					+ Integer.toHexString(bioData != null ? bioData.length : 0) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidNoOfRepresentations(bdir.getNoOfRepresentations())) {
			message.append("<BR>Invalid No Of Representations for Iris Modality, allowed values[0x0001], input value["
					+ Integer.toHexString(bdir.getNoOfRepresentations()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
			message.append("<BR>Invalid Certification Flag for Iris Modality, allowed values[0x00], input value["
					+ Integer.toHexString(bdir.getCertificationFlag()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidNoOfEyesRepresented(bdir.getNoOfEyesPresent())) {
			message.append("<BR>Invalid No Of Eyes Present for Iris Modality, allowed values[0x00, 0x01], input value["
					+ Integer.toHexString(bdir.getNoOfEyesPresent()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidRepresentationLength(bdir.getRepresentationsLength())) {
			message.append(
					"<BR>Invalid Representation Length for Iris Modality, allowed values between[0x00000035 And 0xFFFFFFEF], input value["
							+ Long.toHexString(bdir.getRecordLength()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
				bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(), bdir.getCaptureMinute(),
				bdir.getCaptureSecond(), bdir.getCaptureMilliSecond())) {
			message.append("<BR>Invalid CaptureDateTime for Iris Modality, The capture date and time field shall \r\n"
					+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
					+ "Universal Time (UTC). The capture date \r\n"
					+ "and time field shall consist of 9 bytes., input value[" + bdir.getCaptureDateTime() + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance()
				.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
			message.append("<BR>Invalid Capture Device Technology Identifier for Iris Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance()
				.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
			message.append("<BR>Invalid Capture Device Vendor Identifier for Iris Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceVendorIdentifier()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidCaptureDeviceType(bdir.getCaptureDeviceVendorIdentifier(),
				bdir.getCaptureDeviceTypeIdentifier())) {
			message.append("<BR>Invalid Capture Device Type Identifier for Iris Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTypeIdentifier()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
			message.append("<BR>Invalid No Of Quality Blocks value for Iris Modality, input value["
					+ Integer.toHexString(bdir.getNoOfQualityBlocks()) + "]");
			isValid = false;
		}

		if (bdir.getNoOfQualityBlocks() > 0) {
			for (IrisQualityBlock qualityBlock : bdir.getQualityBlocks()) {
				if (!IrisISOStandardsValidator.getInstance().isValidQualityScore(qualityBlock.getQualityScore())) {
					message.append("<BR>Invalid Quality Score value for Iris Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityScore()) + "]");
					isValid = false;
				}

				if (!IrisISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Identifier for Iris Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmIdentifier()) + "]");
					isValid = false;
				}

				if (!IrisISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmVendorIdentifier(qualityBlock.getQualityAlgorithmVendorIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Vendor Identifier for Iris Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmVendorIdentifier()) + "]");
					isValid = false;
				}
			}
		}

		if (!IrisISOStandardsValidator.getInstance().isValidNoOfRepresentation(bdir.getRepresentationNo())) {
			message.append("<BR>Invalid No Of Representation for Iris Modality, allowed values[0x01],input value["
					+ Integer.toHexString(bdir.getRepresentationNo()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidEyeLabel(purpose, bdir.getEyeLabel())) {
			message.append("<BR>Invalid Iris Eye Label Value for Iris Modality, input value["
					+ Integer.toHexString(bdir.getEyeLabel()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidImageType(purpose, bdir.getImageType())) {
			message.append("<BR>Invalid Image Type No Value Irisnger Modality, allowed values[0x03, 0x07], input value["
					+ Integer.toHexString(bdir.getImageType()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidImageFromat(bdir.getImageFormat())) {
			message.append("<BR>Invalid Image Format Value for Iris Modality, allowed values[0x0A], input value["
					+ Integer.toHexString(bdir.getImageFormat()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance()
				.isValidImageHorizontalOrientation(bdir.getHorizontalOrientation())) {
			message.append(
					"<BR>Invalid Image Horizontal Orientation for Iris Modality, allowed values[0x00, 0x01, 0x02], input value["
							+ Integer.toHexString(bdir.getHorizontalOrientation()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidImageVerticalOrientation(bdir.getVerticalOrientation())) {
			message.append(
					"<BR>Invalid Image Vertical Orientation for Iris Modality, allowed values[0x00, 0x01, 0x02], input value["
							+ Integer.toHexString(bdir.getVerticalOrientation()) + "]");
			isValid = false;
		}

		int compressionType = bdir.getCompressionType();
		if (!IrisISOStandardsValidator.getInstance().isValidImageCompressionType(purpose, compressionType)) {
			message.append(
					"<BR>Invalid Image Compression Type for Iris Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS(Registration)], input value[Purpose("
							+ purpose + "), CompressionType(" + Integer.toHexString(compressionType) + ")]");
			isValid = false;
		}

		byte[] inImageData = bdir.getImage();

		if (!IrisISOStandardsValidator.getInstance().isValidImageWidth(purpose, inImageData, bdir.getWidth())) {
			message.append(
					"<BR>Invalid Image Width Value for Iris Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getWidth()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidImageHeight(purpose, inImageData, bdir.getHeight())) {
			message.append(
					"<BR>Invalid Image Height Value for Iris Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getHeight()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidBitDepth(inImageData, bdir.getBitDepth())) {
			message.append(
					"<BR>Invalid Image Bit Depth Value for Iris Modality, allowed values[0x08(Grayscale)], input value["
							+ Integer.toHexString(bdir.getBitDepth()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidRange(bdir.getRange())) {
			message.append("<BR>Invalid Range Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
					+ Integer.toHexString(bdir.getRange()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidRollAngleOfEye(bdir.getRollAngleOfEye())) {
			message.append(
					"<BR>Invalid Roll Angle Of Eye Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getRollAngleOfEye()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidRollAngleUncertainty(bdir.getRollAngleUncertainty())) {
			message.append(
					"<BR>Invalid Roll Angle Uncertainty Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getRollAngleUncertainty()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterSmallestX(bdir.getIrisCenterSmallestX())) {
			message.append(
					"<BR>Invalid Iris Center Smallest X Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisCenterSmallestX()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterLargestX(bdir.getIrisCenterLargestX())) {
			message.append(
					"<BR>Invalid Iris Center Largest X Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisCenterLargestX()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterSmallestY(bdir.getIrisCenterSmallestY())) {
			message.append(
					"<BR>Invalid Iris Center Smallest Y Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisCenterSmallestY()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisCenterLargestY(bdir.getIrisCenterLargestY())) {
			message.append(
					"<BR>Invalid Iris Center Largest Y Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisCenterLargestY()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisDiameterSmallest(bdir.getIrisDiameterSmallest())) {
			message.append(
					"<BR>Invalid Iris Diameter Smallest Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisDiameterSmallest()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidIrisDiameterLargest(bdir.getIrisDiameterLargest())) {
			message.append(
					"<BR>Invalid Iris Diameter Largest Value for Iris Modality, allowed values[0x0000, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getIrisDiameterLargest()) + "]");
			isValid = false;
		}

		if (!IrisISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
			message.append(
					"<BR>Invalid Image Data Length for Iris Modality, allowed values[0x00000001, 0xFFFFFFFF], input value["
							+ Long.toHexString(bdir.getImageLength()) + "]");
			isValid = false;
		}

		try {
			if (!IrisISOStandardsValidator.getInstance().isValidImageData(purpose, Modality.Iris, inImageData)) {
				message.append(
						"<BR>Invalid Image Data for Iris Modality, allowed values[JPEG_2000_LOSSY(Auth), JPEG_2000_LOSS_LESS(Registration)]");
				isValid = false;
			}
		} catch (Exception e) {
			message.append(
					"<BR>Invalid Image Data for Iris Modality, allowed values[JPEG_2000_LOSSY(Auth), JPEG_2000_LOSS_LESS(Registration)] "
							+ e.getLocalizedMessage());
			isValid = false;
		}

		bdir = null;

		if (!isValid) {
			validationResultDto.setDescription(message.toString());
			message = null;
			return validationResultDto;
		}

		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}

	private ValidationResultDto doFaceISOValidations(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);

		StringBuilder message = new StringBuilder("ISOStandardsValidator failure - " + "with Message - ");
		boolean isValid = true;

		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality(DeviceTypes.FACE.getCode());
		requestDto.setVersion(ISO19794_5_2011);

		byte[] bioData = null;
		try {
			bioData = CommonUtil.decodeURLSafeBase64(bioValue);
			requestDto.setInputBytes(bioData);
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

		if (!FaceISOStandardsValidator.getInstance().isValidFormatIdentifier(bdir.getFormatIdentifier())) {
			message.append("<BR>Invalid Format Identifier for Face Modality, allowed values[0x46414300], input value["
					+ Long.toHexString(bdir.getFormatIdentifier()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidVersionNumber(bdir.getVersionNumber())) {
			message.append("<BR>Invalid Version Number for Face Modality, allowed values[0x30333000], input value["
					+ Long.toHexString(bdir.getVersionNumber()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidRecordLength(bioData != null ? bioData.length : 0,
				bdir.getRecordLength())) {
			message.append("<BR>Invalid Record Length for Face Modality, input value["
					+ Integer.toHexString(bioData != null ? bioData.length : 0) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidNoOfRepresentations(bdir.getNoOfRepresentations())) {
			message.append("<BR>Invalid No Of Representations for Face Modality, allowed values[0x0001], input value["
					+ Integer.toHexString(bdir.getNoOfRepresentations()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidCertificationFlag(bdir.getCertificationFlag())) {
			message.append("<BR>Invalid Certification Flag for Face Modality, allowed values[0x00], input value["
					+ Integer.toHexString(bdir.getCertificationFlag()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidTemporalSemantics(bdir.getTemporalSemantics())) {
			message.append("<BR>Invalid Certification Flag for Face Modality, allowed values[0x0000], input value["
					+ Integer.toHexString(bdir.getTemporalSemantics()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidRepresentationLength(bdir.getRecordLength())) {
			message.append("<BR>Invalid Representation Length for Face Modality, allowed values between[0x00000033 And 0xFFFFFFEF], input value["
					+ Long.toHexString(bdir.getRecordLength()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidCaptureDateTime(bdir.getCaptureYear(),
				bdir.getCaptureMonth(), bdir.getCaptureDay(), bdir.getCaptureHour(), bdir.getCaptureMinute(),
				bdir.getCaptureSecond(), bdir.getCaptureMilliSecond())) {
			message.append("<BR>Invalid CaptureDateTime for Face Modality, The capture date and time field shall \r\n"
					+ "indicate when the capture of this \r\n" + "representation stated in Coordinated \r\n"
					+ "Universal Time (UTC). The capture date \r\n"
					+ "and time field shall consist of 9 bytes., input value[" + bdir.getCaptureDateTime() + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance()
				.isValidCaptureDeviceTechnologyIdentifier(bdir.getCaptureDeviceTechnologyIdentifier())) {
			message.append("<BR>Invalid Capture Device Technology Identifier for Face Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTechnologyIdentifier()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance()
				.isValidCaptureDeviceVendor(bdir.getCaptureDeviceVendorIdentifier())) {
			message.append("<BR>Invalid Capture Device Vendor Identifier for Face Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceVendorIdentifier()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidCaptureDeviceType(bdir.getCaptureDeviceVendorIdentifier(),
				bdir.getCaptureDeviceTypeIdentifier())) {
			message.append("<BR>Invalid Capture Device Type Identifier for Face Modality, input value["
					+ Integer.toHexString(bdir.getCaptureDeviceTypeIdentifier()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidNoOfQualityBlocks(bdir.getNoOfQualityBlocks())) {
			message.append("<BR>Invalid No Of Quality Blocks value for Face Modality, input value["
					+ Integer.toHexString(bdir.getNoOfQualityBlocks()) + "]");
			isValid = false;
		}

		if (bdir.getNoOfQualityBlocks() > 0) {
			for (FaceQualityBlock qualityBlock : bdir.getQualityBlocks()) {
				if (!FaceISOStandardsValidator.getInstance().isValidQualityScore(qualityBlock.getQualityScore())) {
					message.append("<BR>Invalid Quality Score value for Face Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityScore()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmIdentifier(qualityBlock.getQualityAlgorithmIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Identifier for Face Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmIdentifier()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidQualityAlgorithmVendorIdentifier(qualityBlock.getQualityAlgorithmVendorIdentifier())) {
					message.append("<BR>Invalid Quality Algorithm Vendor Identifier for Face Modality, input value["
							+ Integer.toHexString(qualityBlock.getQualityAlgorithmVendorIdentifier()) + "]");
					isValid = false;
				}
			}
		}

		if (!FaceISOStandardsValidator.getInstance().isValidNoOfLandmarkPoints(bdir.getNoOfLandMarkPoints())) {
			message.append("<BR>Invalid No Of Landmark Points for Face Modality, allowed values[0x0000, 0xFFFF],input value["
					+ Integer.toHexString(bdir.getNoOfLandMarkPoints()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidGender(bdir.getGender())) {
			message.append("<BR>Invalid Gender value for Face Modality, allowed values[0x00, 0x01, 0x02, 0xFF],input value["
					+ Integer.toHexString(bdir.getGender()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidEyeColour(bdir.getEyeColor())) {
			message.append("<BR>Invalid Eye Colour value for Face Modality, allowed values[0x00 till 0x07 or 0xFF], input value["
					+ Integer.toHexString(bdir.getEyeColor()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidHairColour(bdir.getHairColor())) {
			message.append("<BR>Invalid Hair Colour Value for Face Modality, allowed values[0x00 till 0x07 or 0xFF], input value["
					+ Integer.toHexString(bdir.getHairColor()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidSubjectHeight(bdir.getSubjectHeight())) {
			message.append("<BR>Invalid Subject Height Value for Face Modality, allowed values[0x00 till 0xFF], input value["
					+ Integer.toHexString(bdir.getSubjectHeight()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidFeatureMask(bdir.getFeaturesMask())) {
			message.append("<BR>Invalid Features Mask Value for Face Modality, allowed values[0x000000 till 0xFFFFFF]input value["
					+ Integer.toHexString(bdir.getFeaturesMask()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidFeatureMask(bdir.getExpressionMask())) {
			message.append("<BR>Invalid Expression Mask Value for Face Modality, allowed values[0x000000 till 0xFFFFFF], input value["
					+ Integer.toHexString(bdir.getExpressionMask()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidPoseAngle(bdir.getPoseAngle())) {
			message.append("<BR>Invalid Pose Angle Value for Face Modality");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidPoseAngleUncertainty(bdir.getPoseAngleUncertainty())) {
			message.append("<BR>Invalid Pose Angle Uncertainty Value for Face Modality");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidPoseAngleUncertainty(bdir.getPoseAngleUncertainty())) {
			message.append("<BR>Invalid Pose Angle Uncertainty Value for Face Modality");
			isValid = false;
		}

		//Future Implemntation
		if (bdir.getNoOfLandMarkPoints() > 0)
		{
			for (LandmarkPoints landmarkPoints : bdir.getLandmarkPoints()) {
				if (!FaceISOStandardsValidator.getInstance()
						.isValidLandmarkPointType(landmarkPoints.getLandmarkPointType())) {
					message.append("<BR>Invalid Landmark Point Type for Face Modality, input value["
							+ Integer.toHexString(landmarkPoints.getLandmarkPointType()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidLandmarkPointCode(landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode())) {
					message.append("<BR>Invalid Landmark Point Code for Face Modality, input value["
							+ Integer.toHexString(landmarkPoints.getLandmarkPointCode()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidLandmarkXCooridinate(landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(), landmarkPoints.getXCoordinate())) {
					message.append("<BR>Invalid Landmark X Cooridinate for Face Modality, input value["
							+ Integer.toHexString(landmarkPoints.getXCoordinate()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidLandmarkYCooridinate(landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(), landmarkPoints.getYCoordinate())) {
					message.append("<BR>Invalid Landmark Y Cooridinate for Face Modality, input value["
							+ Integer.toHexString(landmarkPoints.getYCoordinate()) + "]");
					isValid = false;
				}

				if (!FaceISOStandardsValidator.getInstance()
						.isValidLandmarkZCooridinate(landmarkPoints.getLandmarkPointType(), landmarkPoints.getLandmarkPointCode(), landmarkPoints.getZCoordinate())) {
					message.append("<BR>Invalid Landmark Z Cooridinate for Face Modality, input value["
							+ Integer.toHexString(landmarkPoints.getZCoordinate()) + "]");
					isValid = false;
				}
			}
		}
		
		if (!FaceISOStandardsValidator.getInstance().isValidFaceImageType(bdir.getFaceImageType())) {
			message.append("<BR>Invalid Face Image Type Value for Face Modality, allowed values[0x00 till 0x03 And 0x80 till 0x82 ], input value["
					+ Integer.toHexString(bdir.getFaceImageType()) + "]");
			isValid = false;
		}

		int compressionType = bdir.getImageDataType();
		if (!FaceISOStandardsValidator.getInstance().isValidImageCompressionType(purpose, compressionType)) {
			message.append(
					"<BR>Invalid Image Compression Type for Face Modality, allowed values[0x01(Auth), 0x02(Registration)], input value[Purpose("
							+ purpose + "), CompressionType(" + Integer.toHexString(compressionType) + ")]");
			isValid = false;
		}

		byte[] inImageData = bdir.getImage();

		if (!FaceISOStandardsValidator.getInstance().isValidImageWidth(purpose, inImageData, bdir.getWidth())) {
			message.append(
					"<BR>Invalid Image Width Value for Face Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getWidth()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidImageHeight(purpose, inImageData, bdir.getHeight())) {
			message.append(
					"<BR>Invalid Image Height Value for Face Modality, allowed values[0x0001, 0xFFFF], input value["
							+ Integer.toHexString(bdir.getHeight()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidSpatialSamplingRateLevel(bdir.getSpatialSamplingRateLevel())) {
			message.append(
					"<BR>Invalid Spatial Sampling Rate Level Value for Face Modality, allowed values[0x00 till 0x07], input value["
							+ Integer.toHexString(bdir.getSpatialSamplingRateLevel()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidPostAcquisitionProcessing(bdir.getPostAcquistionProcessing())) {
			message.append(
					"<BR>Invalid Post Acquisition Processing Value for Face Modality, allowed values[0x0000 till 0xFFFF], input value["
							+ Integer.toHexString(bdir.getPostAcquistionProcessing()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidCrossReference(bdir.getCrossReference())) {
			message.append("<BR>Invalid Cross Reference  Value for Face Modality, allowed values[0x00, 0xFF], input value["
					+ Integer.toHexString(bdir.getCrossReference()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidImageColourSpace(purpose, inImageData, bdir.getImageColorSpace())) {
			message.append(
					"<BR>Invalid Image Bit Depth Value for Face Modality, allowed values[0x01(BIT_24_RGB)], input value["
							+ Integer.toHexString(bdir.getImageColorSpace()) + "]");
			isValid = false;
		}

		if (!FaceISOStandardsValidator.getInstance().isValidImageDataLength(inImageData, bdir.getImageLength())) {
			message.append(
					"<BR>Invalid Image Data Length for Face Modality, allowed values[0x00000001, 0xFFFFFFFF], input value["
							+ Long.toHexString(bdir.getImageLength()) + "]");
			isValid = false;
		}

		try {
			if (!FaceISOStandardsValidator.getInstance().isValidImageData(purpose, Modality.Face, inImageData)) {
				message.append(
						"<BR>Invalid Image Data for Face Modality, allowed values[JPEG_2000_LOSSY(Auth), JPEG_2000_LOSS_LESS(Registration)]");
				isValid = false;
			}
		} catch (Exception e) {
			message.append(
					"<BR>Invalid Image Data for Face Modality, allowed values[JPEG_2000_LOSSY(Auth), JPEG_2000_LOSS_LESS(Registration)] "
							+ e.getLocalizedMessage());
			isValid = false;
		}

		bdir = null;

		if (!isValid) {
			validationResultDto.setDescription(message.toString());
			message = null;
			return validationResultDto;
		}

		validationResultDto.setStatus(AppConstants.SUCCESS);
		validationResultDto.setDescription("ISO Standards Validation of response 'biovalue' is successful");
		return validationResultDto;
	}
}
