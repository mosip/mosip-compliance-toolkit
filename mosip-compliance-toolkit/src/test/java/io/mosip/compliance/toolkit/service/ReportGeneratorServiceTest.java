package io.mosip.compliance.toolkit.service;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(MockitoJUnitRunner.class)
public class ReportGeneratorServiceTest {
    @Mock
    private TestRunDetailsResponseDto testRunDetailsResponseDto;

    @Mock
    private TestRunService testRunService;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Autowired
    private ObjectMapperConfig objectMapperConfig1;

    @Before
    public void before(){
        ReportRequestDto requestDto =new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
    }

    @InjectMocks
    private ReportGeneratorService reportGeneratorService;
    @Test
    public void testCreateReportSBI() throws JsonProcessingException {
        ReportRequestDto requestDto =new ReportRequestDto();
        requestDto.setProjectType("SBI");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse =
                new ResponseWrapper<TestRunDetailsResponseDto>();
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
        reportGeneratorService.createReport(requestDto,"abcdefgh");
    }

    @Test
    public void testCreateReportSDK() throws JsonProcessingException {
        ReportRequestDto requestDto =new ReportRequestDto();
        requestDto.setProjectType("SDK");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse =
                new ResponseWrapper<TestRunDetailsResponseDto>();
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
        reportGeneratorService.createReport(requestDto,"abcdefgh");
    }
    @Test
    public void testCreateReportABIS() throws JsonProcessingException {
        ReportRequestDto requestDto =new ReportRequestDto();
        requestDto.setProjectType("ABIS");
        requestDto.setProjectId("kdshfksjd");
        requestDto.setCollectionId("sajdnsaldk");
        requestDto.setTestRunId("12345678");
        ResponseWrapper<TestRunDetailsResponseDto> testRunDetailsResponse =
                new ResponseWrapper<TestRunDetailsResponseDto>();
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
        reportGeneratorService.createReport(requestDto,"abcdefgh");
    }

}
