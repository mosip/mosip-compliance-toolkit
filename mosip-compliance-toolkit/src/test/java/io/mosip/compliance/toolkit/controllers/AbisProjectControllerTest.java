package io.mosip.compliance.toolkit.controllers;


import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.service.AbisProjectService;
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
public class AbisProjectControllerTest {

    @InjectMocks
    private AbisProjectController abisProjectController;

    @Mock
    private AbisProjectService abisProjectService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    /*
     * This class tests the initBinder method
     */
    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        abisProjectController.initBinder(binder);
    }

    /*
     * This class tests the getProjectById method
     */
    @Test
    public void getProjectByIdTest() {
        String id = "123";
        ResponseWrapper<AbisProjectDto> response = new ResponseWrapper<>();
        Mockito.when(abisProjectService.getAbisProject(id)).thenReturn(response);
        Assert.assertEquals(response, ReflectionTestUtils.invokeMethod(abisProjectController, "getProjectById", id));
    }


    /*
     * This class tests the addAbisProject method
     */
    @Test
    public void addAbisProjectTest() throws Exception {
        RequestWrapper<AbisProjectDto> value = new RequestWrapper<>();
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        value.setRequest(abisProjectDto);
        ResponseWrapper<AbisProjectDto> response = new ResponseWrapper<>();
        Mockito.when(abisProjectService.addAbisProject(abisProjectDto)).thenReturn(response);
        abisProjectController.addAbisProject(value, errors);
    }

    /*
     * This class tests the updateAbisProject method
     */
    @Test
    public void updateAbisProjectTest() throws Exception {
        RequestWrapper<AbisProjectDto> value = new RequestWrapper<>();
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        value.setRequest(abisProjectDto);
        ResponseWrapper<AbisProjectDto> response = new ResponseWrapper<>();
        Mockito.when(abisProjectService.updateAbisProject(abisProjectDto)).thenReturn(response);
        abisProjectController.updateAbisProject(value, errors);
    }
}
