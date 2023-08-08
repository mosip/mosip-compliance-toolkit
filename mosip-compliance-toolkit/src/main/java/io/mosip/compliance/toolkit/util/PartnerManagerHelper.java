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
import org.springframework.web.client.RestTemplate;

import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto;

@Component
public class PartnerManagerHelper {

	@Value("${mosip.service.partnermanager.getparnter.url}")
	private String getPartnerUrl;

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	public PartnerDetailsDto getPartnerDetails(String partnerId) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<PartnerDetailsDto> responseEntity = restTemplate.exchange(getPartnerUrl + "/" + partnerId,
				HttpMethod.GET, requestEntity, new ParameterizedTypeReference<PartnerDetailsDto>() {
				});
		PartnerDetailsDto body = responseEntity.getBody();
		return body;
	}
}
