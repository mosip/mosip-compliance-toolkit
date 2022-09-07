package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.service.SdkProjectService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class SdkProjectControllerTest {

    @InjectMocks
    private SdkProjectController sdkProjectController;

    @Mock
    private SdkProjectService sdkProjectService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    /*
     * This class tests the initBinder method
     */
    @Test
    public void initBinderTest(){
        WebDataBinder binder = new WebDataBinder(null);
        sdkProjectController.initBinder(binder);
    }

    /*
     * This class tests the getProjectById method
     */
    @Test
    public void getProjectByIdTest(){
        String id = "123";
        ResponseWrapper<SdkProjectDto> response = new ResponseWrapper<>();
        Mockito.when(sdkProjectService.getSdkProject(id)).thenReturn(response);
        Assert.assertEquals(response, ReflectionTestUtils.invokeMethod(sdkProjectController, "getProjectById", id));
    }


    /*
     * This class tests the addSdkProject method
     */
    @Test
    public void addSdkProjectTest() throws Exception {
        RequestWrapper<SdkProjectDto> value = new RequestWrapper<>();
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        value.setRequest(sdkProjectDto);
        ResponseWrapper<SdkProjectDto> response = new ResponseWrapper<>();
        Mockito.when(sdkProjectService.addSdkProject(sdkProjectDto)).thenReturn(response);
        sdkProjectController.addSdkProject(value, errors);
    }

    /*
     * This class tests the updateSdkProject method
     */
    @Test
    public void updateSdkProjectTest() throws Exception {
        RequestWrapper<SdkProjectDto> value = new RequestWrapper<>();
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        value.setRequest(sdkProjectDto);
        ResponseWrapper<SdkProjectDto> response = new ResponseWrapper<>();
        Mockito.when(sdkProjectService.updateSdkProject(sdkProjectDto)).thenReturn(response);
        sdkProjectController.updateSdkProject(value, errors);
    }
}
