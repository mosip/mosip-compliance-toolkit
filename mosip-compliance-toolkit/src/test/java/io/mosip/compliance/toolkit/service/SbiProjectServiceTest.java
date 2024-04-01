package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.EncryptionKeyResponseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.restassured.response.ResponseBody;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class SbiProjectServiceTest {

    @InjectMocks
    public SbiProjectService sbiProjectService;

    @Mock
    private SbiProjectRepository sbiProjectRepository;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private ResourceCacheService resourceCacheService;

    @Mock
    KeyManagerHelper keyManagerHelper;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private CollectionsService collectionsService;

    @Mock
    private TestCasesService testCasesService;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Authentication mockAuthentication;

    @Mock
    private AuthUserDetails mockAuthUserDetails;

    @Before
    public void setUp(){
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(mockAuthentication);
    }

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.invokeMethod(sbiProjectService, "authUserDetails");
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
        String result = ReflectionTestUtils.invokeMethod(sbiProjectService, "getPartnerId");
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
        String result = ReflectionTestUtils.invokeMethod(sbiProjectService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the getSbiProject method
     */
    @Test
    public void getSbiProjectTest(){
        String id = "123";
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        SbiProjectEntity sbiProjectEntity = new SbiProjectEntity();
        Optional<SbiProjectEntity> sbiProjectEntityopt = Optional.of(sbiProjectEntity);
        Mockito.when(sbiProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sbiProjectEntityopt);

        ResponseWrapper<SbiProjectDto> result = sbiProjectService.getSbiProject(id);
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        Assert.assertEquals( sbiProjectDto, result.getResponse());
        System.out.println("sbi-get= "+sbiProjectDto);
    }

    /*
     * This class tests the getSbiProject method in case of Exception
     */
    @Test
    public void getSbiProjectTestException(){
        sbiProjectService.getSbiProject("123");
    }

    /*
     * This class tests the getSbiProject method when sbiProjectEntity is null
     */
    @Test
    public void getSbiProjectTestNullEntity(){
        String id = "123";
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        SbiProjectEntity sbiProjectEntity = null;
        Optional<SbiProjectEntity> sbiProjectEntityopt = Optional.ofNullable(sbiProjectEntity);
        Mockito.when(sbiProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(sbiProjectEntityopt);
        sbiProjectService.getSbiProject(id);
    }

    /*
     * This class tests the addSbiProject method
     */
    @Test
    public void addSbiProjectTest(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("123");
        sbiProjectDto.setName("reg sbi");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setOrgName("abc");
        sbiProjectDto.setWebsiteUrl("https://test.com");

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        Mockito.when(resourceCacheService.getOrgName("abc")).thenReturn("abc");
        SecurityContextHolder.setContext(securityContext);

        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        sbiProjectDtoResponseWrapper = sbiProjectService.addSbiProject(sbiProjectDto);
        Assert.assertEquals(sbiProjectDto, sbiProjectDtoResponseWrapper.getResponse());
    }

    @Test
    public void addSbiProjectDefaultCollectionTest(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setId("");
        sbiProjectDto.setName("reg sbi");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setOrgName("abc");
        sbiProjectDto.setWebsiteUrl("https://test.com");

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
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
        testCaseDto.setTestCaseType("SBI");
        testCaseDto.setTestId("SBI1000");
        testCaseDto.setSpecVersion("0.9.5");
        testCaseDto.setTestName("Discover Device");
        testCaseDto.setTestDescription("Test to perform validation for the device discovery interface");
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
        Mockito.when(testCasesService.getSbiTestCases(Mockito.any(),Mockito.any(),Mockito.any()
        ,Mockito.any(), Mockito.any())).thenReturn(testCaseWrapper);


        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        sbiProjectDtoResponseWrapper = sbiProjectService.addSbiProject(sbiProjectDto);
        Assert.assertEquals(sbiProjectDto, sbiProjectDtoResponseWrapper.getResponse());
    }


    /*
     * This class tests the addSbiProject method in case of Exception
     */
    @Test
    public void addSbiProjectTestException(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectService.addSbiProject(sbiProjectDto);
    }

    /*
     * This class tests the addSbiProject method in case of Toolkit Exception
     */
    @Test
    public void addSbiProjectTestToolkitException(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        // Auth:Finger - Double
        sbiProjectDto.setPurpose("Auth");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Double");
        sbiProjectService.addSbiProject(sbiProjectDto);

        // Auth:Iris - Slap
        sbiProjectDto.setDeviceType("Iris");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectService.addSbiProject(sbiProjectDto);

        // Auth:Face - Single
        sbiProjectDto.setDeviceType("Face");
        sbiProjectDto.setDeviceSubType("Single");
        sbiProjectService.addSbiProject(sbiProjectDto);

        // Registration:Finger - Double
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Double");
        sbiProjectService.addSbiProject(sbiProjectDto);

        // Registration:Iris - Slap
        sbiProjectDto.setDeviceType("Iris");
        sbiProjectDto.setDeviceSubType("Full face");
        sbiProjectService.addSbiProject(sbiProjectDto);
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectService.addSbiProject(sbiProjectDto);

        // Registration:Face - Single
        sbiProjectDto.setDeviceType("Face");
        sbiProjectDto.setDeviceSubType("Single");
        sbiProjectService.addSbiProject(sbiProjectDto);
    }

    /*
     * This class tests the addSbiProject method in case of Exception
     */
    @Test
    public void addSbiProjectTestException1(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("123");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        sbiProjectDtoResponseWrapper = sbiProjectService.addSbiProject(sbiProjectDto);
    }

    @Test
    public void addSbiProjectTestException2(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");

        Mockito.when(securityContext.getAuthentication()).thenThrow(new DataIntegrityViolationException("Exception"));
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);

        ResponseWrapper<SbiProjectDto> sbiProjectDtoResponseWrapper = new ResponseWrapper<>();
        sbiProjectDtoResponseWrapper = sbiProjectService.addSbiProject(sbiProjectDto);
    }

    /*
     * This class tests the isValidSbiProject method
     */
    @Test
    public void isValidSbiProjectTest(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
        // Registration:Finger
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
        // Registration:Iris
        sbiProjectDto.setDeviceType("Iris");
        sbiProjectDto.setDeviceSubType("Double");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
        // Registration:Face
        sbiProjectDto.setDeviceType("Face");
        sbiProjectDto.setDeviceSubType("Full face");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);

        // Auth:Finger
        sbiProjectDto.setPurpose("Auth");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
        // Auth:Iris
        sbiProjectDto.setDeviceType("Iris");
        sbiProjectDto.setDeviceSubType("Double");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
        // Auth:Face
        sbiProjectDto.setDeviceType("Face");
        sbiProjectDto.setDeviceSubType("Full face");
        ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
    }

    /*
     * This class tests the isValidSbiProject method in case of exception
     */
    @Test
    public void isValidSbiProjectTestException(){
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");

        // Registration:Finger
        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        Boolean result=ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
        Assert.assertEquals(true,result);
    }

    /*
     * This class tests the isValidSbiProject method in case of exception
     */
    @Test(expected = ToolkitException.class)
    public void isValidSbiProjectTestException1(){
        SbiProjectDto sbiProjectDto=new SbiProjectDto();
        sbiProjectDto.setProjectType("SDK");
        sbiProjectDto.setSbiVersion("0.9.5");

        sbiProjectDto.setPurpose("Registration");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        Boolean result=ReflectionTestUtils.invokeMethod(sbiProjectService, "isValidSbiProject", sbiProjectDto);
    }

    /*
	 * This class tests the getEncryptionKey method
	 */
	@Test
	public void getEncryptionKeyTest() throws IOException {

		EncryptionKeyResponseDto keyResponseDto = new EncryptionKeyResponseDto();
		EncryptionKeyResponseDto.EncryptionKeyResponse encryptionKeyResponse = new EncryptionKeyResponseDto.EncryptionKeyResponse();
		encryptionKeyResponse.setCertificate("abc");
		keyResponseDto.setResponse(encryptionKeyResponse);
		Mockito.when(keyManagerHelper.getCertificate()).thenReturn(keyResponseDto);
		ResponseWrapper<String> result = ReflectionTestUtils.invokeMethod(sbiProjectService, "getEncryptionKey");
		Assert.assertEquals(keyResponseDto.getResponse().getCertificate(), result.getResponse());

	}

    @Test
    public void getEncryptionKeyTestException() throws IOException {

        EncryptionKeyResponseDto keyResponseDto = new EncryptionKeyResponseDto();
        EncryptionKeyResponseDto.EncryptionKeyResponse encryptionKeyResponse = new EncryptionKeyResponseDto.EncryptionKeyResponse();
        encryptionKeyResponse.setCertificate("abc");
        keyResponseDto.setResponse(encryptionKeyResponse);
        Mockito.when(keyManagerHelper.getCertificate()).thenThrow(new IOException());
        ResponseWrapper<String> result = ReflectionTestUtils.invokeMethod(sbiProjectService, "getEncryptionKey");

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

    @Test
    public void updateSbiProjectTest() {
        ResponseWrapper<SbiProjectDto> responseWrapper = new ResponseWrapper<SbiProjectDto>();
        SbiProjectDto sbiProjectDto = new SbiProjectDto();
        SbiProjectEntity sbiProjectEntity = new SbiProjectEntity();
        sbiProjectEntity.setSbiHash("adsadsadsad");
        sbiProjectEntity.setSbiVersion("0.9.5");
        sbiProjectEntity.setDeleted(false);
        sbiProjectEntity.setProjectType("SBI");
        sbiProjectEntity.setCrBy(null);
        sbiProjectEntity.setId("SBI1000");
        sbiProjectEntity.setCrDate(LocalDateTime.now());
        sbiProjectEntity.setDelTime(null);
        sbiProjectEntity.setDeviceImage1("sdadsadsad");
        sbiProjectEntity.setDeviceImage2("wqewqewqe");
        sbiProjectEntity.setDeviceImage3("cxvxcvcxv");
        sbiProjectEntity.setDeviceImage4("jhjhgjhgjhgj");
        sbiProjectEntity.setDeviceType("Finger");
        sbiProjectEntity.setDeviceSubType("Slap");
        sbiProjectEntity.setName("project name");
        sbiProjectEntity.setWebsiteUrl("https://");

        sbiProjectDto.setSbiHash("adsadsadsad");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setCrBy(null);
        sbiProjectDto.setId("SBI1000");
        sbiProjectDto.setCrDate(LocalDateTime.now());
        sbiProjectDto.setDeviceImage1("sdadsadsad");
        sbiProjectDto.setDeviceImage2("wqewqewqe");
        sbiProjectDto.setDeviceImage3("cxvxcvcxv");
        sbiProjectDto.setDeviceImage4("jhjhgjhgjhgj");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setName("project name");
        sbiProjectDto.setWebsiteUrl("https://");
        responseWrapper.setResponse(sbiProjectDto);
        Optional<SbiProjectEntity> optionalSbiProjectEntity = Optional.of(sbiProjectEntity);


        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockAuthUserDetails);
        Mockito.when(sbiProjectRepository.findById(Mockito.anyString(), Mockito.any()))
                .thenReturn(optionalSbiProjectEntity);
        sbiProjectService.updateSbiProject(sbiProjectDto);
    }

    @Test
    public void updateSbiProjectTestElse() {
        SbiProjectDto sbiProjectDto = new SbiProjectDto();

        sbiProjectDto.setSbiHash("adsadsadsad");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setCrBy(null);
        sbiProjectDto.setId("SBI1000");
        sbiProjectDto.setCrDate(LocalDateTime.now());
        sbiProjectDto.setDeviceImage1("sdadsadsad");
        sbiProjectDto.setDeviceImage2("wqewqewqe");
        sbiProjectDto.setDeviceImage3("cxvxcvcxv");
        sbiProjectDto.setDeviceImage4("jhjhgjhgjhgj");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setName("project name");
        sbiProjectDto.setWebsiteUrl("https://");

        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockAuthUserDetails);
        Mockito.when(sbiProjectRepository.findById(Mockito.anyString(), Mockito.any()))
                .thenReturn(Optional.empty());
        sbiProjectService.updateSbiProject(sbiProjectDto);
    }

    @Test
    public void updateSbiProjectTestException() {
        SbiProjectDto sbiProjectDto = new SbiProjectDto();

        sbiProjectDto.setSbiHash("adsadsadsad");
        sbiProjectDto.setSbiVersion("0.9.5");
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setCrBy(null);
        sbiProjectDto.setId("SBI1000");
        sbiProjectDto.setCrDate(LocalDateTime.now());
        sbiProjectDto.setDeviceImage1("sdadsadsad");
        sbiProjectDto.setDeviceImage2("wqewqewqe");
        sbiProjectDto.setDeviceImage3("cxvxcvcxv");
        sbiProjectDto.setDeviceImage4("jhjhgjhgjhgj");
        sbiProjectDto.setDeviceType("Finger");
        sbiProjectDto.setDeviceSubType("Slap");
        sbiProjectDto.setName("project name");
        sbiProjectDto.setWebsiteUrl("https://");

        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockAuthUserDetails);
        Mockito.when(sbiProjectRepository.findById(Mockito.anyString(), Mockito.any()))
                .thenThrow(new ToolkitException("TOOLKIT_EXCEPTION_001","Exception"));
        sbiProjectService.updateSbiProject(sbiProjectDto);
    }
}