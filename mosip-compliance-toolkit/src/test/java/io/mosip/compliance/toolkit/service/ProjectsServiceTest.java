package io.mosip.compliance.toolkit.service;

import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;


@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
@PrepareForTest( { ProjectsService.class })
public class ProjectsServiceTest {

    @InjectMocks
    public ProjectsService projectsService = new ProjectsService();


    /*
     * This class tests the getProjects method when isProjectTypeValid == false
     */
    @Test
    public void getProjectsTest(){
        projectsService.getProjects("other");
    }

    /*
     * This class tests the getProjects method when isProjectTypeValid == true
     */
    @Test
    public void getProjectsTestException(){
//        When type=SBI
        projectsService.getProjects("SBI");
//        When type=ABIS
        projectsService.getProjects("ABIS");
//        When type=SDK
        projectsService.getProjects("SDK");
//        When type=null
        projectsService.getProjects(null);
    }

//    @Test
//    public void authUserDetailsTest() throws Exception {
//
//        SecurityContextHolder securityContextHolder = PowerMockito.mock(SecurityContextHolder.class);
//        PowerMockito.whenNew(SecurityContextHolder.class).withNoArguments().thenReturn(securityContextHolder);
//        ReflectionTestUtils.invokeMethod(projectsService, "authUserDetails");
//    }

}







