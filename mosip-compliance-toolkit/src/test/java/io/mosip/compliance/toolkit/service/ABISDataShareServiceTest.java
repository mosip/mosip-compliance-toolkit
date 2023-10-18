package io.mosip.compliance.toolkit.service;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import io.mosip.compliance.toolkit.dto.abis.*;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.util.DataShareHelper;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.entity.AbisDataShareTokenEntity;
import io.mosip.compliance.toolkit.repository.AbisDataShareTokenRepository;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ABISDataShareServiceTest {

	@InjectMocks
	private ABISDataShareService abisDataShareService;

	@Mock
	private TestCasesService testCasesService;

	@Mock
	private KeyManagerHelper keyManagerHelper;

	@Mock
	private Authentication authentication;

	@Mock
	SecurityContext securityContext;

	@Mock
	private ObjectStoreAdapter objectStore;

	@Mock
	private BiometricTestDataRepository biometricTestDataRepository;

	@Mock
	private ObjectMapperConfig objectMapperConfig;

	@Mock
	private AbisDataShareTokenRepository abisDataShareTokenRepository;

	@Mock
	private ObjectMapper mapper;

	@Mock
	private DataShareHelper dataShareHelper;

	private MosipUserDto mosipUserDto;

	@Before
    public void before() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

		String tokenTestcases = "ABIS3030, ABIS3031";
		ReflectionTestUtils.setField(abisDataShareService, "tokenTestCases", tokenTestcases);

	}

	/*
	 * This class tests the authUserDetails method
	 */
	@Test
	public void authUserDetailsTest() {
		ReflectionTestUtils.invokeMethod(abisDataShareService, "authUserDetails");
	}

	/*
	 * This class tests the getPartnerId method
	 */
	@Test
	public void getPartnerIdTest() {
		String result = ReflectionTestUtils.invokeMethod(abisDataShareService, "getPartnerId");
		Assert.assertEquals(mosipUserDto.getUserId(), result);
	}

	/*
	 * This class tests the getDataShareUrl method
	 */
	@Test
	public void getDataShareUrlTest() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		when(testCasesService.getPartnerTestDataStream(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(inputStream);

		DataShareRequestDto dataShareRequestDto = new DataShareRequestDto();
		dataShareRequestDto.setTestcaseId("ABIS3000");
		dataShareRequestDto.setBioTestDataName("testdata");
		dataShareRequestDto.setCbeffFileSuffix(1);

		DataShareResponseDto dataShareResponseDto = new DataShareResponseDto();
		dataShareResponseDto.setId("asdadada");
		dataShareResponseDto.setResponseTime(LocalDateTime.now().toString());
		dataShareResponseDto.setVersion("0.9.5");
		DataShareResponseDto.DataShare dataShare = new DataShareResponseDto.DataShare();
		dataShare.setUrl("https://");
		dataShare.setTransactionsAllowed(4);
		dataShare.setPolicyId("sdsdsd12");
		dataShare.setSubscriberId("sdsadsad22");
		dataShare.setValidForInMinutes(4);
		dataShareResponseDto.setDataShare(dataShare);

		Mockito.when(dataShareHelper.createDataShareUrl(Mockito.any(), Mockito.any())).thenReturn(dataShareResponseDto);

		ResponseWrapper<DataShareResponseWrapperDto> result = abisDataShareService
				.createDataShareUrl(dataShareRequestDto);

		assertNotNull(result);
	}

	@Test
	public void getDataShareUrlTestCondition() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		when(testCasesService.getPartnerTestDataStream(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(inputStream);

		DataShareRequestDto dataShareRequestDto = new DataShareRequestDto();
		dataShareRequestDto.setTestcaseId("ABIS3000");
		dataShareRequestDto.setBioTestDataName("testdata");
		dataShareRequestDto.setCbeffFileSuffix(1);
		dataShareRequestDto.setAbisProjectModality("ABIS");

		ResponseWrapper<DataShareResponseWrapperDto> result = abisDataShareService
				.createDataShareUrl(dataShareRequestDto);

		assertNotNull(result);
	}

	/*
	 * This class tests the expireDataShareUrl method
	 */
	@Test
	public void expireDataShareUrlTest() throws IOException {
		DataShareExpireRequest dataShareExpireRequest = new DataShareExpireRequest();
		dataShareExpireRequest.setUrl("wss://activemq.dev.mosip.net/ws");
		dataShareExpireRequest.setTransactionsAllowed(1);

		Mockito.when(dataShareHelper.callDataShareUrl(Mockito.any())).thenReturn("ABCD");

		abisDataShareService.expireDataShareUrl(dataShareExpireRequest);
	}

	/*
	 * These functions test the saveDataShareToken method
	 */
	@Test
	public void testSaveDataShareToken_existingToken() {

		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);

		AbisDataShareTokenEntity savedEntity = new AbisDataShareTokenEntity();
		savedEntity.setToken("token");
		savedEntity.setTestRunId("9012");
		savedEntity.setTestCaseId("5678");
		savedEntity.setPartnerId("1234");

		when(abisDataShareTokenRepository.findTokenForTestRun(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(Optional.of(savedEntity));

		ResponseWrapper<String> responseWrapper = abisDataShareService.saveDataShareToken(requestWrapper);

		verify(abisDataShareTokenRepository, Mockito.times(1)).updateResultInRow(AppConstants.SUCCESS,
				savedEntity.getPartnerId(), savedEntity.getTestCaseId(), savedEntity.getTestRunId());
		Assert.assertEquals(AppConstants.SUCCESS, responseWrapper.getResponse());
	}

	@Test
	public void testSaveDataShareToken_newToken() {
		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);

		when(abisDataShareTokenRepository.findTokenForTestRun(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(Optional.empty());

		ResponseWrapper<String> responseWrapper = abisDataShareService.saveDataShareToken(requestWrapper);

		verify(abisDataShareTokenRepository, Mockito.times(1)).save(Mockito.any(AbisDataShareTokenEntity.class));
	}

	@Test
	public void testSaveDataShareTokenException() {
		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);

		when(abisDataShareTokenRepository.findTokenForTestRun(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(new ToolkitException("TOOLKIT_EXCEPTION_001","Exception"));

		ResponseWrapper<String> responseWrapper = abisDataShareService.saveDataShareToken(requestWrapper);
	}

	@Test
	public void testInvalidateDataShareToken() throws IOException {
		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);

		ObjectMapper objectMapperMock = mock(ObjectMapper.class);
		when(objectMapperConfig.objectMapper()).thenReturn(objectMapperMock);

		ResponseWrapper<AuthNResponse> mockResponse = new ResponseWrapper<>();
		mockResponse.setErrors(null);
		mockResponse.setId("asdf");
		mockResponse.setVersion("0.9.5");
		mockResponse.setResponsetime(LocalDateTime.now());
		AuthNResponse authNResponse = new AuthNResponse();
		authNResponse.setMessage("auth response");
		authNResponse.setStatus("success");
		mockResponse.setResponse(authNResponse);
		TypeReference<ResponseWrapper<AuthNResponse>> ref = new TypeReference<>() {};

		Mockito.when(objectMapperMock.readValue(Mockito.anyString(), Mockito.any(TypeReference.class))).thenReturn(mockResponse);


		Mockito.when(dataShareHelper.invalidateToken()).thenReturn("ABCD");

		ResponseWrapper<String> response = abisDataShareService.invalidateDataShareToken(requestWrapper);

	}

	@Test
	public void testInvalidateDataShareToken_failureResultCase() {
		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);
		AbisDataShareTokenEntity abisDataShareTokenEntity = new AbisDataShareTokenEntity();
		abisDataShareTokenEntity.setPartnerId("1234");
		abisDataShareTokenEntity.setTestCaseId("5678");
		abisDataShareTokenEntity.setTestRunId("9012");
		abisDataShareTokenEntity.setToken("Token");

		Optional<AbisDataShareTokenEntity> dbEntity = Optional.of(abisDataShareTokenEntity);
		when(abisDataShareTokenRepository.findTokenForTestRun(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(dbEntity);

		ResponseWrapper<String> response = abisDataShareService.invalidateDataShareToken(requestWrapper);

		assertEquals(AppConstants.SUCCESS, response.getResponse());
	}

	@Test
	public void testInvalidateDataShareToken_successResultCase() {
		RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
		DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
		dataShareSaveTokenRequest.setPartnerId("1234");
		dataShareSaveTokenRequest.setCtkTestCaseId("5678");
		dataShareSaveTokenRequest.setCtkTestRunId("9012");
		requestWrapper.setRequest(dataShareSaveTokenRequest);
		AbisDataShareTokenEntity abisDataShareTokenEntity = new AbisDataShareTokenEntity();
		abisDataShareTokenEntity.setPartnerId("1234");
		abisDataShareTokenEntity.setTestCaseId("5678");
		abisDataShareTokenEntity.setTestRunId("9012");
		abisDataShareTokenEntity.setToken("Tokenn");

		Optional<AbisDataShareTokenEntity> dbEntity = Optional.of(abisDataShareTokenEntity);
		when(abisDataShareTokenRepository.findTokenForTestRun(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(dbEntity);

		ResponseWrapper<String> response = abisDataShareService.invalidateDataShareToken(requestWrapper);

		assertEquals(AppConstants.SUCCESS, response.getResponse());
	}

	/*
	 * This method is used to get MosipUserDto in class
	 */
	private MosipUserDto getMosipUserDto() {
		MosipUserDto mosipUserDto = new MosipUserDto();
		mosipUserDto.setUserId("123");
		mosipUserDto.setMail("abc@gmail.com");
		return mosipUserDto;
	}
}
