package io.mosip.compliance.toolkit.validators;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.jnbis.api.model.Bitmap;
import org.jnbis.internal.WsqDecoder;

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
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;

public abstract class BioUtilValidator extends SBIValidator {
    
    private static final String ISO19794_5_2011 = "ISO19794_5_2011";
    private static final String ISO19794_6_2011 = "ISO19794_6_2011";
    private static final String ISO19794_4_2011 = "ISO19794_4_2011";
    public static final String KEY_SPLITTER = "#KEY_SPLITTER#";

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
                    validationResultDto.setDescription("BioUtilValidator failure - " + "with Message - "
                            + " invalid bioType = " + bioType);
                    break;
            }
        } else {
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

        FingerImageCompressionType compressionType = bdir.getRepresentation().getRepresentationHeader().getCompressionType();        
        if (!(compressionType == FingerImageCompressionType.JPEG_2000_LOSS_LESS || compressionType == FingerImageCompressionType.JPEG_2000_LOSSY || compressionType == FingerImageCompressionType.WSQ))
        {
            errorCode = ToolkitErrorCodes.INVALID_FINGER_COMPRESSION_TYPE;
            throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
        }
        
        byte[] inImageData = bdir.getRepresentation().getRepresentationBody().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
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

        IrisImageCompressionType compressionType = bdir.getRepresentation().getRepresentationHeader().getImageInformation().getCompressionType();        
        if (!(compressionType == IrisImageCompressionType.JPEG_LOSSLESS_OR_NONE || compressionType == IrisImageCompressionType.JPEG_LOSSY))
        {
            errorCode = ToolkitErrorCodes.INVALID_IRIS_COMPRESSION_TYPE;
            throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
        }

        byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
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

        ImageDataType compressionType = bdir.getRepresentation().getRepresentationHeader().getImageInformation().getImageDataType();        
        if (!(compressionType == ImageDataType.JPEG2000_LOSSY || compressionType == ImageDataType.JPEG2000_LOSS_LESS))
        {
            errorCode = ToolkitErrorCodes.INVALID_FACE_COMPRESSION_TYPE;
            throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
        }

        byte[] inImageData = bdir.getRepresentation().getRepresentationData().getImageData().getImage();
        validationResultDto = isValidImageType(purpose, inImageData);
        return validationResultDto;
    }

    protected ValidationResultDto isValidImageType(String purpose, byte[] inImageData) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.FAILURE);
        switch (Purposes.fromCode(purpose)) {
            case AUTH:
                try {
                    if (isJP2000(inImageData) || isWSQ(inImageData)) {
                        validationResultDto.setStatus(AppConstants.SUCCESS);
                        validationResultDto.setDescription(
                                "Validation of response 'biovalue' as a valid ISO and image type is successful");
                    } else {
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
                    if (isJP2000(inImageData)) {
                        validationResultDto.setStatus(AppConstants.SUCCESS);
                        validationResultDto.setDescription(
                                "Validation of response 'biovalue' as a valid ISO and image type is successful");
                    } else {
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

    /**
     * Some Extra info about other file format with jpeg: initial of file contains
     * these bytes
     * BMP : 42 4D
     * JPG : FF D8 FF EO ( Starting 2 Byte will always be same)
     * PNG : 89 50 4E 47
     * GIF : 47 49 46 38
     * When a JPG file uses JFIF or EXIF, The signature is different :
     * Raw : FF D8 FF DB
     * JFIF : FF D8 FF E0
     * EXIF : FF D8 FF E1
     * WSQ : check with marker from WsqDecoder
     * JP2000: 6a 70 32 68 // JP2 Header
     */
    protected static Boolean isJP2000(byte[] imageData) throws Exception {
        boolean isValid = false;
        DataInputStream ins = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(imageData)));
        try {
            while (true) {
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

    protected static Boolean isWSQ(byte[] imageData) throws Exception {

        try {
            WsqDecoder decoder = new WsqDecoder();
            Bitmap bitmap = decoder.decode(imageData);
            if (bitmap != null && bitmap.getPixels() != null && bitmap.getPixels().length > 0) {
                return true;
            } else {
                return false;

            }
        } finally {
        }
    }
}
