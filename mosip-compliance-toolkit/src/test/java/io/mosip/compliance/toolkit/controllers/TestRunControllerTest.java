package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.dto.testrun.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.compliance.toolkit.service.TestRunService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class TestRunControllerTest {

    @InjectMocks
    private TestRunController testRunController;

    @Mock
    private TestRunService testRunService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    /*
     * This class tests the addTestRun method in case of Exception
     */
    @Test
    public void addTestRunTest() throws Exception {
        RequestWrapper<TestRunDto> request = new RequestWrapper<>();
        testRunController.addTestrun(request, errors);
    }

    /*
     * This class tests the updateTestrun method in case of Exception
     */
    @Test
    public void updateTestrunTest() throws Exception {
        RequestWrapper<TestRunDto> request = new RequestWrapper<>();
        testRunController.updateTestrun(request, errors);
    }

    /*
     * This class tests the addTestrunDetails method in case of Exception
     */
    @Test
    public void addTestrunDetailsTest() throws Exception {
        RequestWrapper<TestRunDetailsDto> request = new RequestWrapper<>();
        testRunController.addTestrunDetails(request, errors);
    }

    /*
     * This class tests the getTestRunDetails method
     */
    @Test
    public void getTestRunDetailsTest() throws Exception {
        String runId ="123";
        ResponseWrapper<TestRunDetailsResponseDto> response = new ResponseWrapper<>();
        Mockito.when(testRunService.getTestRunDetails(runId)).thenReturn(response);
        ResponseWrapper<TestRunDetailsResponseDto> result = testRunController.getTestRunDetails(runId);
        Assert.assertEquals(response.getResponse(), result.getResponse());
    }

    /*
     * This class tests the getTestRunHistory method
     */
    @Test
    public void getTestRunHistoryTest() throws Exception {
        String collectionId ="123";
        int pageNo = 0;
        int pageSize = 10;
        ResponseWrapper<PageDto<TestRunHistoryDto>> response = new ResponseWrapper<>();
        Mockito.when(testRunService.getTestRunHistory(collectionId, pageNo, pageSize)).thenReturn(response);
        Assert.assertEquals(response, testRunController.getTestRunHistory(collectionId, pageNo, pageSize));
    }

    /*
     * This class tests the getTestRunDetails method
     */
    @Test
    public void getTestRunStatusTest() throws Exception {
        String runId ="123";
        ResponseWrapper<TestRunStatusDto> response = new ResponseWrapper<>();
        Mockito.when(testRunService.getTestRunStatus(runId)).thenReturn(response);
        ResponseWrapper<TestRunStatusDto> result = testRunController.getTestRunStatus(runId);
        Assert.assertEquals(response.getResponse(), result.getResponse());
    }
}

