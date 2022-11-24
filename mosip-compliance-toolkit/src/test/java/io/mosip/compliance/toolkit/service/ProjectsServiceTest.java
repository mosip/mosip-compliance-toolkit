package io.mosip.compliance.toolkit.service;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.projects.ProjectDto;
import io.mosip.compliance.toolkit.dto.projects.ProjectsResponseDto;
import io.mosip.compliance.toolkit.entity.ProjectSummaryEntity;
import io.mosip.compliance.toolkit.repository.ProjectSummaryRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import org.junit.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ProjectsServiceTest {
    @InjectMocks
    public ProjectsService projectsService = new ProjectsService();

    @Mock
    private ProjectSummaryRepository projectSummaryRepository;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    private MosipUserDto mosipUserDto;

    @Before
    public void before(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest(){
        ReflectionTestUtils.invokeMethod(projectsService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest(){
        String result = ReflectionTestUtils.invokeMethod(projectsService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getProjects method when isProjectTypeValid == false
     */
    @Test
    public void getProjectsTest(){
        List<ProjectDto> projects = new ArrayList<>();
        ProjectsResponseDto projectsResponseDtoExpected = new ProjectsResponseDto();
        projectsResponseDtoExpected.setProjects(projects);
        LocalDateTime now = LocalDateTime.now();
        ProjectSummaryEntity projectSummaryEntity = new ProjectSummaryEntity("projectId", "projectname", "projecttype", now,
        3, "collectionid", "runid", now);
        List<ProjectSummaryEntity> projectsSummaryList = new ArrayList<>();
        projectsSummaryList.add(projectSummaryEntity);
        Mockito.when(projectSummaryRepository.getSummaryOfAllSBIProjects(Mockito.anyString())).thenReturn(projectsSummaryList);
        projectsService.getProjects(AppConstants.SBI);
        ResponseWrapper<ProjectsResponseDto> responseWrapper = projectsService.getProjects("other");
        Assert.assertEquals(projectsResponseDtoExpected.getProjects(),
                responseWrapper.getResponse().getProjects());
    }

    /*
     * This class tests the getProjects method in case of exception
     */
    @Test
    public void getProjectsTestException(){
        ProjectsResponseDto projectsResponseDtoExpected = new ProjectsResponseDto();
        List<ProjectDto> projects = new ArrayList<>();
        projectsResponseDtoExpected.setProjects(projects);
//        When type=SBI
        projectsService.getProjects("SBI");
//        When type=ABIS
        projectsService.getProjects("ABIS");
//        When type=SDK
        projectsService.getProjects("SDK");
//        When type=null
        projectsService.getProjects(null);
        ReflectionTestUtils.setField(projectsService, "projectSummaryRepository", null);
        projectsService.getProjects("SBI");
    }

    /*
     * This method is used to get MosipUserDto in class
     */
    private MosipUserDto getMosipUserDto(){
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
