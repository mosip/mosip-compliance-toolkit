package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.kernel.core.http.RequestWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import io.mosip.compliance.toolkit.service.ReportGeneratorService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ReportGeneratorControllerTest {

    @Mock
    private ReportGeneratorService reportGeneratorService;

    @InjectMocks
    private ReportGeneratorController reportGeneratorController;

    @Mock
    RequestValidator requestValidator;

    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        reportGeneratorController.initBinder(binder);
    }

    @Test
    public void createReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> value = new RequestWrapper<>();
        Errors errors = mock(Errors.class);
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setTestRunId("123");
        reportRequestDto.setCollectionId("123");
        value.setRequest(reportRequestDto);
        value.setId("abc");
        value.setRequesttime(LocalDateTime.now());
        value.setVersion("1.0");
        String origin = "abc";
        reportGeneratorController.createReport(value, origin, errors);
    }
}
