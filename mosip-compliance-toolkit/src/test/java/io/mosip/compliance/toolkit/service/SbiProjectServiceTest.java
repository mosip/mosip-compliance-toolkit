package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.EncryptionKeyResponseDto;
import io.mosip.compliance.toolkit.dto.projects.SbiProjectDto;
import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.SbiProjectRepository;
import io.mosip.compliance.toolkit.util.KeyManagerHelper;
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

import java.io.IOException;
import java.util.Optional;

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
    KeyManagerHelper keyManagerHelper;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

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
        sbiProjectDto.setProjectType("SBI");
        sbiProjectDto.setSbiVersion("0.9.5");
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
    @Test(expected = Exception.class)
    public void getEncryptionKeyTest() throws IOException {
        io.restassured.response.Response response=keyManagerHelper.encryptionKeyResponse();
        EncryptionKeyResponseDto keyResponseDto=new EncryptionKeyResponseDto();
        EncryptionKeyResponseDto.EncryptionKeyResponse encryptionKeyResponse=null;
        encryptionKeyResponse.setCertificate("abc");
        keyResponseDto.setResponse(encryptionKeyResponse);
        Mockito.when(objectMapperConfig.objectMapper().readValue(response.getBody().asString(),EncryptionKeyResponseDto.class)).thenReturn(keyResponseDto);
        sbiProjectService.getEncryptionKey();

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
