package io.mosip.compliance.toolkit.service;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto.ErrorDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.report.*;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryMappingEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.compliance.toolkit.repository.*;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.StringUtil;
import io.mosip.kernel.core.http.RequestWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto.Partner;


import javax.validation.constraints.AssertTrue;
import java.util.Optional;

@TestPropertySource(properties = "mosip.toolkit.api.id.partner.report.get=value")
@RunWith(MockitoJUnitRunner.class)
public class ReportServiceTest {


    @Mock
    private TestRunService testRunService;

    @Mock
    private SbiProjectService sbiProjectService;

    @Mock
    AuthUserDetails authUserDetails;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private SdkProjectService sdkProjectService;

    @Mock
    private AbisProjectService abisProjectService;

    @Mock
    private Authentication mockAuthentication;

    @Mock
    TestRunDetailsDto testRunDetailsDto;

    @Mock
    private AuthUserDetails mockAuthUserDetails;

    @Mock
    private PartnerManagerHelper partnerManagerHelper;

    @Mock
    private TestCasesService testCasesService;

    @Mock
    private CollectionsService collectionsService;

    @Mock
    ComplianceTestRunSummaryRepository complianceTestRunSummaryRepository;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ObjectMapperConfig objectMapperConfig;

    @Mock
    ComplianceTestRunSummaryPK complianceTestRunSummaryPK;

    @Mock
    SbiProjectRepository sbiProjectRepository;

    @Mock
    SdkProjectRepository sdkProjectRepository;

    @Mock
    AbisProjectRepository abisProjectRepository;

    @Mock
    CollectionsRepository collectionsRepository;

    @Mock
    ResourceCacheService resourceCacheService;

    @Mock
    BiometricScoresService biometricScoresService;


    @Before
    public void before() {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");

        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        String ignoreSdkTestcases = "SDK2000";
        ReflectionTestUtils.setField(reportGeneratorService, "ignoreTestDataSourceForSdkTestcases", ignoreSdkTestcases);
        String ignoreAbisTestcases = "ABIS3000";
        ReflectionTestUtils.setField(reportGeneratorService, "ignoreTestDataSourceForAbisTestcases", ignoreAbisTestcases);
    }

    @InjectMocks
    private ReportService reportGeneratorService;

