package io.mosip.compliance.toolkit.validators;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.jnbis.api.model.Bitmap;
import org.jnbis.internal.WsqDecoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;
import io.mosip.biometrics.util.face.FaceBDIR;
import io.mosip.biometrics.util.face.FaceDecoder;
import io.mosip.biometrics.util.finger.FingerBDIR;
import io.mosip.biometrics.util.finger.FingerDecoder;
import io.mosip.biometrics.util.iris.IrisBDIR;
import io.mosip.biometrics.util.iris.IrisDecoder;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

@Component
public class RegistrationBioUtilValidator extends SBIValidator {
	public static final String BIOMETRICS = "biometrics";
	public static final String BIO = "bio";
	public static final String DECODED_DATA = "dataDecoded";
	public static final String BIO_VALUE = "bioValue";
	public static final String PURPOSE = "purpose";
	public static final String BIO_TYPE = "bioType";
	public static final String BIO_SUBTYPE = "bioSubType";

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
					String bioSubType = dataNode.get(BIO_SUBTYPE).asText();
					String bioValue = null;
					switch (Purposes.fromCode(purpose))
					{
						case REGISTRATION:
							bioValue = dataNode.get(BIO_VALUE).asText();
							break;
						case AUTH:
							throw new ToolkitException (ToolkitErrorCodes.INVALID_PURPOSE.getErrorCode(), ToolkitErrorCodes.INVALID_PURPOSE.getErrorMessage());
							//bioValue = decrypt (dataNode.get(BIO_VALUE).asText());
							//break;
					}
					
					validationResultDto = isValidISOTemplate(purpose, bioType, bioSubType, bioValue);
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
					{
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

	private String decrypt(String asText) {
		// TODO Auto-generated method stub
		return null;
	}

	private ValidationResultDto isValidISOTemplate(String purpose, String bioType, String bioSubType, String bioValue) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		if (bioValue != null)
		{
			switch (bioType)
			{
				case "Finger":
					validationResultDto = isValidFingerISOTemplate(purpose, bioValue);
					break;
				case "Iris":
					validationResultDto = isValidIrisISOTemplate(purpose, bioValue);
					break;
				case "Face":
					validationResultDto = isValidFaceISOTemplate(purpose, bioValue);
					break;
				default:
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
							+ " invalid bioType = " + bioType);
					break;
			}
		}
		else
		{
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
					+ " isValidISOTemplate BioValue is Empty or Null");
		}
		return validationResultDto;
	}
	
	private ValidationResultDto isValidFingerISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);
		
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Finger");
		requestDto.setVersion("ISO19794_4_2011");

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64 (bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}
		
		FingerBDIR bdir;
		try {
			bdir = FingerDecoder.getFingerBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FINGER_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		byte [] inImageData = bdir.getRepresentation().getRepresentationBody().getImageData().getImage();
		validationResultDto = isValidImageType(purpose, inImageData);
		return validationResultDto;
	}
	
	private ValidationResultDto isValidIrisISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);
		
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Iris");
		requestDto.setVersion("ISO19794_6_2011");

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64 (bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}
		
		IrisBDIR bdir;
		try {
			bdir = IrisDecoder.getIrisBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_IRIS_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		byte [] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
		validationResultDto = isValidImageType(purpose, inImageData);
		return validationResultDto;
	}

	private ValidationResultDto isValidFaceISOTemplate(String purpose, String bioValue) {
		ToolkitErrorCodes errorCode = null;
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);
		
		ConvertRequestDto requestDto = new ConvertRequestDto();
		requestDto.setModality("Face");
		requestDto.setVersion("ISO19794_5_2011");

		try {
			requestDto.setInputBytes(CommonUtil.decodeURLSafeBase64 (bioValue));
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}
		
		FaceBDIR bdir;
		try {
			bdir = FaceDecoder.getFaceBDIR(requestDto);
		} catch (Exception e) {
			errorCode = ToolkitErrorCodes.SOURCE_NOT_VALID_FACE_ISO_FORMAT_EXCEPTION;
			throw new ToolkitException (errorCode.getErrorCode(), e.getLocalizedMessage());
		}

		byte [] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
		validationResultDto = isValidImageType(purpose, inImageData);
		return validationResultDto;
	}

	private ValidationResultDto isValidImageType(String purpose, byte[]inImageData)
	{
		ValidationResultDto validationResultDto = new ValidationResultDto();
		validationResultDto.setStatus(AppConstants.FAILURE);
		switch(Purposes.fromCode(purpose))
		{
			case AUTH:
				try {
					if (isJP2000(inImageData) || isWSQ(inImageData))
					{
						validationResultDto.setStatus(AppConstants.SUCCESS);
						validationResultDto.setDescription("Bio value validation is valid");
					}
					else
					{
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
								+ " isValidISOTemplate is not valid for image type JP2000 and WSQ");
					}
				} catch (Exception e) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
							+ " isValidISOTemplate is not valid " + e.getLocalizedMessage());
				}
				break;
			case REGISTRATION:
			try {
				if (isJP2000(inImageData))
				{
					validationResultDto.setStatus(AppConstants.SUCCESS);
					validationResultDto.setDescription("Bio value validation is valid");
				}
				else
				{
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
							+ " isValidISOTemplate is not valid for image type JP2000 and WSQ");
				}
			} catch (Exception e) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
						+ " isValidISOTemplate is not valid " + e.getLocalizedMessage());
			}
				break;
		}
		return validationResultDto;
	}
	
	/*
	*	Some Extra info about other file format with jpeg: initial of file contains these bytes
	*	BMP : 42 4D
	*	JPG : FF D8 FF EO ( Starting 2 Byte will always be same)
	* 	PNG : 89 50 4E 47
	* 	GIF : 47 49 46 38
	*  	When a JPG file uses JFIF or EXIF, The signature is different :
	*	Raw  : FF D8 FF DB
	*	JFIF : FF D8 FF E0
	*	EXIF : FF D8 FF E1
	*	WSQ  : check with marker from WsqDecoder
	*	JP2000: 6a 70 32 68 // JP2 Header 
 	*/
	public static Boolean isJP2000(byte[] imageData) throws Exception {
		boolean isValid = false;
		DataInputStream ins = new DataInputStream(new BufferedInputStream (new ByteArrayInputStream (imageData)));
		try {
			while (true)
			{
				if (ins.readInt() == 0x6a703268) {
					isValid = true;
					break;
				}	
			}
		} finally {
			ins.close();
		}
		return isValid;
	}

	public static Boolean isWSQ(byte[] imageData) throws Exception {
		
		try {
			WsqDecoder decoder = new WsqDecoder ();
			Bitmap bitmap = decoder.decode(imageData);
			if (bitmap != null && bitmap.getPixels() != null  && bitmap.getPixels().length > 0) {
				return true;
			} else {
				return false;

			}
		} finally {
		}
	}
}
