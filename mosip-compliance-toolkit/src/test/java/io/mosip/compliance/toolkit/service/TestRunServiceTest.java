package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.dto.PageDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDetailsResponseDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunHistoryDto;
import io.mosip.compliance.toolkit.dto.testrun.TestRunStatusDto;
import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.compliance.toolkit.entity.TestRunHistoryEntity;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.TestRunDetailsRepository;
import io.mosip.compliance.toolkit.repository.TestRunRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class TestRunServiceTest {

    @InjectMocks
    private TestRunService testRunService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private CollectionsRepository collectionsRepository;

    @Mock
    private TestRunDetailsRepository testRunDetailsRepository;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private TestRunRepository testRunRepository;  

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.invokeMethod(testRunService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        String result = ReflectionTestUtils.invokeMethod(testRunService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        String result = ReflectionTestUtils.invokeMethod(testRunService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the addTestRun method
     */
    @Test
    public void addTestRunTest(){
        testRunService.addTestRun(null);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        TestRunDto inputTestRun = new TestRunDto();
        String id="ABCKALKJA";
        inputTestRun.setCollectionId(id);
        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("456");
        testRunService.addTestRun(inputTestRun);
        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestRunEntity entity = new TestRunEntity();
        Mockito.when(mapper.convertValue(inputTestRun, TestRunEntity.class)).thenReturn(entity);
        TestRunEntity outputEntity = new TestRunEntity();
        Mockito.when(testRunRepository.save(Mockito.any())).thenReturn(outputEntity);
        TestRunDto testRun = new TestRunDto();
        Mockito.when(mapper.convertValue(outputEntity, TestRunDto.class)).thenReturn(testRun);
        ResponseWrapper<TestRunDto> response = testRunService.addTestRun(inputTestRun);
        Assert.assertEquals(testRun, response.getResponse());
    }

    /*
     * This class tests the addTestRun method in case Exception
     */
    @Test
    public void addTestRunTestException(){
        TestRunDto inputTestRun = new TestRunDto();
        String id="ABCKALKJA";
        inputTestRun.setCollectionId(id);
        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
        testRunService.addTestRun(inputTestRun);
    }

    /*
     * This class tests the updateTestRunExecutionTime method
     */
    @Test
    public void updateTestRunExecutionTimeTest(){
        testRunService.updateTestRunExecutiionTime(null);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        TestRunDto inputTestRun = new TestRunDto();
        String id="ABCKALKJA";
        inputTestRun.setCollectionId(id);
        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("456");
        Mockito.when(testRunRepository.updateExecutionDateById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
        testRunService.updateTestRunExecutiionTime(inputTestRun);

        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
        Mockito.when(testRunRepository.updateExecutionDateById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(0);
        testRunService.updateTestRunExecutiionTime(inputTestRun);

        Mockito.when(testRunRepository.updateExecutionDateById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
        ResponseWrapper<TestRunDto> result = testRunService.updateTestRunExecutiionTime(inputTestRun);
        Assert.assertEquals(inputTestRun,result.getResponse());
    }

    /*
     * This class tests the updateTestRunExecutionTime method in case Exception
     */
//    @Test
//    public void updateTestRunExecutionTimeTestException(){
//        TestRunDto inputTestRun = new TestRunDto();
//        String id="ABCKALKJA";
//        inputTestRun.setCollectionId(id);
//        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
//
//        Mockito.when(testRunRepository.updateExecutionDateById(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(1);
//        testRunService.addTestRun(inputTestRun);
//    }

    /*
     * This class tests the addTestRunDetails method
     */
    @Test
    public void addTestRunDetailsTest(){
        testRunService.addTestRunDetails(null);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        TestRunDetailsDto inputTestRunDetails = new TestRunDetailsDto();
        String id="ABCKALKJA";
        inputTestRunDetails.setRunId(id);
        Mockito.when(testRunRepository.getPartnerIdByRunId(id)).thenReturn("456");
        testRunService.addTestRunDetails(inputTestRunDetails);

        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestRunDetailsEntity entity = new TestRunDetailsEntity();
        Mockito.when(mapper.convertValue(inputTestRunDetails, TestRunDetailsEntity.class)).thenReturn(entity);
        Mockito.when(testRunRepository.getPartnerIdByRunId(id)).thenReturn("123");
        Mockito.when(testRunDetailsRepository.save(entity)).thenReturn(entity);
        ResponseWrapper<TestRunDetailsDto> result = testRunService.addTestRunDetails(inputTestRunDetails);
        Assert.assertNull(result.getResponse());
    }

    /*
     * This class tests the addTestRunDetails method in case Exception
     */
    @Test
    public void addTestRunDetailsTestException(){
        TestRunDetailsDto inputTestRunDetails = new TestRunDetailsDto();
        String id="ABCKALKJA";
        inputTestRunDetails.setRunId(id);
        Mockito.when(collectionsRepository.getPartnerById(id)).thenReturn("123");
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
        testRunService.addTestRunDetails(inputTestRunDetails);
    }

    /*
     * This class tests the getTestRunDetails method
     */
    @Test
    public void getTestRunDetailsTest(){
        testRunService.getTestRunDetails(null);
        String runId = "123";
        TestRunEntity entity = new TestRunEntity();

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(testRunRepository.getTestRunById(Mockito.any(),Mockito.any())).thenReturn(null);
        testRunService.getTestRunDetails(runId);

        Mockito.when(testRunRepository.getTestRunById(Mockito.any(),Mockito.any())).thenReturn(entity);
        List<TestRunDetailsEntity> testRunDetailsEntityList = new ArrayList<>();
        Mockito.when(testRunDetailsRepository.getTestRunDetails(runId)).thenReturn(testRunDetailsEntityList);
        testRunService.getTestRunDetails(runId);

        TestRunDetailsEntity testRunDetailsEntity = new TestRunDetailsEntity();
        testRunDetailsEntityList.add(testRunDetailsEntity);
        Mockito.when(testRunDetailsRepository.getTestRunDetails(runId)).thenReturn(testRunDetailsEntityList);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestRunDetailsDto dto = new TestRunDetailsDto();
        Mockito.when(mapper.convertValue(testRunDetailsEntity, TestRunDetailsDto.class)).thenReturn(dto);
        ResponseWrapper<TestRunDetailsResponseDto> result = testRunService.getTestRunDetails(runId);
        TestRunDetailsResponseDto expected = new TestRunDetailsResponseDto();
        List<TestRunDetailsDto> testRunDetailsDtosList = new ArrayList<>();
        testRunDetailsDtosList.add(new TestRunDetailsDto());
        expected.setTestRunDetailsList(testRunDetailsDtosList);
        Assert.assertEquals(expected, result.getResponse());
    }

    /*
     * This class tests the getTestRunDetails method in case of exception
     */
    @Test
    public void getTestRunDetailsTestException(){
        testRunService.getTestRunDetails("123");
    }

    /*
     * This class tests the getTestRunHistory method
     */
    @Test
    public void getTestRunHistoryTest(){
        testRunService.getTestRunHistory(null, 0, 10);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
   
        Page<TestRunHistoryEntity> testRunHistoryEntityPage = Page.empty();
        Mockito.when(testRunRepository.getTestRunHistoryByCollectionId(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(testRunHistoryEntityPage);
        String collectionId = "123";
        int pageNo = 0;
        int pageSize = 10;
        testRunService.getTestRunHistory(collectionId, pageNo, pageSize);

        LocalDateTime lastRunTime = LocalDateTime.now();
        
        List<TestRunHistoryEntity> entityList = new ArrayList<>();
        TestRunHistoryEntity entity = new TestRunHistoryEntity(collectionId, lastRunTime, 2 , 1, 1);
        entityList.add(entity);

        Page<TestRunHistoryEntity> pages = new PageImpl<TestRunHistoryEntity>(entityList);

        Mockito.when(testRunRepository.getTestRunHistoryByCollectionId(Mockito.any(),Mockito.any(), Mockito.any())).thenReturn(pages);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestRunHistoryDto dto = new TestRunHistoryDto();
        Mockito.when(mapper.convertValue(entity, TestRunHistoryDto.class)).thenReturn(dto);
        ResponseWrapper<PageDto<TestRunHistoryDto>>  testRunHistoryResponse= testRunService.getTestRunHistory(collectionId, pageNo, pageSize);
        Assert.assertEquals(dto, testRunHistoryResponse.getResponse().getContent().get(0));
    }

    /*
     * This class tests the getTestRunHistory method in case of exception
     */
    @Test
    public void getTestRunHistoryTestException(){
        testRunService.getTestRunHistory("123", 0, 10);
    }

    /*
     * This class tests the getTestRunStatus method
     */
    @Test
    public void getTestRunStatusTest(){
        testRunService.getTestRunStatus(null);
        String runId= "123";
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        List<TestRunHistoryEntity> testRunHistoryEntityList = new ArrayList<>();
        Mockito.when(testRunRepository.getTestCaseCount(Mockito.any())).thenReturn(0);
        Mockito.when(testRunDetailsRepository.getTestRunSuccessCount(Mockito.any())).thenReturn(0);
        Mockito.when(testRunRepository.getPartnerIdByRunId(runId)).thenReturn("456");
        testRunService.getTestRunStatus(runId);

        Mockito.when(testRunRepository.getPartnerIdByRunId(runId)).thenReturn(runId);
        ResponseWrapper<TestRunStatusDto> result = testRunService.getTestRunStatus(runId);
        TestRunStatusDto dto = new TestRunStatusDto();
        dto.setResultStatus(true);
        Assert.assertEquals(dto, result.getResponse());
    }

    /*
     * This class tests the getTestRunStatus method in case of exception
     */
    @Test
    public void getTestRunStatusTestException(){
        testRunService.getTestRunStatus("123");
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
