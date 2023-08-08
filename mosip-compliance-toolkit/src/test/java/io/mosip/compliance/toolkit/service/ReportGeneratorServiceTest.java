package io.mosip.compliance.toolkit.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class ReportGeneratorServiceTest {
        @Mock
        private TestRunDetailsResponseDto testRunDetailsResponseDto;

        @Mock
        private TestRunService testRunService;

        @Mock
        private SbiProjectService sbiProjectService;

        @Mock
        private SdkProjectService sdkProjectService;

        @Mock
        private AbisProjectService abisProjectService;

        @Mock
        private ObjectMapperConfig objectMapperConfig;

        @Mock
        private Authentication mockAuthentication;

        @Mock
        private AuthUserDetails mockAuthUserDetails;

        @Mock
        private PartnerManagerHelper partnerManagerHelper;

        @Mock
        private TestCasesService testCasesService;

        @Autowired
        private ObjectMapperConfig objectMapperConfig1;


        @Before
        public void before() {
                ReportRequestDto requestDto = new ReportRequestDto();
                requestDto.setProjectType("SBI");
                requestDto.setProjectId("kdshfksjd");
                requestDto.setCollectionId("sajdnsaldk");
                requestDto.setTestRunId("12345678");

                SecurityContext securityContext = Mockito.mock(SecurityContext.class);
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
                testRunDetailsDto.setMethodResponse(null);
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

                Mockito.when(testCasesService.getTestCaseById(Mockito.any())).thenReturn(testCaseDto);
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


}
