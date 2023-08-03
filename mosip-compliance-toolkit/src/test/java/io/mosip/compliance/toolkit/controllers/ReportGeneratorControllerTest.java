package io.mosip.compliance.toolkit.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import io.mosip.compliance.toolkit.service.ReportGeneratorService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ReportGeneratorControllerTest {

    @Mock
    private ReportGeneratorService reportGeneratorService;

    @InjectMocks
    private ReportGeneratorController reportGeneratorController;

    @Mock
    private Errors errors;

    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        reportGeneratorController.initBinder(binder);
    }
}