    @Test
    public void testgenerateDraftReportSBI() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse("method response");
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SBI1000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setReportStatus("");
        SecurityContextHolder.setContext(securityContext);
        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftReportSBI1() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        testRunDetailsResponseDto1.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto1.setExecutionDtimes(LocalDateTime.now());
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodId("123");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse("{\"biometrics\":\"Fingerprint\"}");
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SBI1000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setReportStatus("draft");

        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode element = mapper.createObjectNode();
        ObjectNode value1 = mapper.createObjectNode();
        value1.put("make", "abc");
        value1.put("model", "aisak");
        value1.put("serialNo", "aisak");
        value1.put("deviceProvider", "aisaasdk");
        value1.put("deviceProviderId", "aaesrdtyisak");
        element.put("deviceInfoDecoded", value1);
        element.put("digitalIdDecoded", value1);
        element.put("dataDecoded", value1);
        arrayNode.add(element);
        ObjectNode methodResponse = mapper.createObjectNode();
        ArrayNode arrayNode1 = mapper.createArrayNode();
        arrayNode1.add(element);
        methodResponse.put("biometrics", arrayNode1);

        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        when(objectMapper.readValue(anyString(), Mockito.eq(ArrayNode.class))).thenReturn(arrayNode);
        when(objectMapper.readValue(anyString(), Mockito.eq(ObjectNode.class))).thenReturn(methodResponse);
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("123");
        responseWrapper.setResponse(sbiProjectDto);
        when(sbiProjectService.getSbiProject(anyString())).thenReturn(responseWrapper);
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("1234");
        ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper1 = new ResponseWrapper<>();
        CollectionTestCasesResponseDto collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
        List<TestCaseDto> testCaseDtos = new ArrayList<>();
        collectionTestCasesResponseDto.setTestcases(testCaseDtos);
        responseWrapper1.setResponse(collectionTestCasesResponseDto);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        when(collectionsRepository.getCollectionNameById(anyString(), anyString())).thenReturn("abc");
        when(resourceCacheService.getOrgName(anyString())).thenReturn("abcd");
        Mockito.when(complianceTestRunSummaryRepository.findById(any())).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        SecurityContextHolder.setContext(securityContext);
        when(testRunService.getTestRunDetails(any(), any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);
        ResponseWrapper<TestRunDetailsDto> responseWrapper2 = new ResponseWrapper<>();
        responseWrapper2.setId("123");
        responseWrapper2.setResponse(testRunDetailsDto);
        when(testRunService.getMethodDetails(anyString(), anyString(), anyString(), anyString())).thenReturn(responseWrapper2);
        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftReportSDK() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SDK");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse(null);
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SDK2000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);

        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftReportSDKDefault() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SDK");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("MOSIP_DEFAULT");
        testRunDetailsDto.setMethodId("123");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse(null);
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SDK2000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);

        ResponseWrapper<SdkProjectDto> sdkProjectResponse = new ResponseWrapper<>();
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setName("SDK Project");
        sdkProjectDto.setProjectType("SDK");
        sdkProjectDto.setId("qwwqwqwqw");
        sdkProjectDto.setPurpose("Reg");
        sdkProjectDto.setSdkVersion("0.9.5");
        sdkProjectDto.setWebsiteUrl("https://");
        sdkProjectResponse.setResponse(sdkProjectDto);


        Mockito.when(testRunService.getTestRunDetails(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(testRunDetailsResponse);
        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftReportABIS() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("ABIS");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse(null);
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("ABIS3000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(testRunService.getTestRunDetails(Mockito.any(), Mockito.any(), Mockito.anyBoolean()))
                .thenReturn(testRunDetailsResponse);
        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftReportAbisDefault() throws IOException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("ABIS");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("MOSIP_DEFAULT");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse(null);
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("ABIS3000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponseDto1.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto1.setExecutionDtimes(LocalDateTime.now().plusMinutes(4));
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        Mockito.when(testRunService.getTestRunDetails(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);

        ResponseWrapper<AbisProjectDto> abisProjectResponse = new ResponseWrapper<>();
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setAbisHash("fdgfdgfdgfd");
        abisProjectDto.setAbisVersion("0.9.5");
        abisProjectDto.setId("wqeqweqwe");
        abisProjectDto.setName("Abis project");
        abisProjectDto.setWebsiteUrl("https://");
        abisProjectResponse.setResponse(abisProjectDto);
        abisProjectResponse.setErrors(null);

        PartnerDetailsDto partnerDetailsDto = new PartnerDetailsDto();
        partnerDetailsDto.setMetadata(null);
        partnerDetailsDto.setVersion("0.9.5");
        partnerDetailsDto.setId("12345678");
        partnerDetailsDto.setResponsetime(LocalDateTime.now().toString());
        List errorList = new ArrayList<>();
        partnerDetailsDto.setErrors(errorList);
        PartnerDetailsDto.Partner partner = new PartnerDetailsDto.Partner();
        partner.setContactNumber("9898989898");
        partner.setStatus("success");
        partner.setPartnerID("P1");
        partner.setPartnerType("ABIS");
        partner.setAddress("India");
        partner.setEmailId("abispartner@mosip.com");
        partner.setOrganizationName("Cyberpwn");
        partnerDetailsDto.setResponse(partner);

        ResponseWrapper<TestCaseDto> testCaseDto = new ResponseWrapper<>();
        TestCaseDto caseDto = new TestCaseDto();
        caseDto.setTestName("Abistestcase");
        caseDto.setTestCaseType("ABIS");
        testCaseDto.setResponse(caseDto);

        reportGeneratorService.generateDraftReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportNotSbi() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");

        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportNullProjectId() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId(null);
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");

        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportErr() throws JsonProcessingException {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("123");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        List<ServiceError> serviceErrors = new ArrayList<>();
        ServiceError error = new ServiceError();
        serviceErrors.add(error);
        testRunDetailsResponse.setErrors(serviceErrors);
        when(testRunService.getTestRunDetails(any(), any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);
        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportFinger() throws Exception {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        testRunDetailsResponseDto1.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto1.setExecutionDtimes(LocalDateTime.now());
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodId("123");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse("{\"biometrics\":\"Fingerprint\"}");
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SBI1000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setReportStatus("draft");

        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode element = mapper.createObjectNode();
        ObjectNode value1 = mapper.createObjectNode();
        value1.put("make", "abc");
        value1.put("model", "aisak");
        value1.put("serialNo", "aisak");
        value1.put("deviceProvider", "aisaasdk");
        value1.put("deviceProviderId", "aaesrdtyisak");
        element.put("deviceInfoDecoded", value1);
        element.put("digitalIdDecoded", value1);
        element.put("dataDecoded", value1);
        arrayNode.add(element);
        ObjectNode methodResponse = mapper.createObjectNode();
        ArrayNode arrayNode1 = mapper.createArrayNode();
        arrayNode1.add(element);
        methodResponse.put("biometrics", arrayNode1);

        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        when(objectMapper.readValue(anyString(), Mockito.eq(ArrayNode.class))).thenReturn(arrayNode);
        when(objectMapper.readValue(anyString(), Mockito.eq(ObjectNode.class))).thenReturn(methodResponse);
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("123");
        sbiProjectDto.setDeviceType("Finger");
        responseWrapper.setResponse(sbiProjectDto);
        when(sbiProjectService.getSbiProject(anyString())).thenReturn(responseWrapper);
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("1234");
        ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper1 = new ResponseWrapper<>();
        CollectionTestCasesResponseDto collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
        List<TestCaseDto> testCaseDtos = new ArrayList<>();
        collectionTestCasesResponseDto.setTestcases(testCaseDtos);
        responseWrapper1.setResponse(collectionTestCasesResponseDto);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        when(collectionsRepository.getCollectionNameById(anyString(), anyString())).thenReturn("abc");
        when(resourceCacheService.getOrgName(anyString())).thenReturn("abcd");
        Mockito.when(complianceTestRunSummaryRepository.findById(any())).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        SecurityContextHolder.setContext(securityContext);
        when(testRunService.getTestRunDetails(any(), any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);
        List<BiometricScores> biometricScoresList = new ArrayList<>();
        BiometricScores biometricScores = new BiometricScores();
        biometricScoresList.add(biometricScores);
        when(biometricScoresService.getFingerBiometricScoresList(anyString(), anyString(),
                anyString())).thenReturn(biometricScoresList);
        ResponseWrapper<TestRunDetailsDto> responseWrapper2 = new ResponseWrapper<>();
        responseWrapper2.setId("123");
        responseWrapper2.setResponse(testRunDetailsDto);
        when(testRunService.getMethodDetails(anyString(), anyString(), anyString(), anyString())).thenReturn(responseWrapper2);
        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportFace() throws Exception {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        testRunDetailsResponseDto1.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto1.setExecutionDtimes(LocalDateTime.now());
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodId("123");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse("{\"biometrics\":\"Fingerprint\"}");
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SBI1000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setReportStatus("draft");

        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode element = mapper.createObjectNode();
        ObjectNode value1 = mapper.createObjectNode();
        value1.put("make", "abc");
        value1.put("model", "aisak");
        value1.put("serialNo", "aisak");
        value1.put("deviceProvider", "aisaasdk");
        value1.put("deviceProviderId", "aaesrdtyisak");
        element.put("deviceInfoDecoded", value1);
        element.put("digitalIdDecoded", value1);
        element.put("dataDecoded", value1);
        arrayNode.add(element);
        ObjectNode methodResponse = mapper.createObjectNode();
        ArrayNode arrayNode1 = mapper.createArrayNode();
        arrayNode1.add(element);
        methodResponse.put("biometrics", arrayNode1);

        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        when(objectMapper.readValue(anyString(), Mockito.eq(ArrayNode.class))).thenReturn(arrayNode);
        when(objectMapper.readValue(anyString(), Mockito.eq(ObjectNode.class))).thenReturn(methodResponse);
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("123");
        sbiProjectDto.setDeviceType("Face");
        responseWrapper.setResponse(sbiProjectDto);
        when(sbiProjectService.getSbiProject(anyString())).thenReturn(responseWrapper);
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("1234");
        ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper1 = new ResponseWrapper<>();
        CollectionTestCasesResponseDto collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
        List<TestCaseDto> testCaseDtos = new ArrayList<>();
        collectionTestCasesResponseDto.setTestcases(testCaseDtos);
        responseWrapper1.setResponse(collectionTestCasesResponseDto);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        when(collectionsRepository.getCollectionNameById(anyString(), anyString())).thenReturn("abc");
        when(resourceCacheService.getOrgName(anyString())).thenReturn("abcd");
        Mockito.when(complianceTestRunSummaryRepository.findById(any())).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        SecurityContextHolder.setContext(securityContext);
        when(testRunService.getTestRunDetails(any(), any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);
        List<BiometricScores> biometricScoresList = new ArrayList<>();
        BiometricScores biometricScores = new BiometricScores();
        biometricScoresList.add(biometricScores);
        when(biometricScoresService.getFaceBiometricScoresList(anyString(), anyString(),
                anyString())).thenReturn(biometricScoresList);
        ResponseWrapper<TestRunDetailsDto> responseWrapper2 = new ResponseWrapper<>();
        responseWrapper2.setId("123");
        responseWrapper2.setResponse(testRunDetailsDto);
        when(testRunService.getMethodDetails(anyString(), anyString(), anyString(), anyString())).thenReturn(responseWrapper2);
        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void testgenerateDraftQAReportIris() throws Exception {
        ReportRequestDto requestDto = new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<TestRunDetailsResponseDto>();
        TestRunDetailsResponseDto testRunDetailsResponseDto1 = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto1.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto1.setRunId("ksjdkjdhaskj");
        testRunDetailsResponseDto1.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto1.setExecutionDtimes(LocalDateTime.now());
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("sjadskajddk");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodId("123");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse("{\"biometrics\":\"Fingerprint\"}");
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("SBI1000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto1.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponse.setResponse(testRunDetailsResponseDto1);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setReportStatus("draft");

        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        ObjectNode element = mapper.createObjectNode();
        ObjectNode value1 = mapper.createObjectNode();
        value1.put("make", "abc");
        value1.put("model", "aisak");
        value1.put("serialNo", "aisak");
        value1.put("deviceProvider", "aisaasdk");
        value1.put("deviceProviderId", "aaesrdtyisak");
        element.put("deviceInfoDecoded", value1);
        element.put("digitalIdDecoded", value1);
        element.put("dataDecoded", value1);
        arrayNode.add(element);
        ObjectNode methodResponse = mapper.createObjectNode();
        ArrayNode arrayNode1 = mapper.createArrayNode();
        arrayNode1.add(element);
        methodResponse.put("biometrics", arrayNode1);

        ObjectMapper objectMapper = objectMapperConfig.objectMapper();
        when(objectMapper.readValue(anyString(), Mockito.eq(ArrayNode.class))).thenReturn(arrayNode);
        when(objectMapper.readValue(anyString(), Mockito.eq(ObjectNode.class))).thenReturn(methodResponse);
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("123");
        sbiProjectDto.setDeviceType("Iris");
        responseWrapper.setResponse(sbiProjectDto);
        when(sbiProjectService.getSbiProject(anyString())).thenReturn(responseWrapper);
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("1234");
        ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper1 = new ResponseWrapper<>();
        CollectionTestCasesResponseDto collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
        List<TestCaseDto> testCaseDtos = new ArrayList<>();
        collectionTestCasesResponseDto.setTestcases(testCaseDtos);
        responseWrapper1.setResponse(collectionTestCasesResponseDto);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper1);
        when(collectionsRepository.getCollectionNameById(anyString(), anyString())).thenReturn("abc");
        when(resourceCacheService.getOrgName(anyString())).thenReturn("abcd");
        Mockito.when(complianceTestRunSummaryRepository.findById(any())).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        SecurityContextHolder.setContext(securityContext);
        when(testRunService.getTestRunDetails(any(), any(), Mockito.anyBoolean())).thenReturn(testRunDetailsResponse);
        List<BiometricScores> biometricScoresList = new ArrayList<>();
        BiometricScores biometricScores = new BiometricScores();
        biometricScoresList.add(biometricScores);
        when(biometricScoresService.getIrisBiometricScoresList(anyString(), anyString(),
                anyString())).thenReturn(biometricScoresList);
        ResponseWrapper<TestRunDetailsDto> responseWrapper2 = new ResponseWrapper<>();
        responseWrapper2.setId("123");
        responseWrapper2.setResponse(testRunDetailsDto);
        when(testRunService.getMethodDetails(anyString(), anyString(), anyString(), anyString())).thenReturn(responseWrapper2);
        reportGeneratorService.generateDraftQAReport(requestDto, "abcdefgh");
    }

    @Test
    public void getPartnerDetailsTest() throws Exception {
        PartnerDetailsDto partnerDetailsDto = new PartnerDetailsDto();
        List<ErrorDto> errors = new ArrayList<>();
        partnerDetailsDto.setErrors(errors);
        Partner partner = new Partner();
        partner.setOrganizationName("abc");
        partner.setAddress("abc");
        partner.setEmailId("abc@gamil.com");
        partner.setContactNumber("12345678987");
        partnerDetailsDto.setResponse(partner);
        when(partnerManagerHelper.getPartnerDetails(anyString())).thenReturn(partnerDetailsDto);
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "getPartnerDetails", "123");
    }

    @Test
    public void validateTestDataSourceTestSdk() throws Exception {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsDtoList);
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "validateTestDataSource", testRunDetailsResponseDto, "SDK");
    }

    @Test
    public void validateTestDataSourceTestAbis() throws Exception {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsDtoList);
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "validateTestDataSource", testRunDetailsResponseDto, "ABIS");
    }

    @Test(expected = Exception.class)
    public void isReportAlreadySubmittedException() throws Exception {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        reportGeneratorService.isReportAlreadySubmitted(requestWrapper);
    }

    @Test
    public void testHandleServiceErrors_WithErrors() throws Exception {
        List<ServiceError> serviceErrors = new ArrayList<>();
        serviceErrors.add(new ServiceError());
        serviceErrors.add(new ServiceError());
        ReportRequestDto reportRequestDto = new ReportRequestDto();

        ResponseEntity<Resource> result = invokeHandleServiceErrors(reportRequestDto, serviceErrors);

    }

    private ResponseEntity<Resource> invokeHandleServiceErrors(ReportRequestDto requestDto, List<ServiceError> serviceErrors) throws Exception {
        ReportService reportGeneratorService = new ReportService();
        Class<?>[] parameterTypes = {ReportRequestDto.class, List.class};
        Object[] arguments = {requestDto, serviceErrors};
        java.lang.reflect.Method privateMethod = ReportService.class.getDeclaredMethod("handleServiceErrors", parameterTypes);
        privateMethod.setAccessible(true);
        return (ResponseEntity<Resource>) privateMethod.invoke(reportGeneratorService, arguments);
    }

    @Test
    public void testGetSbiProjectDetails() throws Exception {
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setName("SBI Project");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setSbiHash("wqeweqeqeewqewqewq");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setWebsiteUrl("https://");

        List<TestRunDetailsDto> testRunDetailsList = new ArrayList<>();

        SbiProjectTable sbiProjectTable = new SbiProjectTable();
        SbiProjectTable sbiProjectTable1 = new SbiProjectTable();

        sbiProjectTable1 = invokeGetSbiProjectDetails(sbiProjectDto, testRunDetailsList, sbiProjectTable);
    }

    private SbiProjectTable invokeGetSbiProjectDetails(SbiProjectDto projectDto, List<TestRunDetailsDto> testRunDetailsList, SbiProjectTable sbiProjectTable) throws Exception {
        ReportService reportGeneratorService = new ReportService();
        Class<?>[] parameterTypes = {SbiProjectDto.class, List.class, SbiProjectTable.class};
        Object[] arguments = {projectDto, testRunDetailsList, sbiProjectTable};
        java.lang.reflect.Method privateMethod = ReportService.class.getDeclaredMethod("getSbiProjectDetails", parameterTypes);
        privateMethod.setAccessible(true);
        return (SbiProjectTable) privateMethod.invoke(reportGeneratorService, arguments);
    }

    @Test
    public void testValidateDeviceMakeModelSerialNo() throws Exception {
        SbiProjectTable sbiProjectTable = new SbiProjectTable();
        JsonNode dataNode = mock(JsonNode.class);
        JsonNode makeNode = Mockito.mock(JsonNode.class);
        JsonNode modelNode = Mockito.mock(JsonNode.class);
        JsonNode serialNoNode = Mockito.mock(JsonNode.class);

        when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
        when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
        when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

        String makeInResp = "MakeInResp";
        String modelInResp = "ModelInResp";
        String serialNoInResp = "SerialNoInResp";

        when(makeNode.asText()).thenReturn(makeInResp);
        when(modelNode.asText()).thenReturn(modelInResp);
        when(serialNoNode.asText()).thenReturn(serialNoInResp);

        boolean bool = true;
        boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool, dataNode);
    }

    @Test
    public void testValidateDeviceMakeModelSerialNoException() throws Exception {
        SbiProjectTable sbiProjectTable = new SbiProjectTable();
        sbiProjectTable.setDeviceMake("BioDevices");
        sbiProjectTable.setDeviceModel("FINGER1212");
        sbiProjectTable.setDeviceSerialNo("1212121");
        JsonNode dataNode = mock(JsonNode.class);
        JsonNode makeNode = Mockito.mock(JsonNode.class);
        JsonNode modelNode = Mockito.mock(JsonNode.class);
        JsonNode serialNoNode = Mockito.mock(JsonNode.class);

        when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
        when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
        when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

        when(makeNode.asText()).thenReturn(null);
        when(modelNode.asText()).thenReturn(null);
        when(serialNoNode.asText()).thenReturn(null);

        boolean bool = true;
        boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool, dataNode);
    }

    @Test
    public void testValidateDeviceMakeModelSerialNoException2() throws Exception {
        SbiProjectTable sbiProjectTable = new SbiProjectTable();
        sbiProjectTable.setDeviceMake("BioDevices");
        sbiProjectTable.setDeviceModel("FINGER1212");
        sbiProjectTable.setDeviceSerialNo("1212121");
        JsonNode dataNode = mock(JsonNode.class);
        JsonNode makeNode = Mockito.mock(JsonNode.class);
        JsonNode modelNode = Mockito.mock(JsonNode.class);
        JsonNode serialNoNode = Mockito.mock(JsonNode.class);

        when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
        when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
        when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
        when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

        String makeInResp = "MakeInResp";
        String modelInResp = "ModelInResp";
        String serialNoInResp = "SerialNoInResp";

        when(makeNode.asText()).thenReturn(makeInResp);
        when(modelNode.asText()).thenReturn(modelInResp);
        when(serialNoNode.asText()).thenReturn(serialNoInResp);

        boolean bool = true;
        boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool, dataNode);
    }

    private boolean invokeValidateDeviceMakeModelSerialNo(SbiProjectTable sbiProjectTable, boolean validationResult, JsonNode dataNode) throws Exception {
        ReportService reportGeneratorService = new ReportService();
        Class<?>[] parameterTypes = {SbiProjectTable.class, boolean.class, JsonNode.class};
        Object[] arguments = {sbiProjectTable, validationResult, dataNode};
        java.lang.reflect.Method privateMethod = ReportService.class.getDeclaredMethod("validateDeviceMakeModelSerialNo", parameterTypes);
        privateMethod.setAccessible(true);
        return (boolean) privateMethod.invoke(reportGeneratorService, arguments);
    }

    @Test
    public void getTestRunExecutionTimeTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setRunId("abc");
        testRunDetailsResponseDto.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto.setExecutionDtimes(LocalDateTime.now());
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "getTestRunExecutionTime", testRunDetailsResponseDto);
    }

    @Test(expected = Exception.class)
    public void getTotalTestcasesTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setRunId("abc");
        testRunDetailsResponseDto.setCollectionId("123");
        collectionsService.getTestCasesForCollection(Mockito.any(), testRunDetailsResponseDto.getCollectionId());
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "getTotalTestcases", testRunDetailsResponseDto);
    }

    @Test(expected = Exception.class)
    public void getCountOfPassedTestCasesTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("lkdjskdjsaldks");
        testRunDetailsResponseDto.setRunId("ksjdkjdhaskj");
        List<TestRunDetailsDto> testRunDetailsDtoList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = new TestRunDetailsDto();
        testRunDetailsDto.setResultStatus("Success");
        testRunDetailsDto.setRunId("kjdfhkfdjhskjd");
        testRunDetailsDto.setTestDataSource("MOSIP_DEFAULT");
        testRunDetailsDto.setMethodUrl("https://");
        testRunDetailsDto.setMethodRequest(null);
        testRunDetailsDto.setMethodResponse(null);
        testRunDetailsDto.setResultDescription("Test Run successful");
        testRunDetailsDto.setTestcaseId("ABIS3000");
        testRunDetailsDtoList.add(testRunDetailsDto);
        testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsDtoList);
        testRunDetailsResponseDto.setRunDtimes(LocalDateTime.now());
        testRunDetailsResponseDto.setExecutionDtimes(LocalDateTime.now().plusMinutes(4));
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "getCountOfPassedTestCases", testRunDetailsResponseDto);
        ReflectionTestUtils.invokeMethod(reportGeneratorService, "getCountOfFailedTestCases", testRunDetailsResponseDto);
    }

    @Test
    public void populateTestRunTableTest() {
        List<TestCaseDto> testcasesList = new ArrayList<>();
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("132");
        testCaseDto.setTestName("iufewhfi");
        testcasesList.add(testCaseDto);
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        List<TestRunDetailsDto> testRunDetailsList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = Mockito.mock(TestRunDetailsDto.class);
        when(testRunDetailsDto.getTestcaseId()).thenReturn("132");
        when(testRunDetailsDto.getResultStatus()).thenReturn(AppConstants.SUCCESS);
        testRunDetailsList.add(testRunDetailsDto);
        testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
        ReportService reportGeneratorService = new ReportService();
        List<TestRunTable> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "populateTestRunTable", testcasesList, testRunDetailsResponseDto);
    }

    @Test
    public void countOfSuccessTestCasesTest() {
        List<TestCaseDto> testcasesList = new ArrayList<>();
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("123");
        testcasesList.add(testCaseDto);

        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        List<TestRunDetailsDto> testRunDetailsList = new ArrayList<>();
        TestRunDetailsDto testRunDetailsDto = Mockito.mock(TestRunDetailsDto.class);
        when(testRunDetailsDto.getTestcaseId()).thenReturn("123");
        when(testRunDetailsDto.getResultStatus()).thenReturn(AppConstants.SUCCESS);
        testRunDetailsList.add(testRunDetailsDto);
        testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
        ReportService reportGeneratorService = new ReportService();
        int result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "countOfSuccessTestCases", testcasesList, testRunDetailsResponseDto);
        assertEquals(1, result);
    }

    @Test
    public void getTestRunStartDtTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        LocalDateTime testRunStartDt = LocalDateTime.of(2023, 10, 17, 10, 30, 0);
        testRunDetailsResponseDto.setRunDtimes(testRunStartDt);

        String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getTestRunStartDt", testRunDetailsResponseDto);

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
        String expected = formatter.format(testRunStartDt);
        assertEquals(expected, result);
    }

    @Test
    public void getReportValidityDtTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        LocalDateTime testRunStartDt = LocalDateTime.of(2023, 10, 17, 10, 30, 0);
        testRunDetailsResponseDto.setRunDtimes(testRunStartDt);
        int reportExpiryPeriod = 6;
        String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getReportValidityDt", testRunDetailsResponseDto);
    }

    @Test
    public void getSdkProjectDetailsTest() {
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setName("sdk");
        sdkProjectDto.setProjectType("SDK");
        sdkProjectDto.setPurpose("check quality");
        sdkProjectDto.setSdkVersion("1.0");
        sdkProjectDto.setSdkHash("sdf");
        sdkProjectDto.setWebsiteUrl("test.com");
        SdkProjectTable result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSdkProjectDetails", sdkProjectDto);
        assertThat(result, instanceOf(SdkProjectTable.class));
    }

    @Test
    public void getAbisProjectDetailsTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType("ABIS");
        abisProjectDto.setName("abis");
        abisProjectDto.setAbisVersion("1.0");
        abisProjectDto.setAbisHash("aygc");
        abisProjectDto.setWebsiteUrl("test.com");
        AbisProjectTable result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getAbisProjectDetails", abisProjectDto);
        assertThat(result, instanceOf(AbisProjectTable.class));
    }

    @Test
    public void isReportAlreadySubmittedTestFalse() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setProjectType("SBI");
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.setField(reportGeneratorService, "getPartnerReportId", "mockedPartnerReportId");

        ResponseWrapper<Boolean> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "isReportAlreadySubmitted", requestWrapper);
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test
    public void isReportAlreadySubmittedTestTrue() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setReportStatus("teyrugt");
        complianceTestRunSummaryEntity.setProjectType("SBI");
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.setField(reportGeneratorService, "getPartnerReportId", "mockedPartnerReportId");

        ResponseWrapper<Boolean> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "isReportAlreadySubmitted", requestWrapper);
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test(expected = Exception.class)
    public void isReportAlreadySubmittedTestException() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setProjectType("SBI");
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");

        ResponseWrapper<Boolean> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "isReportAlreadySubmitted", requestWrapper);
    }

    @Test(expected = Exception.class)
    public void getReportListTest() {
        boolean isAdmin = false;
        ComplianceTestRunSummaryMappingEntity complianceTestRunSummaryMappingEntity = new ComplianceTestRunSummaryMappingEntity();
        complianceTestRunSummaryMappingEntity.setReportStatus("teyrugt");
        complianceTestRunSummaryMappingEntity.setProjectType("SBI");
        List<ComplianceTestRunSummaryMappingEntity> listEntity = new ArrayList<>();
        listEntity.add(complianceTestRunSummaryMappingEntity);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(complianceTestRunSummaryRepository.findAllBySubmittedReportsPartnerId(anyString())).thenReturn(listEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        ComplianceTestRunSummaryDto complianceTestRunSummaryDto = new ComplianceTestRunSummaryDto();
        complianceTestRunSummaryDto.setCollectionId("123");
        ResponseWrapper<Boolean> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getReportList", isAdmin, "draft");
    }

    @Test
    public void getProjectNameSbiTest() {
        ComplianceTestRunSummaryMappingEntity respEntity = new ComplianceTestRunSummaryMappingEntity();
        respEntity.setReportStatus("teyrugt");
        respEntity.setProjectType("SBI");
        when(sbiProjectRepository.getProjectNameById(respEntity.getProjectId(), respEntity.getPartnerId())).thenReturn("yasud");
        String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getProjectName", respEntity);
        assertThat(result, instanceOf(String.class));
    }

    @Test
    public void getProjectNameSdkTest() {
        ComplianceTestRunSummaryMappingEntity respEntity = new ComplianceTestRunSummaryMappingEntity();
        respEntity.setReportStatus("teyrugt");
        respEntity.setProjectType("SDK");
        when(sdkProjectRepository.getProjectNameById(respEntity.getProjectId(), respEntity.getPartnerId())).thenReturn("yasud");
        String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getProjectName", respEntity);
        assertThat(result, instanceOf(String.class));
    }

    @Test
    public void getProjectNameAbisTest() {
        ComplianceTestRunSummaryMappingEntity respEntity = new ComplianceTestRunSummaryMappingEntity();
        respEntity.setReportStatus("teyrugt");
        respEntity.setProjectType("ABIS");
        when(abisProjectRepository.getProjectNameById(respEntity.getProjectId(), respEntity.getPartnerId())).thenReturn("yasud");
        String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getProjectName", respEntity);
        assertThat(result, instanceOf(String.class));
    }


    @Test
    public void getSubmittedReportTest() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, true);
        assertThat(result, instanceOf(ResponseEntity.class));
    }

    @Test
    public void getSubmittedReportTest1() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("review");
        complianceTestRunSummaryEntity.setReportDataJson("fagf");
        ReportDataDto reportDataDto = new ReportDataDto();
        reportDataDto.setProjectType("SBI");
        reportDataDto.setOrigin("origin");
        PartnerTable partnerTable = new PartnerTable();
        reportDataDto.setPartnerDetails(partnerTable);
        SbiProjectTable sbiProjectTable = new SbiProjectTable();
        reportDataDto.setSbiProjectDetailsTable(sbiProjectTable);
        reportDataDto.setCollectionName("abc");
        reportDataDto.setTestRunStartTime("26378");
        reportDataDto.setReportExpiryPeriod(3);
        reportDataDto.setReportValidityDate("12");
        List<TestRunTable> testRunTable = new ArrayList<>();
        reportDataDto.setTestRunDetailsList(testRunTable);
        reportDataDto.setTimeTakenByTestRun("123");
        reportDataDto.setTotalTestCasesCount(12);
        reportDataDto.setCountOfFailedTestCases(1);
        reportDataDto.setCountOfPassedTestCases(11);
        List<BiometricScores> biometricScoresList = new ArrayList<>();
        reportDataDto.setBiometricScores(biometricScoresList);
        reportDataDto.setBiometricType("Face");
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        when(objectMapper.readValue(anyString(), eq(ReportDataDto.class))).thenReturn(reportDataDto);
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseWrapper<CollectionDto> responseWrapper = new ResponseWrapper<>();
        CollectionDto collectionDto = new CollectionDto();
        collectionDto.setCollectionType("quality_assessment_collection");
        responseWrapper.setResponse(collectionDto);
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, false);
        assertThat(result, instanceOf(ResponseEntity.class));
    }

    @Test
    public void getSubmittedReportTestException() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, true);
    }

    @Test
    public void getSubmittedReportSbiTest() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("doiwfjsd");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        ReportDataDto reportDataDto = new ReportDataDto();
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        Mockito.when(objectMapper.readValue(anyString(), eq(ReportDataDto.class))).thenReturn(reportDataDto);
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, true);
        assertThat(result, instanceOf(ResponseEntity.class));
    }

    @Test
    public void getSubmittedReportSdkTest() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SDK");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("drsdaft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        ReportDataDto reportDataDto = new ReportDataDto();
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        Mockito.when(objectMapper.readValue(anyString(), eq(ReportDataDto.class))).thenReturn(reportDataDto);
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, true);
        assertThat(result, instanceOf(ResponseEntity.class));
    }

    @Test
    public void getSubmittedReportabisTest() throws JsonProcessingException {
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("ABIS");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("draqweft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        ReportDataDto reportDataDto = new ReportDataDto();
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        Mockito.when(objectMapper.readValue(anyString(), eq(ReportDataDto.class))).thenReturn(reportDataDto);
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseEntity result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getSubmittedReport", "abc", reportRequestDto, true);
        assertThat(result, instanceOf(ResponseEntity.class));
    }

    @Test
    public void updateReportStatusTest() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        MosipUserDto mosipUserDto = getMosipUserDto();
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(complianceTestRunSummaryRepository.save(complianceTestRunSummaryEntity)).thenReturn(complianceTestRunSummaryEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        ComplianceTestRunSummaryDto complianceTestRunSummaryDto = new ComplianceTestRunSummaryDto();
        ComplianceTestRunSummaryEntity respEntity = mock(ComplianceTestRunSummaryEntity.class);
        ResponseWrapper<ComplianceTestRunSummaryDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "updateReportStatus", "abc", requestWrapper, "draft", "approved");
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test
    public void updateReportStatusTestReview() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        ComplianceTestRunSummaryDto complianceTestRunSummaryDto = new ComplianceTestRunSummaryDto();
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseWrapper<ComplianceTestRunSummaryDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "updateReportStatus", "abc", requestWrapper, "draft", "review");
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test
    public void updateReportStatusTestError() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12");
        complianceTestRunSummaryEntity.setReportStatus("driuydaft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseWrapper<ComplianceTestRunSummaryDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "updateReportStatus", "abc", requestWrapper, "draft", "review");
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test
    public void updateReportStatusTesterror2() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBsfI");
        reportRequestDto.setTestRunId("12");
        reportRequestDto.setProjectId("12374");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setCollectionId("263");
        requestWrapper.setRequest(reportRequestDto);
        ComplianceTestRunSummaryEntity complianceTestRunSummaryEntity = new ComplianceTestRunSummaryEntity();
        complianceTestRunSummaryEntity.setProjectType("SBI");
        complianceTestRunSummaryEntity.setRunId("12234");
        complianceTestRunSummaryEntity.setReportStatus("draft");
        complianceTestRunSummaryEntity.setReportDataJson("asfg");
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        ComplianceTestRunSummaryDto complianceTestRunSummaryDto = new ComplianceTestRunSummaryDto();
        when(complianceTestRunSummaryRepository.findById(any(ComplianceTestRunSummaryPK.class))).thenReturn(Optional.of(complianceTestRunSummaryEntity));
        ResponseWrapper<ComplianceTestRunSummaryDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "updateReportStatus", "abc", requestWrapper, "draft", "review");
        assertThat(result, instanceOf(ResponseWrapper.class));
    }

    @Test
    public void updateReportStatusTestException() {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        ResponseWrapper<ComplianceTestRunSummaryDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "updateReportStatus", "abc", requestWrapper, "draft", "review");
    }

    @Test
    public void getAllTestcasesTest() {
        TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
        testRunDetailsResponseDto.setCollectionId("1234");
        ResponseWrapper<CollectionTestCasesResponseDto> responseWrapper = new ResponseWrapper<>();
        CollectionTestCasesResponseDto collectionTestCasesResponseDto = new CollectionTestCasesResponseDto();
        List<TestCaseDto> testCaseDtos = new ArrayList<>();
        collectionTestCasesResponseDto.setTestcases(testCaseDtos);
        responseWrapper.setResponse(collectionTestCasesResponseDto);
        Mockito.when(collectionsService.getTestCasesForCollection(anyString(), anyString())).thenReturn(responseWrapper);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        List<TestCaseDto> result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getAllTestcases", testRunDetailsResponseDto);
        assertThat(result, instanceOf(List.class));
    }

    private MosipUserDto getMosipUserDto() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }

}
