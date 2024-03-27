package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.SdkSpecVersions;
import io.mosip.compliance.toolkit.service.ResourceManagementService;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ResourceManagementControllerTest {

    @InjectMocks
    private ResourceManagementController resourceManagementController;

    @Mock
    private ResourceManagementService resourceMgmtService;

    /*
     * This class tests the uploadResourceFile method
     */
    @Test
    public void uploadResourceFileTest() throws IOException {
        String type = ProjectTypes.SDK.getCode();
        String version = SdkSpecVersions.SPEC_VER_0_9_0.getCode();
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFile.zip", "multipart/form-data", inputFile);
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(true);
        Mockito.when(resourceMgmtService.uploadResourceFile(type, version, file)).thenReturn(responseWrapper);
        Assert.assertEquals(true, resourceManagementController.uploadResourceFile(type, version, file).getResponse());
    }

    @Test
    public void uploadtemplateTest() throws IOException {
        String langCode = "eng";
        String templateName = "terms_and_conditions_template";
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        MockMultipartFile file = new MockMultipartFile("file", "terms_and_conditions_template.vm", "multipart/form-data", inputFile);
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(true);
        Mockito.when(resourceMgmtService.uploadTemplate(langCode, templateName, file)).thenReturn(responseWrapper);
        Assert.assertEquals(true, resourceManagementController.uploadTemplate(langCode, templateName, file).getResponse());
    }
}
