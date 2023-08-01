package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
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
public class AbisProjectServiceTest {

    @InjectMocks
    private AbisProjectService abisProjectService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private AbisProjectRepository abisProjectRepository;

    @Mock
    private ObjectStoreAdapter objectStore;

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
    public void before() {
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
    public void authUserDetailsTest() {
        ReflectionTestUtils.invokeMethod(abisProjectService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest() {
        String result = ReflectionTestUtils.invokeMethod(abisProjectService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest() {
        String result = ReflectionTestUtils.invokeMethod(abisProjectService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the getAbisProject method
     */
    @Test
    public void getAbisProjectTest() {
        String id = "123";
        AbisProjectEntity abisProjectEntity = new AbisProjectEntity();
        Optional<AbisProjectEntity> abisProjectEntityOpt = Optional.of(abisProjectEntity);
        Mockito.when(abisProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(abisProjectEntityOpt);

        ResponseWrapper<AbisProjectDto> result = abisProjectService.getAbisProject(id);
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        Assert.assertEquals(abisProjectDto, result.getResponse());
    }

    /*
     * This class tests the getAbisProject method in case of Exception
     */
    @Test
    public void getAbisProjectTestException() {
        ReflectionTestUtils.setField(abisProjectService, "abisProjectRepository", null);
        abisProjectService.getAbisProject("123");
    }

    /*
     * This class tests the getAbisProject method when AbisProjectEntity is null
     */
    @Test
    public void getAbisProjectTestNullEntity() {
        String id = "123";
        AbisProjectEntity abisProjectEntity = null;
        Optional<AbisProjectEntity> abisProjectEntityOpt = Optional.ofNullable(abisProjectEntity);
        Mockito.when(abisProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(abisProjectEntityOpt);
        abisProjectService.getAbisProject(id);
    }

    /*
     * This class tests the addAbisProject method
     */
    @Test
    public void addAbisProjectTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectDto.setUsername("admin");
        abisProjectDto.setPassword("admin123");
        abisProjectDto.setOutboundQueueName("ctk-to-abis");
        abisProjectDto.setInboundQueueName("abis-to-ctk");
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");
        abisProjectDto.setBioTestDataFileName("testFile");

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        AbisProjectDto abisProjectDtoResponse = new AbisProjectDto();
        Mockito.when(mapper.convertValue(null, AbisProjectDto.class)).thenReturn(abisProjectDtoResponse);

        ResponseWrapper<AbisProjectDto> abisProjectDtoResponseWrapper = new ResponseWrapper<>();
        abisProjectDtoResponseWrapper = abisProjectService.addAbisProject(abisProjectDto);
        Assert.assertNotNull(abisProjectDtoResponseWrapper.getResponse());
    }

    @Test
    public void addAbisProjectAddDefaultCollectionTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectDto.setUsername("admin");
        abisProjectDto.setPassword("admin123");
        abisProjectDto.setOutboundQueueName("ctk-to-abis");
        abisProjectDto.setInboundQueueName("abis-to-ctk");
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");
        abisProjectDto.setBioTestDataFileName("testFile");

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        AbisProjectDto abisProjectDtoResponse = new AbisProjectDto();
        Mockito.when(mapper.convertValue(null, AbisProjectDto.class)).thenReturn(abisProjectDtoResponse);
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
        testCaseDto.setTestCaseType("ABIS");
        testCaseDto.setTestId("ABIS3000");
        testCaseDto.setSpecVersion("0.9.5");
        testCaseDto.setTestName("Insert one person's biomterics in ABIS");
        testCaseDto.setTestDescription("Insert one person's biomterics in ABIS");
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
        Mockito.when(testCasesService.getAbisTestCases(Mockito.any())).thenReturn(testCaseWrapper);

        ResponseWrapper<AbisProjectDto> abisProjectDtoResponseWrapper = new ResponseWrapper<>();
        abisProjectDtoResponseWrapper = abisProjectService.addAbisProject(abisProjectDto);
        Assert.assertNotNull(abisProjectDtoResponseWrapper.getResponse());
    }


    /*
     * This class tests the addAbisProject method in case of exception
     */
    @Test
    public void addAbisProjectExceptionTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectService.addAbisProject(abisProjectDto);
        abisProjectDto.setUsername("admin");
        abisProjectDto.setPassword("admin123");
        abisProjectDto.setOutboundQueueName("ctk-to-abis");
        abisProjectDto.setInboundQueueName("abis-to-ctk");
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");
        abisProjectDto.setBioTestDataFileName(null);
        abisProjectService.addAbisProject(abisProjectDto);

        abisProjectDto.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);

        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.doThrow(DataIntegrityViolationException.class).when(biometricTestDataRepository).findByTestDataName(Mockito.any(), Mockito.any());
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
        abisProjectService.addAbisProject(abisProjectDto);
    }

    /*
     * This class tests the updateAbisProject method
     */
    @Test
    public void updateAbisProjectTest() {
        String id = "123";
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setId(id);
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectDto.setUsername("admin");
        abisProjectDto.setPassword("admin123");
        abisProjectDto.setOutboundQueueName("ctk-to-abis");
        abisProjectDto.setInboundQueueName("abis-to-ctk");
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");
        abisProjectDto.setBioTestDataFileName("testFile");

        AbisProjectEntity abisProjectEntity = new AbisProjectEntity();
        Optional<AbisProjectEntity> abisProjectEntityOpt = Optional.ofNullable(abisProjectEntity);
        Mockito.when(abisProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(abisProjectEntityOpt);
        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        AbisProjectDto abisProjectDtoResponse = new AbisProjectDto();
        Mockito.when(mapper.convertValue(null, AbisProjectDto.class)).thenReturn(abisProjectDtoResponse);

        ResponseWrapper<AbisProjectDto> abisProjectDtoResponseWrapper = new ResponseWrapper<>();
        abisProjectDtoResponseWrapper = abisProjectService.updateAbisProject(abisProjectDto);
        Assert.assertNotNull(abisProjectDtoResponseWrapper.getResponse());
    }

    /*
     * This class tests the updateAbisProject method in case of exception
     */
    @Test
    public void updateAbisProjectExceptionTest() {
        String id = "123";
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setId(id);
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectService.updateAbisProject(abisProjectDto);
        abisProjectDto.setUsername("admin");
        abisProjectDto.setPassword("admin123");
        abisProjectDto.setOutboundQueueName("ctk-to-abis");
        abisProjectDto.setInboundQueueName("abis-to-ctk");
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");

        AbisProjectEntity abisProjectEntity = new AbisProjectEntity();
        Optional<AbisProjectEntity> abisProjectEntityOpt = Optional.ofNullable(abisProjectEntity);
        Mockito.when(abisProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(abisProjectEntityOpt);
        abisProjectDto.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);
        abisProjectService.updateAbisProject(abisProjectDto);

        abisProjectDto.setBioTestDataFileName("testFile");
        BiometricTestDataEntity biometricTestData = new BiometricTestDataEntity();
        biometricTestData.setFileId("1234");
        Mockito.when(biometricTestDataRepository.findByTestDataName(Mockito.any(), Mockito.any())).thenReturn(biometricTestData);
        ResponseWrapper<AbisProjectDto> abisProjectDtoResponseWrapper = new ResponseWrapper<>();
        abisProjectService.updateAbisProject(abisProjectDto);
    }

    /*
     * This class tests the isValidAbisProject method
     */
    @Test
    public void isValidAbisProjectTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectDto.setUrl("wss://activemq.dev.mosip.net/ws");
        Boolean result = ReflectionTestUtils.invokeMethod(abisProjectService, "isValidAbisProject", abisProjectDto);
        Assert.assertEquals(result, true);
    }

    /*
     * This class tests the updateAbisProject method in case of exception
     */
    @Test(expected = Exception.class)
    public void isValidAbisProjectExceptionTest() {
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        abisProjectDto.setProjectType(ProjectTypes.ABIS.getCode());
        abisProjectDto.setAbisVersion(AbisSpecVersions.SPEC_VER_0_9_0.getCode());
        abisProjectDto.setUrl(null);
        Boolean result = ReflectionTestUtils.invokeMethod(abisProjectService, "isValidAbisProject", abisProjectDto);
        Assert.assertEquals(result, true);
    }

    /*
     * This method is used to get MosipUserDto in class
     */
    private MosipUserDto getMosipUserDto() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
