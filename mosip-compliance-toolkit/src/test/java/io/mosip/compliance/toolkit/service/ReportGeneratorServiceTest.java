package io.mosip.compliance.toolkit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.report.*;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.meta.When;

import static io.restassured.RestAssured.authentication;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportGeneratorServiceTest {


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



        @Before
        public void before() {
                ReportRequestDto requestDto = new ReportRequestDto();
                requestDto.setProjectType("SBI");
                requestDto.setProjectId("kdshfksjd");
                requestDto.setCollectionId("sajdnsaldk");
                requestDto.setTestRunId("12345678");

                SecurityContext securityContext = mock(SecurityContext.class);
                SecurityContextHolder.setContext(securityContext);
                Mockito.when(securityContext.getAuthentication()).thenReturn(mockAuthentication);

                String ignoreSdkTestcases = "SDK2000";
                ReflectionTestUtils.setField(reportGeneratorService, "ignoreSdkTestcases", ignoreSdkTestcases);
                String ignoreAbisTestcases = "ABIS3000";
                ReflectionTestUtils.setField(reportGeneratorService, "ignoreAbisTestcases", ignoreAbisTestcases);
        }

        @InjectMocks
        private ReportGeneratorService reportGeneratorService;

        @Test
        public void testCreateReportSBI() throws JsonProcessingException {
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

                Mockito.when(testRunService.getTestRunDetails(Mockito.any()))
                                .thenReturn(testRunDetailsResponse);
                reportGeneratorService.createReport(requestDto, "abcdefgh");
        }

        @Test
        public void testCreateReportSDK() throws JsonProcessingException {
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
                Mockito.when(testRunService.getTestRunDetails(Mockito.any()))
                                .thenReturn(testRunDetailsResponse);
                reportGeneratorService.createReport(requestDto, "abcdefgh");
        }

        @Test
        public void testCreateReportSDKDefault() throws JsonProcessingException {
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



                Mockito.when(sdkProjectService.getSdkProject(Mockito.any())).thenReturn(sdkProjectResponse);
                Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockAuthUserDetails);
                Mockito.when(testRunService.getTestRunDetails(Mockito.any()))
                        .thenReturn(testRunDetailsResponse);
                reportGeneratorService.createReport(requestDto, "abcdefgh");
        }

        @Test
        public void testCreateReportABIS() throws JsonProcessingException {
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
                Mockito.when(testRunService.getTestRunDetails(Mockito.any()))
                                .thenReturn(testRunDetailsResponse);
                reportGeneratorService.createReport(requestDto, "abcdefgh");
        }

        @Test
        public void testCreateReportAbisDefault() throws IOException {
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
                Mockito.when(testRunService.getTestRunDetails(Mockito.any()))
                        .thenReturn(testRunDetailsResponse);

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

                Mockito.when(partnerManagerHelper.getPartnerDetails(Mockito.any())).thenReturn(partnerDetailsDto);
                Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockAuthUserDetails);
                Mockito.when(abisProjectService.getAbisProject(Mockito.any())).thenReturn(abisProjectResponse);
                reportGeneratorService.createReport(requestDto, "abcdefgh");
        }
        @Test
        public void testHandleServiceErrors_WithErrors() throws Exception {
                List<ServiceError> serviceErrors = new ArrayList<>();
                serviceErrors.add(new ServiceError());
                serviceErrors.add(new ServiceError());
                ReportRequestDto reportRequestDto = new ReportRequestDto();

                ResponseEntity<Resource> result = invokeHandleServiceErrors(reportRequestDto, serviceErrors);

        }

        private ResponseEntity<Resource> invokeHandleServiceErrors(ReportRequestDto requestDto, List<ServiceError> serviceErrors)
                throws Exception {
                ReportGeneratorService reportGeneratorService = new ReportGeneratorService();
                Class<?>[] parameterTypes = { ReportRequestDto.class, List.class };
                Object[] arguments = { requestDto, serviceErrors };
                java.lang.reflect.Method privateMethod = ReportGeneratorService.class.getDeclaredMethod("handleServiceErrors", parameterTypes);
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

        private SbiProjectTable invokeGetSbiProjectDetails(SbiProjectDto projectDto, List<TestRunDetailsDto> testRunDetailsList,
        SbiProjectTable sbiProjectTable)
                throws Exception {
                ReportGeneratorService reportGeneratorService = new ReportGeneratorService();
                Class<?>[] parameterTypes = { SbiProjectDto.class, List.class, SbiProjectTable.class };
                Object[] arguments = { projectDto, testRunDetailsList, sbiProjectTable };
                java.lang.reflect.Method privateMethod = ReportGeneratorService.class.getDeclaredMethod("getSbiProjectDetails", parameterTypes);
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

                Mockito.when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
                Mockito.when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
                Mockito.when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

                String makeInResp = "MakeInResp";
                String modelInResp = "ModelInResp";
                String serialNoInResp = "SerialNoInResp";

                Mockito.when(makeNode.asText()).thenReturn(makeInResp);
                Mockito.when(modelNode.asText()).thenReturn(modelInResp);
                Mockito.when(serialNoNode.asText()).thenReturn(serialNoInResp);

                boolean bool = true;
                boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool,
                        dataNode);
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

                Mockito.when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
                Mockito.when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
                Mockito.when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

                Mockito.when(makeNode.asText()).thenReturn(null);
                Mockito.when(modelNode.asText()).thenReturn(null);
                Mockito.when(serialNoNode.asText()).thenReturn(null);

                boolean bool = true;
                boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool,
                        dataNode);
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

                Mockito.when(dataNode.get(AppConstants.DIGITAL_ID_DECODED_DATA)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.MAKE)).thenReturn(makeNode);
                Mockito.when(dataNode.get(AppConstants.MODEL)).thenReturn(modelNode);
                Mockito.when(dataNode.get(AppConstants.SERIAL_NO)).thenReturn(serialNoNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER)).thenReturn(dataNode);
                Mockito.when(dataNode.get(AppConstants.DEVICE_PROVIDER_ID)).thenReturn(dataNode);

                String makeInResp = "MakeInResp";
                String modelInResp = "ModelInResp";
                String serialNoInResp = "SerialNoInResp";

                Mockito.when(makeNode.asText()).thenReturn(makeInResp);
                Mockito.when(modelNode.asText()).thenReturn(modelInResp);
                Mockito.when(serialNoNode.asText()).thenReturn(serialNoInResp);

                boolean bool = true;
                boolean bool1 = invokeValidateDeviceMakeModelSerialNo(sbiProjectTable, bool,
                        dataNode);
        }

        private boolean invokeValidateDeviceMakeModelSerialNo(SbiProjectTable sbiProjectTable, boolean validationResult,
                                                                      JsonNode dataNode)
                throws Exception {
                ReportGeneratorService reportGeneratorService = new ReportGeneratorService();
                Class<?>[] parameterTypes = { SbiProjectTable.class, boolean.class, JsonNode.class };
                Object[] arguments = { sbiProjectTable, validationResult, dataNode };
                java.lang.reflect.Method privateMethod = ReportGeneratorService.class.getDeclaredMethod("validateDeviceMakeModelSerialNo", parameterTypes);
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
              collectionsService.getTestCasesForCollection(testRunDetailsResponseDto.getCollectionId());
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
                Mockito.when(testRunDetailsDto.getTestcaseId()).thenReturn("132");
                Mockito.when(testRunDetailsDto.getResultStatus()).thenReturn(AppConstants.SUCCESS);
                testRunDetailsList.add(testRunDetailsDto);
                testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
                ReportGeneratorService reportGeneratorService = new ReportGeneratorService();
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
                Mockito.when(testRunDetailsDto.getTestcaseId()).thenReturn("123");
                Mockito.when(testRunDetailsDto.getResultStatus()).thenReturn(AppConstants.SUCCESS);
                testRunDetailsList.add(testRunDetailsDto);
                testRunDetailsResponseDto.setTestRunDetailsList(testRunDetailsList);
                ReportGeneratorService reportGeneratorService = new ReportGeneratorService();
                int result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "countOfSuccessTestCases", testcasesList, testRunDetailsResponseDto);
                assertEquals(1, result);
        }

        @Test
        public void getTestRunStartDtTest() {
                TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
                LocalDateTime testRunStartDt = LocalDateTime.of(2023, 10, 17, 10, 30, 0);
                testRunDetailsResponseDto.setRunDtimes(testRunStartDt);

                String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getTestRunStartDt",testRunDetailsResponseDto);

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
                String result = ReflectionTestUtils.invokeMethod(reportGeneratorService, "getReportValidityDt",testRunDetailsResponseDto);
        }



        private MosipUserDto getMosipUserDto(){
                MosipUserDto mosipUserDto = new MosipUserDto();
                mosipUserDto.setUserId("123");
                mosipUserDto.setMail("abc@gmail.com");
                return mosipUserDto;
        }


}
