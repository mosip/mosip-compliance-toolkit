package io.mosip.compliance.toolkit.service;

import io.mosip.compliance.toolkit.dto.ProjectDto;
import io.mosip.compliance.toolkit.dto.ProjectsResponseDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;


@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ProjectsServiceTest {

    @InjectMocks
    public ProjectsService projectsService = new ProjectsService();


    /*
     * This class tests the getProjects method when isProjectTypeValid == false
     */
    @Test
    public void getProjectsTest(){
//        Expected
        List<ProjectDto> projects = new ArrayList<>();
        ProjectsResponseDto projectsResponseDtoExpected = new ProjectsResponseDto();
        projectsResponseDtoExpected.setProjects(projects);

        ResponseWrapper<ProjectsResponseDto> responseWrapper = projectsService.getProjects("other");
        Assert.assertEquals(projectsResponseDtoExpected.getProjects(),
                responseWrapper.getResponse().getProjects());
    }

    /*
     * This class tests the getProjects method when isProjectTypeValid == true
     */
    @Test
    public void getProjectsTestException(){
//        Expected
        ProjectsResponseDto projectsResponseDtoExpected = new ProjectsResponseDto();
        projectsResponseDtoExpected.setProjects(null);

//        When type=SBI
        projectsService.getProjects("SBI");
//        When type=ABIS
        projectsService.getProjects("ABIS");
//        When type=SDK
        projectsService.getProjects("SDK");
//        When type=null
        ResponseWrapper<ProjectsResponseDto> responseWrapper = projectsService.getProjects(null);
        Assert.assertEquals(projectsResponseDtoExpected.getProjects(),
                responseWrapper.getResponse().getProjects());
    }
}







