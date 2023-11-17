package io.mosip.compliance.toolkit.controllers;

import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ReportControllerTest {

    @Mock
    private ReportService reportGeneratorService;

    @InjectMocks
    private ReportController reportGeneratorController;

    @Mock
    RequestValidator requestValidator;

    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        reportGeneratorController.initBinder(binder);
    }

    @Test
    public void generateDraftReportTest() throws Exception {
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
        reportGeneratorController.generateDraftReport(value, origin, errors);
    }
}
