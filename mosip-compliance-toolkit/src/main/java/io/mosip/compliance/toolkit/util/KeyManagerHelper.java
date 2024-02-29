package io.mosip.compliance.toolkit.util;

import java.io.IOException;

import io.mosip.compliance.toolkit.dto.testrun.DecryptDataRequestDto;
import io.mosip.compliance.toolkit.dto.testrun.DecryptDataResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.EncryptDataRequestDto;
import io.mosip.compliance.toolkit.dto.testrun.EncryptDataResponseDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
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

	@Value("${mosip.service.keymanager.encryption.key.url}")
	private String keyManagerGetEncryptionKeyUrl;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	public String getAppId() {
		return appId;
	}

	public String getRefId() {
		return refId;
	}

	public DecryptValidatorResponseDto decryptionResponse(DecryptValidatorDto decryptValidatorDto)
			throws RestClientException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DecryptValidatorDto> requestEntity = new HttpEntity<>(decryptValidatorDto, headers);
		ResponseEntity<DecryptValidatorResponseDto> responseEntity = restTemplate.exchange(keyManagerDecryptUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<DecryptValidatorResponseDto>() {
				});
		DecryptValidatorResponseDto body = responseEntity.getBody();
		return body;
	}

	public ResponseWrapper<EncryptDataResponseDto> dataEncryptionResponse(RequestWrapper<EncryptDataRequestDto> encryptRequest)
			throws RestClientException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RequestWrapper<EncryptDataRequestDto>> requestEntity = new HttpEntity<>(encryptRequest, headers);
		ResponseEntity<ResponseWrapper<EncryptDataResponseDto>> responseEntity = restTemplate.exchange(keyManagerEncryptUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
				});
		ResponseWrapper<EncryptDataResponseDto> body = responseEntity.getBody();
		return body;
	}

	public ResponseWrapper<DecryptDataResponseDto> dataDecryptionResponse(RequestWrapper<DecryptDataRequestDto> decryptRequest)
			throws RestClientException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RequestWrapper<DecryptDataRequestDto>> requestEntity = new HttpEntity<>(decryptRequest, headers);
		ResponseEntity<ResponseWrapper<DecryptDataResponseDto>> responseEntity = restTemplate.exchange(keyManagerDecryptUrl,
				HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
				});
		ResponseWrapper<DecryptDataResponseDto> body = responseEntity.getBody();
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

	public EncryptionKeyResponseDto encryptionKeyResponse() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<EncryptionKeyResponseDto> responseEntity = restTemplate.exchange(keyManagerGetEncryptionKeyUrl,
				HttpMethod.GET, requestEntity, new ParameterizedTypeReference<EncryptionKeyResponseDto>() {
				});
		EncryptionKeyResponseDto body = responseEntity.getBody();
		return body;
	}

}
