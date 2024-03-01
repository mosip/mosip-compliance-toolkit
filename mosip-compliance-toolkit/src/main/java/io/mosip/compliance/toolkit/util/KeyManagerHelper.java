package io.mosip.compliance.toolkit.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.mosip.compliance.toolkit.dto.EncryptionKeyResponseDto;
import io.mosip.compliance.toolkit.validators.SBIValidator.*;

@Component
public class KeyManagerHelper {

	@Value("${mosip.service.keymanager.decrypt.appid}")
	private String appId;

	@Value("${mosip.service.keymanager.decrypt.refid}")
	private String refId;

	@Value("${mosip.service.keymanager.verifyCertificateTrust.url}")
	protected String keyManagerTrustUrl;

	@Value("${mosip.service.keymanager.decrypt.url}")
	private String keyManagerDecryptUrl;

	@Value("${mosip.service.keymanager.encrypt.url}")
	private String keyManagerEncryptUrl;

	@Value("${mosip.service.keymanager.certificate.key.url}")
	private String keyManagerGetCertificateKeyUrl;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	public String getAppId() {
		return appId;
	}

	public String getRefId() {
		return refId;
	}

	public DecryptValidatorResponseDto decryptionResponse(DecryptValidatorRequestDto decryptValidatorRequestDto)
			throws RestClientException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DecryptValidatorRequestDto> requestEntity = new HttpEntity<>(decryptValidatorRequestDto, headers);
		ResponseEntity<DecryptValidatorResponseDto> responseEntity = restTemplate.exchange(keyManagerDecryptUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<DecryptValidatorResponseDto>() {
				});
		DecryptValidatorResponseDto body = responseEntity.getBody();
		return body;
	}

	public EncryptValidatorResponseDto encryptionResponse(EncryptValidatorRequestDto encryptValidatorRequestDto)
			throws RestClientException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<EncryptValidatorRequestDto> requestEntity = new HttpEntity<>(encryptValidatorRequestDto, headers);
		ResponseEntity<EncryptValidatorResponseDto> responseEntity = restTemplate.exchange(keyManagerEncryptUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<EncryptValidatorResponseDto>() {
				});
		EncryptValidatorResponseDto body = responseEntity.getBody();
		return body;
	}

	public DeviceValidatorResponseDto trustValidationResponse(DeviceValidatorDto deviceValidatorDto)
			throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DeviceValidatorDto> requestEntity = new HttpEntity<>(deviceValidatorDto, headers);
		ResponseEntity<DeviceValidatorResponseDto> responseEntity = restTemplate.exchange(keyManagerTrustUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<DeviceValidatorResponseDto>() {
				});
		DeviceValidatorResponseDto body = responseEntity.getBody();
		return body;
	}

	public EncryptionKeyResponseDto getCertificate() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<EncryptionKeyResponseDto> responseEntity = restTemplate.exchange(keyManagerGetCertificateKeyUrl,
				HttpMethod.GET, requestEntity, new ParameterizedTypeReference<EncryptionKeyResponseDto>() {
				});
		EncryptionKeyResponseDto body = responseEntity.getBody();
		return body;
	}

}
