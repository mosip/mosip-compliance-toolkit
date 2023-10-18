package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.projects.SdkProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.SdkProjectEntity;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.repository.SdkProjectRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.Optional;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class SdkProjectServiceTest {

    @InjectMocks
    private SdkProjectService sdkProjectService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private SdkProjectRepository sdkProjectRepository;

    @Mock
    private ObjectStoreAdapter objectStore;

    @Mock
    ResourceCacheService resourceCacheService;

    @Mock
    private BiometricTestDataRepository biometricTestDataRepository;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CollectionsService collectionsService;

    @Mock
    private TestCasesService testCasesService;

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
        ReflectionTestUtils.invokeMethod(sdkProjectService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest(){
        String result = ReflectionTestUtils.invokeMethod(sdkProjectService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest(){
        String result = ReflectionTestUtils.invokeMethod(sdkProjectService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the getSdkProject method
     */
    @Test
    public void getSdkProjectTest(){
        String id = "123";
        SdkProjectEntity sdkProjectEntity = new SdkProjectEntity();
        Optional<SdkProjectEntity> sdkProjectEntityOpt = Optional.of(sdkProjectEntity);
        Mockito.when(sdkProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sdkProjectEntityOpt);

        ResponseWrapper<SdkProjectDto> result = sdkProjectService.getSdkProject(id);
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        Assert.assertEquals( sdkProjectDto, result.getResponse());
    }

    /*
     * This class tests the getSdkProject method in case of Exception
     */
    @Test
    public void getSdkProjectTestException(){
        ReflectionTestUtils.setField(sdkProjectService, "sdkProjectRepository", null);
        sdkProjectService.getSdkProject("123");
    }

    /*
     * This class tests the getSdkProject method when sdkProjectEntity is null
     */
    @Test
    public void getSdkProjectTestNullEntity(){
        String id = "123";
        SdkProjectEntity sdkProjectEntity = null;
        Optional<SdkProjectEntity> sdkProjectEntityOpt = Optional.ofNullable(sdkProjectEntity);
        Mockito.when(sdkProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sdkProjectEntityOpt);
        sdkProjectService.getSdkProject(id);
    }

    /*
     * This class tests the addSdkProject method
     */
    @Test
    public void addSdkProjectTest(){
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");
        sdkProjectDto.setBioTestDataFileName("testFile");
        sdkProjectDto.setOrgName("Not_Available");

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        Mockito.when(resourceCacheService.getOrgName("abc")).thenReturn("abc");
        SdkProjectDto sdkProjectDtoResponse = new SdkProjectDto();
        Mockito.when(mapper.convertValue(null, SdkProjectDto.class)).thenReturn(sdkProjectDtoResponse);

        ResponseWrapper<SdkProjectDto> sdkProjectDtoResponseWrapper = new ResponseWrapper<>();
        sdkProjectDtoResponseWrapper = sdkProjectService.addSdkProject(sdkProjectDto);
        Assert.assertNotNull(sdkProjectDtoResponseWrapper.getResponse());
    }

    @Test
    public void addSdkProjectAddDefaultTestCaseTest(){
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");
        sdkProjectDto.setBioTestDataFileName("testFile");
        sdkProjectDto.setOrgName("Not_Available");

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        SdkProjectDto sdkProjectDtoResponse = new SdkProjectDto();
        Mockito.when(mapper.convertValue(null, SdkProjectDto.class)).thenReturn(sdkProjectDtoResponse);
        ResponseWrapper<CollectionDto> addCollectionWrapper = new ResponseWrapper<CollectionDto>();
        CollectionDto collectionDto = new CollectionDto();
        collectionDto.setCollectionId("12345678");
        collectionDto.setProjectId("abcdefgh");
        collectionDto.setName("MyCollection");
        collectionDto.setTestCaseCount(8);
        addCollectionWrapper.setResponse(collectionDto);
        Mockito.when(collectionsService.addCollection(Mockito.any())).thenReturn(addCollectionWrapper);
        ResponseWrapper<List<TestCaseDto>> testCaseWrapper = new ResponseWrapper<List<TestCaseDto>>();
        List<TestCaseDto> testCaseDtoList = new ArrayList<TestCaseDto>();
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestCaseType("SDK");
        testCaseDto.setTestId("SDK2000");
        testCaseDto.setSpecVersion("0.9.5");
        testCaseDto.setTestName("Init Test");
        testCaseDto.setTestDescription("Initialise Bio SDK Services");
        testCaseDto.setAndroidTestDescription(null);
        testCaseDto.setNegativeTestcase(false);
        testCaseDto.setInactive(false);
        testCaseDto.setInactiveForAndroid(null);
        testCaseDto.setMethodName(null);
        testCaseDto.setRequestSchema(null);
        testCaseDto.setResponseSchema(null);
        testCaseDto.setValidatorDefs(null);
        testCaseDto.setOtherAttributes(null);
        testCaseDtoList.add(testCaseDto);
        testCaseWrapper.setResponse(testCaseDtoList);
        Mockito.when(testCasesService.getSdkTestCases(Mockito.any(),Mockito.any())).thenReturn(testCaseWrapper);


        ResponseWrapper<SdkProjectDto> sdkProjectDtoResponseWrapper = new ResponseWrapper<>();
        sdkProjectDtoResponseWrapper = sdkProjectService.addSdkProject(sdkProjectDto);
        Assert.assertNotNull(sdkProjectDtoResponseWrapper.getResponse());
    }


    /*
     * This class tests the addSdkProject method in case of exception
     */
    @Test
    public void addSdkProjectExceptionTest(){
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectService.addSdkProject(sdkProjectDto);
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");
        sdkProjectDto.setBioTestDataFileName(null);
        sdkProjectService.addSdkProject(sdkProjectDto);

        sdkProjectDto.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.doThrow(DataIntegrityViolationException.class).when(biometricTestDataRepository).findByTestDataName(Mockito.any(), Mockito.any());
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
        sdkProjectService.addSdkProject(sdkProjectDto);
    }

    /*
     * This class tests the updateSdkProject method
     */
    @Test
    public void updateSdkProjectTest(){
        String id = "123";
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setId(id);
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");
        sdkProjectDto.setBioTestDataFileName("testFile");

        SdkProjectEntity sdkProjectEntity = new SdkProjectEntity();
        Optional<SdkProjectEntity> sdkProjectEntityOpt = Optional.ofNullable(sdkProjectEntity);
        Mockito.when(sdkProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sdkProjectEntityOpt);
        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        SdkProjectDto sdkProjectDtoResponse = new SdkProjectDto();
        Mockito.when(mapper.convertValue(null, SdkProjectDto.class)).thenReturn(sdkProjectDtoResponse);

        ResponseWrapper<SdkProjectDto> sdkProjectDtoResponseWrapper = new ResponseWrapper<>();
        sdkProjectDtoResponseWrapper = sdkProjectService.updateSdkProject(sdkProjectDto);
        Assert.assertNotNull(sdkProjectDtoResponseWrapper.getResponse());
    }

    /*
     * This class tests the updateSdkProject method in case of exception
     */
    @Test
    public void updateSdkProjectExceptionTest(){
        String id = "123";
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setId(id);
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectService.updateSdkProject(sdkProjectDto);
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");

        SdkProjectEntity sdkProjectEntity = new SdkProjectEntity();
        Optional<SdkProjectEntity> sdkProjectEntityOpt = Optional.ofNullable(sdkProjectEntity);
        Mockito.when(sdkProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sdkProjectEntityOpt);
        sdkProjectDto.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);
        sdkProjectService.updateSdkProject(sdkProjectDto);

        sdkProjectDto.setBioTestDataFileName("testFile");
        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        ResponseWrapper<SdkProjectDto> sdkProjectDtoResponseWrapper = new ResponseWrapper<>();
        sdkProjectService.updateSdkProject(sdkProjectDto);
    }

    /*
     * This class tests the isValidSdkProject method
     */
    @Test
    public void isValidSbiProjectTest(){
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectDto.setUrl("http://localhost:9099/biosdk-service");
        Boolean result = ReflectionTestUtils.invokeMethod(sdkProjectService, "isValidSdkProject", sdkProjectDto);
        Assert.assertEquals(result, true);
    }

    /*
     * This class tests the updateSdkProject method in case of exception
     */
    @Test(expected = Exception.class)
    public void isValidSbiProjectExceptionTest(){
        SdkProjectDto sdkProjectDto = new SdkProjectDto();
        sdkProjectDto.setProjectType(ProjectTypes.SDK.getCode());
        sdkProjectDto.setSdkVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        sdkProjectDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        sdkProjectDto.setUrl(null);
        ReflectionTestUtils.invokeMethod(sdkProjectService, "isValidSdkProject", sdkProjectDto);
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
