package io.mosip.compliance.toolkit.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.report.SbiProjectTable;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.apache.velocity.VelocityContext;
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

        @Autowired
        private ObjectMapperConfig objectMapperConfig1;


        @Before
        public void before() {
                ReportRequestDto requestDto = new ReportRequestDto();
                requestDto.setProjectType("SBI");
                requestDto.setProjectId("kdshfksjd");
                requestDto.setCollectionId("sajdnsaldk");
                requestDto.setTestRunId("12345678");
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
        public void testCreateReportAbisDefault() throws JsonProcessingException {
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

//        @Test
//        public void testCreateReportForSbiProject() throws Exception {
//                ReportRequestDto requestDto = new ReportRequestDto();
//                requestDto.setProjectType("ABIS");
//                requestDto.setProjectId("kdshfksjd");
//                requestDto.setCollectionId("sajdnsaldk");
//                requestDto.setTestRunId("12345678");
//
//                ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse = new ResponseWrapper<>();
//                TestRunDetailsResponseDto testRunDetailsResponseDto = new TestRunDetailsResponseDto();
//                testRunDetailsResponse.setResponse(testRunDetailsResponseDto);
//                Mockito.when(testRunService.getTestRunDetails(Mockito.anyString())).thenReturn(testRunDetailsResponse);
//
//                ResponseWrapper<SbiProjectDto> sbiProjectResponse = new ResponseWrapper<>();
//                SbiProjectDto sbiProjectDto = new SbiProjectDto();
//                sbiProjectResponse.setResponse(sbiProjectDto);
//                Mockito.when(sbiProjectService.getSbiProject(Mockito.anyString())).thenReturn(sbiProjectResponse);
//
//                ResponseEntity<?> response = reportGeneratorService.createReport(requestDto, "origin");
//
//        }

}
