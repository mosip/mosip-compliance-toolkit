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

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.dto.abis.DataShareResponseDto;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.openid.bridge.api.constants.Constants;

@Component
public class DataShareHelper {

	private static final String FILE = "file";

	@Qualifier("selfTokenRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${mosip.abis.clientid}")
	private String clientID;

	@Value("${mosip.abis.clientsecret}")
	private String clientSecret;

	@Value("${mosip.iam.revoke_endpoint}")
	private String revokeUrl;

	private Logger log = LoggerConfiguration.logConfig(DataShareHelper.class);

	
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

	public boolean revokeToken(String authToken) throws IOException {
		log.info("revokeToken to be invoked");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(Constants.CLIENT_ID, clientID);
		map.add(Constants.CLIENT_SECRET, clientSecret);
		map.add("token", authToken);
		map.add("token_type_hint", "access_token");
		
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(revokeUrl, HttpMethod.POST, requestEntity,
				new ParameterizedTypeReference<String>() {
				});
		log.info("revokeToken called");
		int status = responseEntity.getStatusCodeValue();
		log.info("status for revokeToken: " + status);
		if (status == 200) {
			return true;
		}
		return false;
	}
}
