package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.*;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionsResponseDto;
import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.compliance.toolkit.entity.CollectionSummaryEntity;
import io.mosip.compliance.toolkit.entity.CollectionTestCaseEntity;
import io.mosip.compliance.toolkit.repository.CollectionTestCaseRepository;
import io.mosip.compliance.toolkit.repository.CollectionsRepository;
import io.mosip.compliance.toolkit.repository.CollectionsSummaryRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
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

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class CollectionsServiceTest {

    @InjectMocks
    private CollectionsService collectionsService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private CollectionsRepository collectionsRepository;

    @Mock
    private CollectionTestCaseRepository collectionTestcaseRepository;

    @Mock
    private CollectionsSummaryRepository collectionsSummaryRepository;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

    static String id ="123";

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.invokeMethod(collectionsService, "authUserDetails");
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
        String result = ReflectionTestUtils.invokeMethod(collectionsService, "getPartnerId");
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
        String result = ReflectionTestUtils.invokeMethod(collectionsService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }


    /*
     * This class tests the getCollectionById method
     */
    @Test
    public void getCollectionByIdTest(){
        CollectionEntity collectionEntity = new CollectionEntity();
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(collectionsRepository.getCollectionById(Mockito.any(), Mockito.any())).thenReturn(collectionEntity);
        collectionsService.getCollectionById(id);

        collectionEntity.setSbiProjectId("sbi");
        Mockito.when(collectionsRepository.getCollectionById(Mockito.any(), Mockito.any())).thenReturn(collectionEntity);
        collectionsService.getCollectionById(id);

        collectionEntity.setSbiProjectId(null);
        collectionEntity.setSdkProjectId("sdk");
        Mockito.when(collectionsRepository.getCollectionById(Mockito.any(), Mockito.any())).thenReturn(collectionEntity);
        collectionsService.getCollectionById(id);

        collectionEntity.setSbiProjectId(null);
        collectionEntity.setSdkProjectId(null);
        collectionEntity.setAbisProjectId("abis");
        Mockito.when(collectionsRepository.getCollectionById(Mockito.any(), Mockito.any())).thenReturn(collectionEntity);
        ResponseWrapper<CollectionDto> result = collectionsService.getCollectionById(id);

        CollectionDto collectionDto = new CollectionDto();
        collectionDto.setProjectId("abis");
        Assert.assertEquals( collectionDto, result.getResponse());

    }


    /*
     * This class tests the getCollectionById method with null entity
     */
    @Test
    public void getCollectionByIdNullEntityTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(collectionsRepository.getCollectionById(Mockito.any(), Mockito.any())).thenReturn(null);
        ResponseWrapper<CollectionDto> result = collectionsService.getCollectionById(id);
        Assert.assertNull(result.getResponse());
    }


    /*
     * This class tests the getCollectionById method in case of Exception
     */
    @Test
    public void getCollectionByIdTestException(){
        collectionsService.getCollectionById(id);
    }


    /*
     * This class tests the getTestcasesForCollection method
     */
    @Test
    public void getTestcasesForCollectionTestcaseNullTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(collectionTestcaseRepository.getTestCasesByCollectionId(Mockito.any(), Mockito.any())).thenReturn(null);
        ResponseWrapper<CollectionTestCasesResponseDto> result = collectionsService.getTestCasesForCollection(id);
        Assert.assertNull(result.getResponse());
    }


    /*
     * This class tests the getTestcasesForCollection method
     */
    @Test
    public void getTestcasesForCollectionTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        List<String> testcases = new ArrayList<>();
        testcases.add("sbi1");
        Mockito.when(collectionTestcaseRepository.getTestCasesByCollectionId(Mockito.any(), Mockito.any())).thenReturn(testcases);
        ResponseWrapper<CollectionTestCasesResponseDto> result = collectionsService.getTestCasesForCollection(id);
        Assert.assertNull(result.getResponse());
    }


    /*
     * This class tests the getTestcasesForCollection method in case of Exception
     */
    @Test
    public void getTestcasesForCollectionTestException(){
        collectionsService.getTestCasesForCollection(id);
    }


    /*
     * This class tests the getCollections method
     */
    @Test
    public void getCollectionsTest(){
        String projectType ="others";
        List<CollectionSummaryEntity> collectionsEntityList = new ArrayList<>();
        Mockito.when(collectionsSummaryRepository.getCollectionsOfSbiProjects(Mockito.any(), Mockito.any())).thenReturn(collectionsEntityList);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        collectionsService.getCollections(projectType, id);

        projectType = "Sdk";
        collectionsService.getCollections(projectType, id);
        collectionsService.getCollections(projectType, null);

        projectType = "Abis";
        collectionsService.getCollections(projectType, id);

        projectType = "Sbi";
        ResponseWrapper<CollectionsResponseDto> result = collectionsService.getCollections(projectType, id);
        System.out.println("result"+result);

        CollectionsResponseDto expected = new CollectionsResponseDto();
        expected.setCollections(new ArrayList<>());
        Assert.assertEquals( expected,result.getResponse());
    }


    /*
     * This class tests the getCollections method in case of Exception
     */
    @Test
    public void getCollectionsTestException(){
        String projectType ="Sbi";
        collectionsService.getCollections(projectType, id);
    }

    /*
     * This class tests the addCollection method
     */
    @Test
    public void addCollectionTest(){
        CollectionRequestDto requestDto = new CollectionRequestDto();
        collectionsService.addCollection(requestDto);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
//        type = SBI
        CollectionDto collectionDto = new CollectionDto();
        collectionDto.setProjectId("SBI");
        requestDto.setProjectType("SBI");
        requestDto.setCollectionName(String.valueOf(collectionDto));

        CollectionEntity outputEntity = new CollectionEntity();
        Mockito.when(collectionsRepository.save(Mockito.any())).thenReturn(outputEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        Mockito.when(mapper.convertValue(outputEntity, CollectionDto.class)).thenReturn(collectionDto);
        collectionsService.addCollection(requestDto);
//        type = ABIS
        collectionDto.setCollectionId("ABIS");
        requestDto.setProjectType("ABIS");
        requestDto.setCollectionName(String.valueOf(collectionDto));
        collectionsService.addCollection(requestDto);
//        type = SDK
        collectionDto.setCollectionId("SDK");
        requestDto.setProjectType("SDK");
        requestDto.setCollectionName(String.valueOf(collectionDto));
        ResponseWrapper<CollectionDto> response = collectionsService.addCollection(requestDto);
        CollectionDto result = new CollectionDto();
        Assert.assertEquals(result, response.getResponse());
    }

    /*
     * This class tests the addCollection method in case of Exception
     */
    @Test
    public void addCollectionTestException(){
        CollectionRequestDto requestDto = new CollectionRequestDto();
        CollectionDto collectionDto = new CollectionDto();
        collectionDto.setProjectId("SBI");
        requestDto.setProjectType("SBI");
        requestDto.setCollectionName(String.valueOf(collectionDto));
        collectionsService.addCollection(requestDto);
    }


    /*
     * This class tests the addTestCasesForCollection method
     */
    @Test
    public void addTestCasesForCollectionTest(){
        List<CollectionTestCaseDto> inputList = new ArrayList<>();
        ResponseWrapper<List<CollectionTestCaseDto>> response = new ResponseWrapper<>();
        collectionsService.addTestCasesForCollection(inputList);
        CollectionTestCaseDto dto = new CollectionTestCaseDto();
        inputList.add(dto);
        CollectionTestCaseEntity outputEntity = new CollectionTestCaseEntity();
        Mockito.when(collectionTestcaseRepository.save(Mockito.any())).thenReturn(outputEntity);
        response = collectionsService.addTestCasesForCollection(inputList);
        Assert.assertEquals(inputList, response.getResponse());
    }

    /*
     * This class tests the addTestCasesForCollection method in case of Exception
     */
    @Test
    public void addTestCasesForCollectionTestException(){
        List<CollectionTestCaseDto> inputList = new ArrayList<>();
        CollectionTestCaseDto dto = new CollectionTestCaseDto();
        inputList.add(dto);
        collectionsService.addTestCasesForCollection(inputList);
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
