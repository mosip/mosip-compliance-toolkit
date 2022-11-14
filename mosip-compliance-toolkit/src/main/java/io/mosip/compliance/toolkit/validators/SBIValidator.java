package io.mosip.compliance.toolkit.validators;

import static io.restassured.RestAssured.given;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.jnbis.api.model.Bitmap;
import org.jnbis.internal.WsqDecoder;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.restassured.http.Cookie;
import lombok.Data;

public abstract class SBIValidator extends ToolkitValidator {

    protected static final String TRUST_FOR_DIGITAL_ID = "Digital Id";
    protected static final String TRUST_FOR_DEVICE_INFO = "Device Info";
    protected static final String TRUST_FOR_BIOMETRIC_INFO = "Biometric Data";

    protected static final String DIGITAL_ID = "digitalId";
    protected static final String DEVICE_SUB_TYPE = "deviceSubType";
    protected static final String DEVICE_TYPE = "type";
    protected static final String CERTIFICATION_TYPE = "certificationType";
    protected static final String BIOMETRICS = "biometrics";
    protected static final String DATA = "data";
    protected static final String DEVICE_INFO = "deviceInfo";
    protected static final String DEVICE_STATUS = "deviceStatus";
    private static final String ALG = "alg";
    private static final String X5C = "x5c";

    @Value("${mosip.service.auth.appid}")
    protected String getAuthAppId;

    @Value("${mosip.service.auth.clientid}")
    protected String getAuthClientId;

    @Value("${mosip.service.auth.secretkey}")
    protected String getAuthSecretKey;

    @Value("${mosip.service.authmanager.url}")
    protected String getAuthManagerUrl;

    private static String AUTH_REQ_TEMPLATE = "{ \"id\": \"string\",\"metadata\": {},\"request\": { \"appId\": \"%s\", \"clientId\": \"%s\", \"secretKey\": \"%s\" }, \"requesttime\": \"%s\", \"version\": \"string\"}";

    private final String END_CERTIFICATE = "\n-----END CERTIFICATE-----\n";
    private final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n";

    protected boolean validateMethodName(String methodName) throws Exception {
        MethodName.fromCode(methodName);
        return true;
    }

    protected ValidationResultDto checkIfJWTSignatureIsValid(String jwtInfo) {
        ValidationResultDto validationResultDto = new ValidationResultDto();
        try {
            JsonWebSignature jws = new JsonWebSignature();
            jws.setCompactSerialization(jwtInfo);
            List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
            X509Certificate certificate = certificateChainHeaderValue.get(0);
            certificate.checkValidity();
            PublicKey publicKey = certificate.getPublicKey();
            jws.setKey(publicKey);
            jws.getLeafCertificateHeaderValue().checkValidity();
            boolean verified = jws.verifySignature();
            if (verified) {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("JWT Signature validation is successful");
            } else {
                validationResultDto.setStatus(AppConstants.SUCCESS);
                validationResultDto.setDescription("JWT Signature validation failed");
            }
        } catch (CertificateExpiredException e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto
                    .setDescription(" CertificateExpiredException - " + "with Message - " + e.getLocalizedMessage());
        } catch (CertificateNotYetValidException e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(
                    " CertificateNotYetValidException - " + "with Message - " + e.getLocalizedMessage());
        } catch (JoseException e) {
            validationResultDto.setStatus(AppConstants.FAILURE);
            validationResultDto.setDescription(" JoseException - " + "with Message - " + e.getLocalizedMessage());
        }
        return validationResultDto;
    }

