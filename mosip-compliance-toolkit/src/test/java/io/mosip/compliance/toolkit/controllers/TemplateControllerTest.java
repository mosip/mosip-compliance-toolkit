package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.service.TemplateService;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class TemplateControllerTest {

    @InjectMocks
    private TemplateController templateController;

    @Mock
    private TemplateService templateService;

    /*
     * This class tests the getConsentTemplate method
     */
    @Test
    public void getBiometricsConsentTemplateTest() {
        ResponseWrapper<MasterTemplatesDto> response = new ResponseWrapper<>();
        when(templateService.getTemplate(anyString(),anyString(),anyString())).thenReturn(response);
        Assert.assertEquals(response, templateController.getTemplate("eng","abc","v1"));
    }

    @Test
    public void getLatestTemplateVersionTest() {
        ResponseWrapper<String> response = new ResponseWrapper<>();
        when(templateService.getLatestTemplateVersion(anyString())).thenReturn(response);
        Assert.assertEquals(response, templateController.getLatestTemplateVersion("abc"));
    }
}