package io.mosip.compliance.toolkit.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import io.mosip.compliance.toolkit.dto.abis.DataShareResponseDto;

@Component
public class DataShareHelper {

	private static final String FILE = "file";

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.service.authmanager.invalidate.url}")
	private String invalidateTokenUrl;

	public DataShareResponseDto createDataShareUrl(byte[] cbeffFileBytes, String dataShareFullCreateUrl)
			throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> fileMap = new LinkedMultiValueMap<String, Object>();
		fileMap.add("name", "cbeff.xml");
		fileMap.add("filename", "cbeff.xml");		
		ByteArrayResource contentsAsResource = new ByteArrayResource(cbeffFileBytes) {
	        @Override
	        public String getFilename() {
	            return "cbeff.xml"; // Filename has to be returned in order to be able to post.
	        }
	    };
	    fileMap.add(FILE, contentsAsResource);
		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(fileMap, headers);
		
		ResponseEntity<DataShareResponseDto> responseEntity = restTemplate.exchange(dataShareFullCreateUrl,
				HttpMethod.POST, httpEntity, new ParameterizedTypeReference<DataShareResponseDto>() {
				});
		DataShareResponseDto body = responseEntity.getBody();
		return body;
	}

	public String callDataShareUrl(String urlToBeInvoked) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(urlToBeInvoked, HttpMethod.GET, requestEntity,
				new ParameterizedTypeReference<String>() {
				});
		String body = responseEntity.getBody();
		return body;
	}

	public String invalidateToken() throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> requestEntity = new HttpEntity<>(null, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(invalidateTokenUrl, HttpMethod.POST,
				requestEntity, new ParameterizedTypeReference<String>() {
				});
		String body = responseEntity.getBody();
		return body;
	}
}
