package io.mosip.compliance.toolkit.validators;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Objects;

import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.service.ResourceCacheService;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.keymanagerservice.exception.KeymanagerServiceException;
import io.mosip.kernel.partnercertservice.constant.PartnerCertManagerConstants;
import io.mosip.kernel.partnercertservice.util.PartnerCertificateManagerUtil;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import java.security.cert.CertificateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.logger.spi.Logger;

import javax.security.auth.x500.X500Principal;

@Component
public class SignatureValidator extends SBIValidator {

	protected static final String CERTIFICATION = "certification";

	protected static final String DEVICE_PROVIDER = "DEVICE_PROVIDER";

	private Logger log = LoggerConfiguration.logConfig(SignatureValidator.class);

	@Autowired
	private KeyManagerHelper keyManagerHelper;

	@Autowired
	private ResourceCacheService resourceCacheService;

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public boolean isDeviceProvider() {
		AuthUserDetails authUserDetails = authUserDetails();
		Collection<? extends GrantedAuthority> authorities = authUserDetails.getAuthorities();
		if (authorities == null) {
			throw new ToolkitException(ToolkitErrorCodes.INVALID_USER_DETAILS.getErrorCode(), ToolkitErrorCodes.INVALID_USER_DETAILS.getErrorMessage());
		}
		// Check if the user has the "DEVICE_PROVIDER" partnerType
		return authorities.stream()
				.anyMatch(authority -> authority.getAuthority().replaceFirst("^ROLE_", "").equals(DEVICE_PROVIDER));
	}

