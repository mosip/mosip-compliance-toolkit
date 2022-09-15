package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.projects.ProjectsResponseDto;
import io.mosip.compliance.toolkit.service.ProjectsService;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ProjectsControllerTest {
    @InjectMocks
    private ProjectsController projectsController;

    @Mock
    private ProjectsService projectsService;

    /*
     * This class tests the initBinder method
     */
    @Test
    public void initBinderTest(){
        WebDataBinder binder = new WebDataBinder(null);
        projectsController.initBinder(binder);
    }

    /*
     * This class tests the getProjects method
     */
    @Test
    public void getProjectsTest(){
        ResponseWrapper<ProjectsResponseDto> expected = null;
        String type = null;
        ResponseWrapper<ProjectsResponseDto> responseWrapper = projectsController.getProjects(type);
        Assert.assertEquals(expected, responseWrapper);
    }
}