    protected String getCertificate(String jwtInfo) throws JoseException, IOException {
        String certificate = null;
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwtInfo);
        String encodedHeader = jws.getHeaders().getEncodedHeader();
        String jsonHeader = StringUtil.toUtf8String(StringUtil.base64UrlDecode(encodedHeader));
        ObjectNode headerNode = (ObjectNode) objectMapperConfig.objectMapper().readValue(jsonHeader, ObjectNode.class);
        String algType = headerNode.get(ALG).asText();
        if (algType.equals(AppConstants.RS256_ALGORITHM_TYPE)) {
            ArrayNode arrCertificates = (ArrayNode) headerNode.get(X5C);
            certificate = arrCertificates.get(0).asText();
        }
        return certificate;
    }

    protected String getPayload(String jwtInfo) throws JoseException, IOException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setCompactSerialization(jwtInfo);
        String payload = jws.getEncodedPayload();
        return StringUtil.toUtf8String(StringUtil.base64UrlDecode(payload));
    }

    protected boolean isDeviceInfoUnSigned(ObjectNode objectNode) {
        try {
            ObjectNode deviceInfo = getUnsignedDeviceInfo(objectNode.get(DEVICE_INFO).asText());
            if (!Objects.isNull(deviceInfo)) {
                return true;
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        return false;
    }

    protected ObjectNode getUnsignedDeviceInfo(String deviceInfoResponse)
            throws JsonParseException, JsonMappingException, IOException {
        String deviceInfo = StringUtil.toUtf8String(StringUtil.base64UrlDecode(deviceInfoResponse));
        return objectMapperConfig.objectMapper().readValue(deviceInfo, ObjectNode.class);
    }

    protected io.restassured.response.Response getPostResponse(String postUrl, DeviceValidatorDto deviceValidatorDto)
            throws IOException {
        Cookie.Builder builder = new Cookie.Builder("Authorization",
                getAuthToken(getAuthManagerUrl, getAuthAppId, getAuthClientId, getAuthSecretKey));

        return given().cookie(builder.build()).relaxedHTTPSValidation().body(deviceValidatorDto)
                .contentType("application/json").log().all().when().post(postUrl).then().log().all().extract()
                .response();
    }

    protected String getAuthToken(String authUrl, String appId, String clientId, String secretKey) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String requestBody = String.format(AUTH_REQ_TEMPLATE, appId, clientId, secretKey,
                DateUtils.getUTCCurrentDateTime());

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, requestBody);
        Request request = new Request.Builder().url(authUrl).post(body).build();

        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.header("authorization");
        }
        return "";
    }

    protected String getCertificateData(String certificateInfo) {
        return BEGIN_CERTIFICATE + certificateInfo + END_CERTIFICATE;
    }

    protected String getCurrentDateAndTimeForAPI() {
        String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATEFORMAT);
        LocalDateTime time = LocalDateTime.now(ZoneOffset.UTC);
        String currentTime = time.format(dateFormat);
        return currentTime;
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

    @Data
    protected static class DeviceTrustRequestDto implements Serializable {
        private static final long serialVersionUID = -4874932813550831900L;
        String certificateData;
        String partnerDomain;
    }

    @Data
    protected static class DeviceValidatorDto implements Serializable {
        private static final long serialVersionUID = 6604417847897263692L;
        String id;
        Object metadata;
        Object request;
        String requesttime;
        String version;
    }

    @Data
    protected static class DeviceValidatorResponseDto implements Serializable {
        private static final long serialVersionUID = -3533369757932860828L;
        String id;
        Object metadata;
        DeviceValidatorResponse response;
        String responsetime;
        String version;
        List<ErrorDto> errors;
    }

    @Data
    protected static class ErrorDto {
        String errorCode;
        String message;
    }

    @Data
    protected static class DeviceValidatorResponse implements Serializable {
        private static final long serialVersionUID = -2714540550294089151L;
        String status;
    }

    @Data
    protected static class DeviceValidatorRequestDto implements Serializable {
        private static final long serialVersionUID = 6066767653045843567L;
        String deviceCode;
        String deviceServiceVersion;
        DeviceValidatorDigitalIdDto digitalId;
        String purpose;
        String timeStamp;
    }

    @Data
    protected static class DeviceValidatorDigitalIdDto implements Serializable {
        private static final long serialVersionUID = 5438972186954170612L;
        String dateTime;
        String deviceSubType;
        String dp;
        String dpId;
        String make;
        String model;
        String serialNo;
        String type;
    }

}
