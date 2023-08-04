package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.ErrorDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.service.SbiProjectService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Ignore;
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

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class SbiProjectControllerTest {

    @InjectMocks
    private SbiProjectController sbiProjectController;

    @Mock
    private SbiProjectService sbiProjectService;

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
        sbiProjectController.initBinder(binder);
    }

    /*
     * This class tests the addSbiProject method
     */
    @Test
    public void addSbiProject() throws Exception {
        RequestWrapper<SbiProjectDto> value = new RequestWrapper<>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        value.setRequest(sbiProjectDto);
        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        Mockito.when(sbiProjectService.addSbiProject(sbiProjectDto)).thenReturn(sbiProjectDtoResponseWrapper);
        sbiProjectController.addSbiProject(value, errors);
    }
    
    @Test
    public void updateSbiProject_ValidInput_Success() throws Exception {
        RequestWrapper<SbiProjectDto> requestWrapper = createMockRequestWrapper();
        Errors errors = mock(Errors.class);
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(sbiProjectDto);
        Mockito.when(sbiProjectService.updateSbiProject(requestWrapper.getRequest())).thenReturn(responseWrapper);

        ResponseWrapper<SbiProjectDto> responseWrapper1 = sbiProjectController.updateSbiProject(requestWrapper, errors);
    }

    private RequestWrapper<SbiProjectDto> createMockRequestWrapper() {
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        RequestWrapper<SbiProjectDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setRequest(sbiProjectDto);
        return requestWrapper;
    }

    @Test
    public void getEncryptionKey_Success() throws Exception {
        ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
        String encryptionKey = "your_encryption_key_here";
        responseWrapper.setResponse(encryptionKey);
        Mockito.when(sbiProjectService.getEncryptionKey()).thenReturn(responseWrapper);

        Method getEncryptionKeyMethod = SbiProjectController.class.getDeclaredMethod("getEncryptionKey");
        getEncryptionKeyMethod.setAccessible(true); // Allow invoking private method
        ResponseWrapper<String> result = (ResponseWrapper<String>) getEncryptionKeyMethod.invoke(sbiProjectController);

    }


    /*
     * This class tests the getProjectById method
     */
    @Test
    public void getProjectByIdTest(){
        String id ="123";
        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        Mockito.when(sbiProjectService.getSbiProject(Mockito.anyString())).thenReturn(sbiProjectDtoResponseWrapper);
        ReflectionTestUtils.invokeMethod(sbiProjectController, "getProjectById", id);
    }
}