	@Override
	public ValidationResultDto validateResponse(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			if (validateMethodName(inputDto.getMethodName())) {
				if (Objects.nonNull(inputDto.getMethodResponse())) {
					switch (MethodName.fromCode(inputDto.getMethodName())) {
					case DEVICE:
						validationResultDto = validateDiscoverySignature(inputDto);
						break;
					case INFO:
						validationResultDto = validateDeviceSignature(inputDto);
						break;
					case CAPTURE:
						validationResultDto = validateSignature(inputDto);
						break;
					case RCAPTURE:
						validationResultDto = validateSignature(inputDto);
						break;
					default:
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Method not supported");
						validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_001");
						break;
					}
				} else {
					validationResultDto.setStatus(AppConstants.FAILURE);
					validationResultDto.setDescription("Response is empty");
					validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_002");
				}
			}
		} catch (ToolkitException e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
			validationResultDto.setDescriptionKey(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateDiscoverySignature(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDiscoverResponse = (ArrayNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ArrayNode.class);
			ObjectNode discoveryInfoNode = (ObjectNode) arrDiscoverResponse.get(0);

			String digitalId = StringUtil
					.toUtf8String(StringUtil.base64UrlDecode(discoveryInfoNode.get(DIGITAL_ID).asText()));
			validationResultDto = validateUnsignedDigitalID(digitalId);
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateUnsignedDigitalID(String digitalId) throws Exception {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		ObjectNode digitalIdDto = objectMapperConfig.objectMapper().readValue(digitalId, ObjectNode.class);
		if (Objects.isNull(digitalIdDto) || digitalIdDto.get(DEVICE_TYPE).isNull()
				|| digitalIdDto.get(DEVICE_SUB_TYPE).isNull()) {
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription("Unsigned Digital ID validation failed");
			validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_003");
		} else {
			validationResultDto.setStatus(AppConstants.SUCCESS);
			validationResultDto.setDescription("Unsigned Digital ID validation is successful");
			validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_004");
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateDeviceSignature(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ArrayNode arrDeviceInfoResponse = (ArrayNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ArrayNode.class);

			for (int deviceIndex = 0; deviceIndex < arrDeviceInfoResponse.size(); deviceIndex++) {
				ObjectNode deviceInfoNode = (ObjectNode) arrDeviceInfoResponse.get(deviceIndex);
				if (isDeviceInfoUnSigned(deviceInfoNode)) {
					validationResultDto = validateUnSignedDeviceInfo(deviceInfoNode);
				} else {
					validationResultDto = validateSignedDeviceInfo(deviceInfoNode);
				}
				if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
					break;
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateUnSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode deviceInfoDto = getUnsignedDeviceInfo(objectNode.get(DEVICE_INFO).asText());
			DeviceStatus deviceStatus = DeviceStatus.fromCode(deviceInfoDto.get(DEVICE_STATUS).asText());

			if (deviceStatus == DeviceStatus.NOT_REGISTERED) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Device is not registered");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_005");
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Device is registered, so can not be unsigned");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_006");
			}
		} catch (ToolkitException e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateSignedDeviceInfo(ObjectNode objectNode) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			String deviceInfo = objectNode.get(DEVICE_INFO).asText();
			validationResultDto = checkIfJWTSignatureIsValid(deviceInfo);
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				validationResultDto = trustRootValidation(getCertificate(deviceInfo), PartnerTypes.DEVICE.toString(),
						TRUST_FOR_DEVICE_INFO);
				// validate orgName if trust root validation is successful
				if (validationResultDto.getStatus().equals(AppConstants.SUCCESS) && isDeviceProvider()) {
					validationResultDto = validateOrgNameInCertificate(getCertificate(deviceInfo), PartnerTypes.DEVICE.toString(), TRUST_FOR_DEVICE_INFO);
				}
				if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
					ObjectNode deviceInfoDecoded = objectMapperConfig.objectMapper().readValue(getPayload(deviceInfo),
							ObjectNode.class);

					if (Objects.isNull(deviceInfoDecoded)) {
						validationResultDto.setStatus(AppConstants.FAILURE);
						validationResultDto.setDescription("Device info Decoded value is null");
						validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_007");
					} else {
						validationResultDto = validateSignedDigitalId(deviceInfoDecoded.get(DIGITAL_ID).asText(),
								deviceInfoDecoded.get(CERTIFICATION).asText(), TRUST_FOR_DIGITAL_ID);
					}
				}
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateSignature(ValidationInputDto inputDto) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			ObjectNode captureInfoResponse = (ObjectNode) objectMapperConfig.objectMapper()
					.readValue(inputDto.getMethodResponse(), ObjectNode.class);

			final JsonNode arrBiometricNodes = captureInfoResponse.get(BIOMETRICS);
			if (arrBiometricNodes.isArray()) {
				for (final JsonNode biometricNode : arrBiometricNodes) {
					String dataInfo = biometricNode.get(DATA).asText();
					validationResultDto = checkIfJWTSignatureIsValid(dataInfo);
					if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
						validationResultDto = trustRootValidation(getCertificate(dataInfo),
								PartnerTypes.DEVICE.toString(), TRUST_FOR_BIOMETRIC_INFO);
						// validate orgName after trust root validation is successful
						if (validationResultDto.getStatus().equals(AppConstants.SUCCESS) && isDeviceProvider()) {
							validationResultDto = validateOrgNameInCertificate(getCertificate(dataInfo), PartnerTypes.DEVICE.toString(), TRUST_FOR_BIOMETRIC_INFO);
						}
						if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
							String biometricData = getPayload(dataInfo);
							ObjectNode biometricDataNode = (ObjectNode) objectMapperConfig.objectMapper()
									.readValue(biometricData, ObjectNode.class);

							ObjectNode extraInfo = (ObjectNode) objectMapperConfig.objectMapper()
									.readValue(inputDto.getExtraInfoJson(), ObjectNode.class);
							String certificationType = extraInfo.get(CERTIFICATION_TYPE).asText();
							validationResultDto = validateSignedDigitalId(biometricDataNode.get(DIGITAL_ID).asText(),
									certificationType, TRUST_FOR_DIGITAL_ID);
						}
					}
					if (validationResultDto.getStatus().equals(AppConstants.FAILURE))
						break;
				}
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto
					.setDescription("SignatureValidator failure - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	protected ValidationResultDto validateSignedDigitalId(String digitalId, String certificationType, String trustFor) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			CertificationTypes certification = CertificationTypes.fromCode(certificationType);
			validationResultDto = checkIfJWTSignatureIsValid(digitalId);
			if (validationResultDto.getStatus().equals(AppConstants.SUCCESS)) {
				if (certification == CertificationTypes.L0) {
					validationResultDto = trustRootValidation(getCertificate(digitalId), PartnerTypes.DEVICE.toString(),
							trustFor);
				} else if (certification == CertificationTypes.L1) {
					validationResultDto = trustRootValidation(getCertificate(digitalId), PartnerTypes.FTM.toString(),
							trustFor);
				}
			}
		} catch (ToolkitException e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	public ValidationResultDto trustRootValidation(String certificateData, String partnerType, String trustFor)
			throws IOException {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		DeviceValidatorDto deviceValidatorDto = new DeviceValidatorDto();
		deviceValidatorDto.setRequesttime(getCurrentDateAndTimeForAPI());
		DeviceTrustRequestDto trustRequest = new DeviceTrustRequestDto();

		trustRequest.setCertificateData(getCertificateData(certificateData));
		trustRequest.setPartnerDomain(partnerType);
		deviceValidatorDto.setRequest(trustRequest);

		try {
			DeviceValidatorResponseDto deviceValidatorResponseDto = keyManagerHelper
					.trustValidationResponse(deviceValidatorDto);

			if ((deviceValidatorResponseDto.getErrors() != null && deviceValidatorResponseDto.getErrors().size() > 0)
					|| (deviceValidatorResponseDto.getResponse().getStatus().equals("false"))) {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Trust Validation Failed for [" + trustFor + "] >> PartnerType["
						+ partnerType + "] and CertificateData[" + certificateData + "]");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_008" + AppConstants.ARGUMENTS_DELIMITER + trustFor + AppConstants.ARGUMENTS_SEPARATOR + partnerType +
						AppConstants.ARGUMENTS_SEPARATOR + certificateData);
			} else {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Trust Root Validation is Successful");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_009");
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"Exception in Trust root Validation - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	private ValidationResultDto validateOrgNameInCertificate(String certificateData, String partnerType, String trustFor) {
		ValidationResultDto validationResultDto = new ValidationResultDto();
		try {
			X509Certificate reqX509Cert = (X509Certificate) convertToCertificate(getCertificateData(certificateData));
			String certOrgName = getCertificateOrgName(reqX509Cert.getSubjectX500Principal());
			String orgName = resourceCacheService.getOrgName(getPartnerId());
			if (orgName.equalsIgnoreCase(certOrgName)) {
				validationResultDto.setStatus(AppConstants.SUCCESS);
				validationResultDto.setDescription("Trust Root Validation is Successful");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_009");
			} else {
				validationResultDto.setStatus(AppConstants.FAILURE);
				validationResultDto.setDescription("Trust Validation Failed - Organization Name is not matching with the Certificate for [" + trustFor + "] >> PartnerType["
						+ partnerType + "] and CertificateData[" + certificateData + "]");
				validationResultDto.setDescriptionKey("SIGNATURE_VALIDATOR_008" + AppConstants.ARGUMENTS_DELIMITER + trustFor + AppConstants.ARGUMENTS_SEPARATOR + partnerType +
						AppConstants.ARGUMENTS_SEPARATOR + certificateData);
			}
		} catch (Exception e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "In SignatureValidator - " + e.getMessage());
			validationResultDto.setStatus(AppConstants.FAILURE);
			validationResultDto.setDescription(
					"Exception in Organization Name validation - " + "with Message - " + e.getLocalizedMessage());
		}
		return validationResultDto;
	}

	public static String getCertificateOrgName(X500Principal x500CertPrincipal) {
		X500Name x500Name = new X500Name(x500CertPrincipal.getName());
		RDN[] rdns = x500Name.getRDNs(BCStyle.O);
		if (rdns.length == 0) {
			return PartnerCertManagerConstants.EMPTY;
		}
		return IETFUtils.valueToString((rdns[0]).getFirst().getValue());
	}

	public Certificate convertToCertificate(String certData) {
		try {
			StringReader strReader = new StringReader(certData);
			PemReader pemReader = new PemReader(strReader);
			PemObject pemObject = pemReader.readPemObject();
			if (Objects.isNull(pemObject)) {
				log.debug("sessionId", "idType", "id", "Error while parsing certificate ");
				log.error("sessionId", "idType", "id", "In convertToCertificate method of SignatureValidator ");
				throw new KeymanagerServiceException(ToolkitErrorCodes.TOOLKIT_CERTIFICATE_PARSING_ERR.getErrorCode(),
						ToolkitErrorCodes.TOOLKIT_CERTIFICATE_PARSING_ERR.getErrorMessage());
			}
			byte[] certBytes = pemObject.getContent();
			CertificateFactory certFactory = CertificateFactory.getInstance(AppConstants.CERTIFICATE_TYPE);
			return certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
		} catch (IOException | CertificateException e) {
			log.debug("sessionId", "idType", "id", e.getStackTrace());
			log.error("sessionId", "idType", "id", "Error while parsing certificate in convertToCertificate method of SignatureValidator: " + e.getMessage());
			throw new KeymanagerServiceException(ToolkitErrorCodes.TOOLKIT_CERTIFICATE_PARSING_ERR.getErrorCode(),
					ToolkitErrorCodes.TOOLKIT_CERTIFICATE_PARSING_ERR.getErrorMessage());
		}
	}
}
