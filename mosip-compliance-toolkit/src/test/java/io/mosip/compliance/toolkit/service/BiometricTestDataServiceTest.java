package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class BiometricTestDataServiceTest {

    @InjectMocks
    private BiometricTestDataService biometricTestDataService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private BiometricTestDataRepository biometricTestDataRepository;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ObjectStoreAdapter objectStore;

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
        ReflectionTestUtils.invokeMethod(biometricTestDataService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest(){
        String result = ReflectionTestUtils.invokeMethod(biometricTestDataService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest(){
        String result = ReflectionTestUtils.invokeMethod(biometricTestDataService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the getListOfBiometricTestData method
     */
    @Test
    public void getListOfBiometricTestDataTest(){
        ResponseWrapper<List<BiometricTestDataDto>> response = new ResponseWrapper<>();
        List<BiometricTestDataEntity> entities = new ArrayList<>();
        BiometricTestDataEntity entity_1= new BiometricTestDataEntity();
        entities.add(entity_1);
        Mockito.when(biometricTestDataRepository.findAllByPartnerId(Mockito.any())).thenReturn(entities);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataDto testDataDto = new BiometricTestDataDto();
        Mockito.when(mapper.convertValue(entity_1, BiometricTestDataDto.class)).thenReturn(testDataDto);
        response = biometricTestDataService.getListOfBiometricTestData();
        Assert.assertNotNull(response.getResponse().get(0));
    }

    /*
     * This class tests the getListOfBiometricTestData method in case of Exception
     */
    @Test
    public void getListOfBiometricTestDataExceptionTest(){
        //when entity is null
        ResponseWrapper<List<BiometricTestDataDto>> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataRepository.findAllByPartnerId(Mockito.any())).thenReturn(null);
        biometricTestDataService.getListOfBiometricTestData();
        //exception case
        List<BiometricTestDataEntity> entities = new ArrayList<>();
        BiometricTestDataEntity entity_1= new BiometricTestDataEntity();
        entities.add(entity_1);
        Mockito.when(biometricTestDataRepository.findAllByPartnerId(Mockito.any())).thenReturn(entities);
        biometricTestDataService.getListOfBiometricTestData();
    }

    /*
     * This class tests the addBiometricTestdata method
     */
    @Test
    public void addBiometricTestdataTest() throws IOException {
        ResponseWrapper<BiometricTestDataDto> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto= new BiometricTestDataDto();
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "NameOfTheFile", "multipart/form-data", inputFile);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        response = biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        Assert.assertNull(response.getResponse());
    }

    /*
     * This class tests the addBiometricTestdata method in case of Exception
     */
    @Test
    public void addBiometricTestdataExceptionTest() throws IOException {
        //when file is null
        ResponseWrapper<List<BiometricTestDataDto>> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto= new BiometricTestDataDto();
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, null);
        //exception case
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "NameOfTheFile", "multipart/form-data", inputFile);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(null);
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        // when account exists in object store
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        //exception case: boolean status
        //status=true
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
    }

    /*
     * This class tests the getBioTestDataFileNames method
     */
    @Test
    public void getBioTestDataFileNamesTest(){
        String purpose = "Auth";
        ResponseWrapper<List<String>> response = new ResponseWrapper<>();
        List<ObjectDto> objectDtoList = new ArrayList<>();
        ObjectDto objectDto = new ObjectDto();
        objectDtoList.add(objectDto);
        Mockito.when(objectStore.getAllObjects(Mockito.any(), Mockito.any())).thenReturn(objectDtoList);
        response = biometricTestDataService.getBioTestDataNames(purpose);
        Assert.assertTrue(response.getResponse().isEmpty());
    }

    /*
     * This class tests the getBioTestDataFileNames method in case of Exception
     */
    @Test
    public void getBioTestDataFileNamesExceptionTest(){
        String purpose = "Auth";
        ResponseWrapper<List<String>> response = new ResponseWrapper<>();
        List<ObjectDto> objectDtoList = new ArrayList<>();
        ObjectDto objectDto = new ObjectDto();
        objectDtoList.add(objectDto);
        Mockito.when(objectStore.getAllObjects(Mockito.any(), Mockito.any())).thenReturn(null);
        biometricTestDataService.getBioTestDataNames(purpose);
        // exception case
        ReflectionTestUtils.setField(biometricTestDataService, "objectStore", null);
        biometricTestDataService.getBioTestDataNames(purpose);
    }

    /*
     * This class tests the uploadSampleBioTestDataFile method
     */
//    @Test
//    public void uploadSampleBioTestDataFileTest() throws IOException {
//        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
//        String type = AppConstants.SAMPLE;
//        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
//        MockMultipartFile file = new MockMultipartFile("file", "NAME_MATCHER.zip", "multipart/form-data", inputFile);
//        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
//        Mockito.when(objectStore.deleteObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
////        response = biometricTestDataService.uploadSampleBioTestDataFile(type, file);
//        Assert.assertFalse(response.getResponse());
//    }

    /*
     * This class tests the uploadSampleBioTestDataFile method in case of Exception
     */
//    @Test
//    public void uploadSampleBioTestDataFileExceptionTest() throws IOException {
//        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
//        String type = AppConstants.MOSIP_DEFAULT;
//        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
//        MockMultipartFile file = new MockMultipartFile("file", "NAME_MATCHER_SAMPLE.zip", "multipart/form-data", inputFile);
////        biometricTestDataService.uploadSampleBioTestDataFile(type, file);
//        type = AppConstants.FINGER;
////        biometricTestDataService.uploadSampleBioTestDataFile(type, file);
//    }

    /*
     * This class tests the getSampleBioTestDataFile method
     */
    @Test
    public void getSampleBioTestDataFileTest() throws IOException {
        String purpose = SdkPurpose.CHECK_QUALITY.getCode();
        InputStream inputStream = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(inputStream);
        biometricTestDataService.getSampleBioTestDataFile(purpose);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        ResponseEntity<Resource> response = biometricTestDataService.getSampleBioTestDataFile(purpose);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    /*
     * This class tests the getSampleBioTestDataFile method in case of Exception
     */
    @Test
    public void getSampleBioTestDataFileExceptionTest() throws IOException {
        String purpose = SdkPurpose.CHECK_QUALITY.getCode();
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
        biometricTestDataService.getSampleBioTestDataFile(purpose);
    }

    /*
     * This class tests the getBiometricTestDataFile method
     */
    @Test
    public void getBiometricTestDataFileTest() throws IOException {
        String bioTestDataId = "123abc";
        BiometricTestDataEntity biometricTestDataEntity = new BiometricTestDataEntity();
        biometricTestDataEntity.setFileId("123");
        biometricTestDataEntity.setPurpose("Matcher");
        Mockito.when(biometricTestDataRepository.findById(Mockito.anyString(), Mockito.anyString())).thenReturn(biometricTestDataEntity);
        InputStream inputStream = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(inputStream);
        biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
        Mockito.when(biometricTestDataRepository.findById(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        ResponseEntity<Resource> response = biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    /*
     * This class tests the getBiometricTestDataFile method in case of Exception
     */
    @Test
    public void getBiometricTestDataFileExceptionTest() throws IOException {
        String bioTestDataId = "123abc";
        BiometricTestDataEntity biometricTestDataEntity = new BiometricTestDataEntity();
        biometricTestDataEntity.setFileId("123");
        biometricTestDataEntity.setPurpose("Matcher");
        Mockito.when(biometricTestDataRepository.findById(Mockito.anyString(), Mockito.anyString())).thenReturn(biometricTestDataEntity);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(null);
        biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
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
