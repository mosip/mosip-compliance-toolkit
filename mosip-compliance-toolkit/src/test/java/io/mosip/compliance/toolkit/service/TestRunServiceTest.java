package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.entity.*;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.validators.SBIValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunStatusDto;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.TestRunDetailsRepository;
import io.mosip.compliance.toolkit.repository.TestRunRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class TestRunServiceTest {

	@InjectMocks
	private TestRunService testRunService;

	@Mock
	private Authentication authentication;

	@Mock
	private SecurityContext securityContext;

	@Mock
	ResourceCacheService resourceCacheService;

	@Mock
	private CollectionsRepository collectionsRepository;

	@Mock
	private TestRunDetailsRepository testRunDetailsRepository;

	@Mock
	private ObjectMapperConfig objectMapperConfig;

	@Mock
	private ObjectMapper mapper;

	@Mock
	private TestRunRepository testRunRepository;

	@Mock
	TestCaseCacheService testCaseCacheService;

	@Mock
	KeyManagerHelper keyManagerHelper;

	final static String partnerId = "test";

	/*
	 * This class tests the authUserDetails method
	 */
	@Test
	public void authUserDetailsTest() {
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
		ReflectionTestUtils.invokeMethod(testRunService, "authUserDetails");
	}

	/*
	 * This class tests the getPartnerId method
	 */
	@Test
	public void getPartnerIdTest() {
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);
		String result = ReflectionTestUtils.invokeMethod(testRunService, "getPartnerId");
		Assert.assertEquals(mosipUserDto.getUserId(), result);
	}

	/*
	 * This class tests the getUserBy method
	 */
	@Test
	public void getUserByTest() {
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);
		String result = ReflectionTestUtils.invokeMethod(testRunService, "getUserBy");
		Assert.assertEquals(mosipUserDto.getMail(), result);
	}

	/*
	 * This class tests the addTestRun method
	 */
	@Test
	public void addTestRunTest() {
		testRunService.addTestRun(null);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);

		TestRunDto inputTestRun = new TestRunDto();
		String id = "ABCKALKJA";
		inputTestRun.setCollectionId(id);
		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("456");
		testRunService.addTestRun(inputTestRun);
		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
		TestRunEntity entity = new TestRunEntity();
		Mockito.when(mapper.convertValue(inputTestRun, TestRunEntity.class)).thenReturn(entity);
		TestRunEntity outputEntity = new TestRunEntity();
		Mockito.when(testRunRepository.save(Mockito.any())).thenReturn(outputEntity);
		TestRunDto testRun = new TestRunDto();
		Mockito.when(mapper.convertValue(outputEntity, TestRunDto.class)).thenReturn(testRun);
		ResponseWrapper<TestRunDto> response = testRunService.addTestRun(inputTestRun);
		Assert.assertEquals(testRun, response.getResponse());
	}

	/*
	 * This class tests the addTestRun method in case Exception
	 */
	@Test
	public void addTestRunTestException() {
		TestRunDto inputTestRun = new TestRunDto();
		String id = "ABCKALKJA";
		inputTestRun.setCollectionId(id);
		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
		testRunService.addTestRun(inputTestRun);
	}

	/*
	 * This class tests the updateTestRunExecutionTime method
	 */
	@Test
	public void updateTestRunExecutionTimeTest() {
		testRunService.updateTestRunExecutionTime(null);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);

		TestRunDto inputTestRun = new TestRunDto();
		String id = "ABCKALKJA";
		inputTestRun.setCollectionId(id);
		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("456");
		Mockito.when(testRunRepository.updateTestRunById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
		testRunService.updateTestRunExecutionTime(inputTestRun);

		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
		Mockito.when(testRunRepository.updateTestRunById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
		testRunService.updateTestRunExecutionTime(inputTestRun);

		Mockito.when(testRunRepository.updateTestRunById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
		ResponseWrapper<TestRunDto> result = testRunService.updateTestRunExecutionTime(inputTestRun);
		Assert.assertEquals(inputTestRun, result.getResponse());
	}

	/*
	 * This class tests the updateTestRunExecutionTime method in case Exception
	 */
	@Test
	public void updateTestRunExecutionTestException() {
		TestRunDto inputTestRun = new TestRunDto();
		String id = "ABCKALKJA";
		inputTestRun.setCollectionId(id);
		testRunService.updateTestRunExecutionTime(inputTestRun);
	}

	/*
	 * This class tests the addTestRunDetails method
	 */
	@Test
	public void addTestRunDetailsTest() {
		testRunService.addTestRunDetails(null);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);

		TestRunDetailsDto inputTestRunDetails = new TestRunDetailsDto();
		String id = "ABCKALKJA";
		inputTestRunDetails.setRunId(id);
		Mockito.when(testRunRepository.getPartnerIdByRunId(id, "456")).thenReturn("456");
		testRunService.addTestRunDetails(inputTestRunDetails);

		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
		TestRunDetailsEntity entity = new TestRunDetailsEntity();
		Mockito.when(mapper.convertValue(inputTestRunDetails, TestRunDetailsEntity.class)).thenReturn(entity);
		Mockito.when(testRunRepository.getPartnerIdByRunId(id, "123")).thenReturn("123");
		Mockito.when(testRunDetailsRepository.save(entity)).thenReturn(entity);
		ResponseWrapper<TestRunDetailsDto> result = testRunService.addTestRunDetails(inputTestRunDetails);
		Assert.assertNull(result.getResponse());
	}

	/*
	 * This class tests the addTestRunDetails method in case Exception
	 */
	@Test
	public void addTestRunDetailsTestException() {
		TestRunDetailsDto inputTestRunDetails = new TestRunDetailsDto();
		String id = "ABCKALKJA";
		inputTestRunDetails.setRunId(id);
		Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
		testRunService.addTestRunDetails(inputTestRunDetails);
	}

	/*
	 * This class tests the getTestRunDetails method
	 */
	@Test
	public void getTestRunDetailsTest() throws JsonProcessingException {
		testRunService.getTestRunDetails(partnerId, null, false);
		String runId = "123";
		TestRunEntity entity = new TestRunEntity();

		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
		TestRunDetailsDto dto = new TestRunDetailsDto();

		List<TestRunPartialDetailsEntity> testRunPartialDetailsEntityList = new ArrayList<>();
		TestRunPartialDetailsEntity testRunPartialDetailsEntity = new TestRunPartialDetailsEntity();
		testRunPartialDetailsEntityList.add(testRunPartialDetailsEntity);
		Mockito.when(testRunDetailsRepository.getTestRunPartialDetails(anyString(), anyString())).thenReturn(testRunPartialDetailsEntityList);
		Mockito.when(mapper.convertValue(testRunPartialDetailsEntity, TestRunDetailsDto.class)).thenReturn(dto);
		Mockito.when(testRunRepository.getTestRunById(Mockito.any(), Mockito.any())).thenReturn(null);
		testRunService.getTestRunDetails(partnerId, runId, false);

		Mockito.when(testRunRepository.getTestRunById(Mockito.any(), Mockito.any())).thenReturn(entity);
		List<TestRunDetailsEntity> testRunDetailsEntityList = new ArrayList<>();
		Mockito.when(testRunDetailsRepository.getTestRunDetails(runId, "123")).thenReturn(testRunDetailsEntityList);
		testRunService.getTestRunDetails(partnerId, runId, false);

		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		testRunDetailsEntity.setTestcaseId("SBI1090");
		testRunDetailsEntityList.add(testRunDetailsEntity);

		TestCaseEntity testCaseEntity = new TestCaseEntity();
		Mockito.when(testCaseCacheService.getTestCase(anyString())).thenReturn(testCaseEntity);
		JsonNode jsonNode = mock(JsonNode.class);
		Mockito.when(mapper.readTree(testCaseEntity.getTestcaseJson())).thenReturn(jsonNode);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode node = objectMapper.createObjectNode();
		ArrayNode arrayNode = objectMapper.createArrayNode();
		arrayNode.add("rcapture");
		node.put("methodName", arrayNode);
		Mockito.when(jsonNode.get(anyString())).thenReturn(node);
		Mockito.when(testRunDetailsRepository.getTestRunDetails(anyString(), anyString())).thenReturn(testRunDetailsEntityList);
		Mockito.when(mapper.convertValue(testRunDetailsEntity, TestRunDetailsDto.class)).thenReturn(dto);
		ResponseWrapper<TestRunDetailsResponseDto> result = testRunService.getTestRunDetails(partnerId, runId, true);
		TestRunDetailsResponseDto expected = new TestRunDetailsResponseDto();
		List<TestRunDetailsDto> testRunDetailsDtosList = new ArrayList<>();
		testRunDetailsDtosList.add(new TestRunDetailsDto());
		expected.setTestRunDetailsList(testRunDetailsDtosList);
		//Assert.assertEquals(expected, result.getResponse());
	}

	/*
	 * This class tests the getTestRunDetails method in case of exception
	 */
	@Test
	public void getTestRunDetailsTestException() {
		testRunService.getTestRunDetails(partnerId, "123", false);
	}

	/*
	 * This class tests the getTestRunHistory method
	 */
	@Test
	public void getTestRunHistoryTest() {
		testRunService.getTestRunHistory(null, 0, 10);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);

		Page<TestRunHistoryEntity> testRunHistoryEntityPage = Page.empty();
		Mockito.when(testRunRepository.getTestRunHistoryByCollectionId(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(testRunHistoryEntityPage);
		String collectionId = "123";
		int pageNo = 0;
		int pageSize = 10;
		testRunService.getTestRunHistory(collectionId, pageNo, pageSize);

		LocalDateTime lastRunTime = LocalDateTime.now();

		List<TestRunHistoryEntity> entityList = new ArrayList<>();
		TestRunHistoryEntity entity = new TestRunHistoryEntity(collectionId, lastRunTime, 2, 1);
		entityList.add(entity);

		Page<TestRunHistoryEntity> pages = new PageImpl<TestRunHistoryEntity>(entityList);

		Mockito.when(testRunRepository.getTestRunHistoryByCollectionId(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(pages);
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
		TestRunHistoryDto dto = new TestRunHistoryDto();
		Mockito.when(mapper.convertValue(entity, TestRunHistoryDto.class)).thenReturn(dto);
		ResponseWrapper<PageDto<TestRunHistoryDto>> testRunHistoryResponse = testRunService
				.getTestRunHistory(collectionId, pageNo, pageSize);
		Assert.assertEquals(dto, testRunHistoryResponse.getResponse().getContent().get(0));

		testRunHistoryEntityPage = null;
		Mockito.when(testRunRepository.getTestRunHistoryByCollectionId(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(testRunHistoryEntityPage);
		collectionId = "123";
		pageNo = 0;
		pageSize = 10;
		testRunService.getTestRunHistory(collectionId, pageNo, pageSize);
	}

	/*
	 * This class tests the getTestRunHistory method in case of exception
	 */
	@Test
	public void getTestRunHistoryTestException() {
		testRunService.getTestRunHistory("123", 0, 10);
	}

	/*
	 * This class tests the getTestRunStatus method
	 */
	@Test
	public void getTestRunStatusTest() {
		testRunService.getTestRunStatus(null);
		String runId = "123";
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);
		List<TestRunHistoryEntity> testRunHistoryEntityList = new ArrayList<>();
		Mockito.when(testRunRepository.getTestRunSuccessCount(Mockito.any(), Mockito.any())).thenReturn(1);
		Mockito.when(testRunRepository.getPartnerIdByRunId(runId, "456")).thenReturn("456");
		testRunService.getTestRunStatus(runId);

		Mockito.when(testRunRepository.getPartnerIdByRunId(runId, runId)).thenReturn(runId);
		ResponseWrapper<TestRunStatusDto> result = testRunService.getTestRunStatus(runId);
		TestRunStatusDto dto = new TestRunStatusDto();
		dto.setResultStatus(true);
		Assert.assertEquals(dto, result.getResponse());
	}

	/*
	 * This class tests the getTestRunStatus method in case of exception
	 */
	@Test
	public void getTestRunStatusTestException() {
		testRunService.getTestRunStatus("123");
	}

	/*
	 * This class tests the deleteTestRun method in case of exception
	 */
	@Test
	public void deleteTestRunTest() {
		testRunService.deleteTestRun(null);
		String runId = "123";
		TestRunEntity entity = new TestRunEntity();
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		MosipUserDto mosipUserDto = getMosipUserDto();
		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
		Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(testRunRepository.getTestRunById(Mockito.any(), Mockito.any())).thenReturn(null);
		testRunService.deleteTestRun(runId);

		Mockito.when(testRunRepository.getTestRunById(Mockito.any(), Mockito.any())).thenReturn(entity);
		testRunService.deleteTestRun(runId);
	}

	/*
	 * This class tests the deleteTestRun method in case of exception
	 */
	@Test
	public void deleteTestRunTestException() {
		testRunService.deleteTestRun("123");
	}

	/*
	 * This class tests the getMethodDetails method
	 */
	@Test
	public void getMethodDetailsTest() throws JsonProcessingException {
		TestRunEntity testRunEntity = new TestRunEntity();
		Mockito.when(testRunRepository.getTestRunById(anyString(), anyString())).thenReturn(testRunEntity);
		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		Mockito.when(testRunDetailsRepository.getMethodDetails(anyString(), anyString(), anyString(), anyString())).thenReturn(testRunDetailsEntity);
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
		TestCaseEntity testCaseEntity = new TestCaseEntity();
		Mockito.when(testCaseCacheService.getTestCase(anyString())).thenReturn(testCaseEntity);
		JsonNode jsonNode = mock(JsonNode.class);
		Mockito.when(mapper.readTree(testCaseEntity.getTestcaseJson())).thenReturn(jsonNode);
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode node = objectMapper.createObjectNode();
		ArrayNode arrayNode = objectMapper.createArrayNode();
		arrayNode.add("rcapture");
		node.put("methodName", arrayNode);
		Mockito.when(jsonNode.get(anyString())).thenReturn(node);
		testRunService.getMethodDetails("123", "456", "SBI1023", "123");
	}

	@Test
	public void getMethodDetailsExceptionTest() throws JsonProcessingException {
		Mockito.when(testRunRepository.getTestRunById(anyString(), anyString())).thenReturn(null);
		testRunService.getMethodDetails("123", "456", "SBI1023", "123");
	}

	@Test
	public void getMethodDetailsExceptionTest1() throws JsonProcessingException {
		TestRunEntity testRunEntity = new TestRunEntity();
		Mockito.when(testRunRepository.getTestRunById(anyString(), anyString())).thenThrow(new NullPointerException(""));
		testRunService.getMethodDetails("123", "456", "SBI1023", "123");
	}

	@Test
	public void getMethodDetailsExceptionTest2() throws JsonProcessingException {
		Mockito.when(testRunRepository.getTestRunById(anyString(), anyString())).thenThrow(new NullPointerException(""));
		testRunService.getMethodDetails(null, "456", "SBI1023", "123");
	}


	/*
	 * This class tests the performRcaptureEncryption method
	 */
	@Test
	public void performRcaptureEncryptionTest() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		testRunDetailsEntity.setMethodResponse("{\n" +
				"  \"biometrics\": [\n" +
				"    {\n" +
				"      \"specVersion\": \"\",\n" +
				"      \"data\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidGltZXN0YW1wIjoiMjAyMS0wNC0xMFQxMjoxMTowN1oiLCJ0cmFuc2FjdGlvbklkIjoiMTIzIn0.OftB11ggkTCNZzWNW1_35xQ6sLBbO1QB6LtscxNUoTQ\",\n" +
				"      \"dataDecoded\": \"dsiuidsjijdd\",    \n" +
				"      \"error\": \"\"\n" +
				"    }\n" +
				"  ]\n" +
				"}");
		Mockito.when(keyManagerHelper.getAppId()).thenReturn("abc");
		Mockito.when(keyManagerHelper.getRefId()).thenReturn("aiusi");
		SBIValidator.EncryptValidatorResponseDto encryptValidatorResponseDto = new SBIValidator.EncryptValidatorResponseDto();
		SBIValidator.EncryptValidatorResponse encryptValidatorResponse = new SBIValidator.EncryptValidatorResponse();
		encryptValidatorResponse.setData("{\n" +
				"        \"digitalId\": \"132\",\n" +
				"        \"bioType\": \"face\",\n" +
				"      }");
		encryptValidatorResponseDto.setResponse(encryptValidatorResponse);
		Mockito.when(keyManagerHelper.encryptionResponse(any(SBIValidator.EncryptValidatorRequestDto.class))).thenReturn(encryptValidatorResponseDto);
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureEncryption", testRunDetailsEntity);
	}

	@Test
	public void performRcaptureEncryptionExceptionTest() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		testRunDetailsEntity.setMethodResponse("{\n" +
				"  \"biometrics\": [\n" +
				"    {\n" +
				"      \"specVersion\": \"\",\n" +
				"      \"data\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwidGltZXN0YW1wIjoiMjAyMS0wNC0xMFQxMjoxMTowN1oiLCJ0cmFuc2FjdGlvbklkIjoiMTIzIn0.OftB11ggkTCNZzWNW1_35xQ6sLBbO1QB6LtscxNUoTQ\",\n" +
				"      \"dataDecoded\": \"dsiuidsjijdd\",    \n" +
				"      \"error\": \"\"\n" +
				"    }\n" +
				"  ]\n" +
				"}");
		Mockito.when(keyManagerHelper.getAppId()).thenReturn("abc");
		Mockito.when(keyManagerHelper.getRefId()).thenReturn("aiusi");
		SBIValidator.EncryptValidatorResponseDto encryptValidatorResponseDto = new SBIValidator.EncryptValidatorResponseDto();
		SBIValidator.EncryptValidatorResponse encryptValidatorResponse = new SBIValidator.EncryptValidatorResponse();
		encryptValidatorResponseDto.setResponse(encryptValidatorResponse);
		Mockito.when(keyManagerHelper.encryptionResponse(any(SBIValidator.EncryptValidatorRequestDto.class))).thenReturn(encryptValidatorResponseDto);
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureEncryption", testRunDetailsEntity);
	}

	/*
	 * This class tests the performRcaptureDecryption method
	 */
	@Test
	public void performRcaptureDecryptionTest() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		testRunDetailsEntity.setMethodResponse("{\n" +
				"  \"biometrics\": [\n" +
				"    {\n" +
				"      \"timestamp\": \"2021-04-10T12:11:07Z\",\n" +
				"      \"transactionId\": \"123\",\n" +
				"      \"data\": \"TDYUHU\",\n" +
				"      \"dataDecoded\": \"RTYTYT\",\n" +
				"      \"isEncrypted\": \"true\"\n" +
				"    }\n" +
				"  ]\n" +
				"}\n");
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureDecryption", testRunDetailsEntity);
		Mockito.when(keyManagerHelper.getAppId()).thenReturn("abc");
		Mockito.when(keyManagerHelper.getRefId()).thenReturn("aiusi");
		SBIValidator.DecryptValidatorRequestDto decryptValidatorRequestDto = new SBIValidator.DecryptValidatorRequestDto();
		SBIValidator.DecryptValidatorResponseDto decryptValidatorResponseDto = new SBIValidator.DecryptValidatorResponseDto();
		SBIValidator.DecryptValidatorResponse decryptValidatorResponse = new SBIValidator.DecryptValidatorResponse();
		decryptValidatorResponse.setData("ewogICAgICAgICJkaWdpdGFsSWQiOiAiMTMyIiwKICAgICAgICAiYmlvVHlwZSI6ICJmYWNlIiwKICAgICAgfQ==");
		decryptValidatorResponseDto.setResponse(decryptValidatorResponse);
		Mockito.when(keyManagerHelper.decryptionResponse(any(SBIValidator.DecryptValidatorRequestDto.class))).thenReturn(decryptValidatorResponseDto);
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureDecryption", testRunDetailsEntity);
	}

	@Test
	public void performRcaptureDecryptionExceptionTest() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
		TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
		testRunDetailsEntity.setMethodResponse("{\n" +
				"  \"biometrics\": [\n" +
				"    {\n" +
				"      \"timestamp\": \"2021-04-10T12:11:07Z\",\n" +
				"      \"transactionId\": \"123\",\n" +
				"      \"data\": \"TDYUHU\",\n" +
				"      \"dataDecoded\": \"RTYTYT\",\n" +
				"      \"isEncrypted\": \"true\"\n" +
				"    }\n" +
				"  ]\n" +
				"}\n");
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureDecryption", testRunDetailsEntity);
		Mockito.when(keyManagerHelper.getAppId()).thenReturn("abc");
		Mockito.when(keyManagerHelper.getRefId()).thenReturn("aiusi");
		SBIValidator.DecryptValidatorRequestDto decryptValidatorRequestDto = new SBIValidator.DecryptValidatorRequestDto();
		SBIValidator.DecryptValidatorResponseDto decryptValidatorResponseDto = new SBIValidator.DecryptValidatorResponseDto();
		SBIValidator.DecryptValidatorResponse decryptValidatorResponse = new SBIValidator.DecryptValidatorResponse();
		decryptValidatorResponseDto.setResponse(decryptValidatorResponse);
		Mockito.when(keyManagerHelper.decryptionResponse(any(SBIValidator.DecryptValidatorRequestDto.class))).thenReturn(decryptValidatorResponseDto);
		ReflectionTestUtils.invokeMethod(testRunService, "performRcaptureDecryption", testRunDetailsEntity);
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