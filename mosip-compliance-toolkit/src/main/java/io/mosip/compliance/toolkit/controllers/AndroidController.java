package io.mosip.compliance.toolkit.controllers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;

@CrossOrigin(origins = "http://localhost", allowedHeaders = "*")
@RestController
/**
 * In case of android using Cookies for session management is not a reliable
 * solution. Hence this controller maps the Auth header to a Cookie before
 * forwarding the request.
 * 
 * @author Mayura Deshmukh
 *
 */
public class AndroidController {
	 private Logger log = LoggerConfiguration.logConfig(AndroidController.class);

	@Value("${rest.server.protocol}")
	private String protocol;

	@Value("${rest.server.url}")
	private String server;

	@Value("${rest.server.port}")
	private int port;

	@Value("${rest.server.service.endpoint}")
	private String serviceEndPoint;

	@RequestMapping(value = "/android/**", method = { RequestMethod.GET, RequestMethod.DELETE, RequestMethod.PUT,
			RequestMethod.POST })
	public ResponseEntity mirrorAnyRequest(@RequestBody(required = false) String body, HttpMethod method,
			HttpServletRequest request, HttpServletResponse response)
			throws URISyntaxException, IOException, ServletException {
		System.out.println("AndroidController mirrorAnyRequest");
		System.out.println("**********************************************************************");
		// handle the multipart form data request
		if (isMultipart(request)) {
			return mirrorMultiPartRequest(method, (MultipartHttpServletRequest) request, response);
		}
		String requestUrl = request.getRequestURI();
		System.out.println(requestUrl);
		requestUrl = requestUrl.replaceAll("/android", "");
		log.debug("sessionId", "idType", "id", requestUrl);
		
		URI uri = new URI(protocol, null, server, port, null, null, null);
		uri = UriComponentsBuilder.fromUri(uri).path(serviceEndPoint + requestUrl).query(request.getQueryString())
				.build(true).toUri();
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			// ignore the host header
			if (!headerName.equalsIgnoreCase("host")) {
				headers.set(headerName, request.getHeader(headerName));
			}
			// v imp to set the auth cookie
			if (headerName.equalsIgnoreCase("Authorization")) {
				headers.set("Cookie", "Authorization=" + request.getHeader(headerName));
				headers.set("Authorization", null);
				log.debug("sessionId", "idType", "id", request.getHeader(headerName));
			}
		}
		System.out.println("Calling method: " + method);
		System.out.println("Calling URL: " + uri);
		System.out.println("Request Body: " + body);
		printHeaders(headers, "request");
		HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
		RestTemplate restTemplate = new RestTemplate();
		try {
			log.debug("sessionId", "idType", "id",uri);
			//return restTemplate.exchange(uri, method, httpEntity, String.class);
			ResponseEntity responseEntity = restTemplate.exchange(uri, method, httpEntity, String.class);
			System.out.println("recvd response");
			System.out.println(responseEntity);
			printHeaders(responseEntity.getHeaders(), "response");
			return responseEntity;
		} catch (HttpStatusCodeException e) {
			System.out.println("Exception: " + e.getResponseBodyAsString());
			return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}
	}
	private void printHeaders(HttpHeaders headers, String headersType) {
		System.out.println("With " + headersType + " Headers");
		Iterator itr = headers.entrySet().iterator();
		while (itr.hasNext()) {
			System.out.println(itr.next().toString());
		}
	}

	private ResponseEntity mirrorMultiPartRequest(HttpMethod method, MultipartHttpServletRequest request,
			HttpServletResponse response) throws URISyntaxException, IOException, ServletException {
		String requestUrl = request.getRequestURI();
		requestUrl = requestUrl.replaceAll("/android", "");
		URI uri = new URI(protocol, null, server, port, null, null, null);
		uri = UriComponentsBuilder.fromUri(uri).path(serviceEndPoint + requestUrl).query(request.getQueryString())
				.build(true).toUri();
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = request.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			// ignore the host header
			if (!headerName.equalsIgnoreCase("host")) {
				headers.set(headerName, request.getHeader(headerName));
			}
			// v imp to set the auth cookie
			if (headerName.equalsIgnoreCase("Authorization")) {
				headers.set("Cookie", "Authorization=" + request.getHeader(headerName));
			}
		}
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
		RestTemplate restTemplate = new RestTemplate();
		try {
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
			MultiValueMap<String, MultipartFile> map = request.getMultiFileMap();
			MultipartFile file = map.getFirst("file");
			body.add("file", file.getBytes());
			body.add("Document request", request.getParameter("Document request").toString());
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(
					body, headers);
			return restTemplate.exchange(uri, method, requestEntity, String.class);
		} catch (HttpStatusCodeException e) {
			return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
					.body(e.getResponseBodyAsString());
		}
	}

	private boolean isMultipart(HttpServletRequest request) {
		final String header = request.getHeader("Content-Type");
		if (header == null) {
			return false;
		}
		return header.contains("multipart/form-data");
	}
}
