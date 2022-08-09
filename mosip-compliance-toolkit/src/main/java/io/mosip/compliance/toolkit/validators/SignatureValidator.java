package io.mosip.compliance.toolkit.validators;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.CertificationTypes;
import io.mosip.compliance.toolkit.constants.DeviceStatus;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.restassured.http.Cookie;
import lombok.Data;
import io.mosip.compliance.toolkit.dto.sbi.DeviceInfoDto;
import io.mosip.compliance.toolkit.dto.sbi.DeviceInfoResponseDto;
import io.mosip.compliance.toolkit.dto.sbi.DigitalIdDto;
import io.mosip.compliance.toolkit.dto.sbi.DiscoverResponseDto;

@Component
public class SignatureValidator extends ToolkitValidator {

	@Autowired
	TrustValidatorHelper trustValidatorHelper;

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			if (validateMethodName(responseDto.getMethodName())) {
				if (Objects.nonNull(responseDto.getMethodResponse())) {
					switch (MethodName.fromCode(responseDto.getMethodName())) {
					case INFO:
						validationResultDto = validateDeviceInfo(responseDto);
						break;
					case DEVICE:
						validationResultDto = validateDiscoveryInfo(responseDto);
						break;
					case CAPTURE:
						validationResultDto = validateCaptureInfo(responseDto);
						break;
					case RCAPTURE:
						validationResultDto = validateRCaptureInfo(responseDto);
						break;
					default:
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Method not supported");
						break;
					}
				} else {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Response is empty");
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

	private ValidationResultDto validateDiscoveryInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			// digitalId - Digital ID as per the Digital ID definition but it will not be
			// signed has base64urlencoded data
			DiscoverResponseDto discoverResponse = objectMapper.readValue(responseDto.getMethodResponse(),
					DiscoverResponseDto.class);
			if (Objects.isNull(discoverResponse)) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Discover info value is null");
			} else {
				if (Objects.isNull(discoverResponse.getDigitalId())) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Discover info digitalId is null");
				} else {
					String digitalId = StringUtil
							.toUtf8String(StringUtil.base64UrlDecode(discoverResponse.getDigitalId()));
					validationResultDto = validateUnsignedDigitalID(digitalId);
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateDeviceInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			DeviceInfoResponseDto[] arrDeviceInfoResponseDto = objectMapper.readValue(responseDto.getMethodResponse(),
					DeviceInfoResponseDto[].class);
			if (Objects.isNull(arrDeviceInfoResponseDto) || arrDeviceInfoResponseDto.length == 0) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Device info Decoded value is null");
			} else {
				for (DeviceInfoResponseDto deviceInfoResponseDto : arrDeviceInfoResponseDto) {
					if (isUnSignedDeviceInfo(deviceInfoResponseDto)) {
						validationResultDto = validateUnSignedDeviceInfo(deviceInfoResponseDto);
						if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)){
							DeviceInfoDto deviceInfoDto = getUnsignedDeviceInfoDto(deviceInfoResponseDto.getDeviceInfo());
							validationResultDto = validateUnsignedDigitalID(deviceInfoDto.getDigitalId());
							if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
								break;
						}
					} else {
						validationResultDto = validateSignedDeviceInfo(deviceInfoResponseDto);
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private boolean isUnSignedDeviceInfo(DeviceInfoResponseDto deviceInfoResponseDto) {
		String deviceInfoResponse = deviceInfoResponseDto.getDeviceInfo();
		try {
			if (!Objects.isNull(deviceInfoResponse)) {
				String deviceInfo = StringUtil.toUtf8String(StringUtil.base64UrlDecode(deviceInfoResponse));
				DeviceInfoDto deviceInfoDto = objectMapper.readValue(deviceInfo, DeviceInfoDto.class);
				if (!Objects.isNull(deviceInfoDto)) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	private ValidationResultDto validateUnSignedDeviceInfo(DeviceInfoResponseDto deviceInfoResponseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String deviceInfoResponse = deviceInfoResponseDto.getDeviceInfo();
			if (!Objects.isNull(deviceInfoResponse)) {
				DeviceInfoDto deviceInfoDto = getUnsignedDeviceInfoDto(deviceInfoResponse);
				if (!Objects.isNull(deviceInfoDto)) {
					DeviceStatus deviceStatus = DeviceStatus.fromCode(deviceInfoDto.getDeviceStatus());
					if (deviceStatus == DeviceStatus.NOT_REGISTERED) {
						validationResultDto.setStatus(AppConstants.SUCCESS);
						validationResultDto.setDescription("Device is not registered");
					} else {
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("UnSignedDeviceInfo validation failure");
					}
				} else {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("DeviceInfo can not be null");
				}
			}
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("UnSignedDeviceInfo - " + "with Message - " + e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"UnSignedDeviceInfo validation failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private DeviceInfoDto getUnsignedDeviceInfoDto(String deviceInfoResponse)
			throws JsonParseException, JsonMappingException, IOException {
		String deviceInfo = StringUtil.toUtf8String(StringUtil.base64UrlDecode(deviceInfoResponse));
		return objectMapper.readValue(deviceInfo, DeviceInfoDto.class);
	}

	private ValidationResultDto validateSignedDeviceInfo(DeviceInfoResponseDto deviceInfoResponseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			validationResultDto = validateSignatureValidity(deviceInfoResponseDto.getDeviceInfo());
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				DeviceInfoDto deviceInfoDto = objectMapper
						.readValue(getJWTPayload(deviceInfoResponseDto.getDeviceInfo()), DeviceInfoDto.class);
				if (Objects.isNull(deviceInfoDto)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Device info Decoded value is null");
				} else {
					validationResultDto = validateSignedDigitalID(
							deviceInfoResponseDto.getDeviceInfoDecoded().getDigitalId(),
							deviceInfoResponseDto.getDeviceInfoDecoded().getCertification());

				}
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription(" validateSignedDeviceInfo - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateSignedDigitalID(String digitalId, String certification) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			CertificationTypes certificationType = CertificationTypes.fromCode(certification);
			if (certificationType == CertificationTypes.L0)
				validationResultDto = validateSignatureValidity(digitalId);
			else if (certificationType == CertificationTypes.L1)
				validationResultDto = validateSignatureValidityL1(digitalId);
		} catch (ToolkitException e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateCaptureInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {

		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateRCaptureInfo(ValidationInputDto responseDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {

		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private boolean validateMethodName(String methodName) throws Exception {
		MethodName.fromCode(methodName);
		return true;
	}

	// String is base64urlencoded data
	private ValidationResultDto validateUnsignedDigitalID(String digitalId) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			if (Objects.isNull(digitalId)) {
				DigitalIdDto digitalIdDto = objectMapper.readValue(digitalId, DigitalIdDto.class);
				if (Objects.isNull(digitalIdDto)) {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("DigitalID value is null");
				} else {
					validationResultDto.setStatus(AppConstants.SUCCESS);
					validationResultDto.setDescription("UnsignedDigitalID validation success");
				}
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("UnsignedDigitalID response value is null");
			}
		} catch (Exception e) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription(" validateUnsignedDigitalID - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private boolean validateSignature(String info)
			throws JoseException, CertificateExpiredException, CertificateNotYetValidException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(info);
		List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
		X509Certificate certificate = certificateChainHeaderValue.get(0);
		certificate.checkValidity();
		PublicKey publicKey = certificate.getPublicKey();
		jws.setKey(publicKey);
		return jws.verifySignature();
	}

	private ValidationResultDto validateSignatureValidity(String info) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(info);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			PublicKey publicKey = certificate.getPublicKey();
			jws.setKey(publicKey);
			jws.getLeafCertificateHeaderValue().checkValidity();
			validationResultDto.setStatus(AppConstants.SUCCESS);
			validationResultDto.setDescription("Signature Validation Success");
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

	private String getJWTPayload(String info) throws JoseException {
		JsonWebSignature jws = new JsonWebSignature();
		jws.setCompactSerialization(info);
		List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
		X509Certificate certificate = certificateChainHeaderValue.get(0);
		PublicKey publicKey = certificate.getPublicKey();
		jws.setKey(publicKey);
		boolean verified = jws.verifySignature();
		if (verified) {
			String payload = jws.getEncodedPayload();
			return StringUtil.toUtf8String(StringUtil.base64UrlDecode((payload)));
		}
		return null;
	}

	private ValidationResultDto validateSignatureValidityL1(String info) {
		return validateSignatureValidity(info);
	}

}
