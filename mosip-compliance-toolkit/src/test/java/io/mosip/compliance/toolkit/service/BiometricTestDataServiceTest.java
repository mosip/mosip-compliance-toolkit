package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.SdkSpecVersions;
import io.mosip.compliance.toolkit.dto.AddBioTestDataResponseDto;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
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
import org.springframework.dao.DataIntegrityViolationException;
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
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;

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
    VirusScanner<Boolean, InputStream> virusScan;

    @Mock
    private ObjectStoreAdapter objectStore;

    @Mock
    private TestCaseCacheService testCaseCacheService;

    private MosipUserDto mosipUserDto;

    private String ignoreTestcases = "";

    private String purposeAbis = "ABIS";

    private String ignoreAbisTestcases = "";

    @Before
    public void before(){
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.setField(biometricTestDataService, "scanDocument", true);
        ReflectionTestUtils.setField(biometricTestDataService, "maxAllowedGalleryFiles", "5");
        ReflectionTestUtils.setField(biometricTestDataService, "ignoreTestcases", ignoreTestcases);
        ReflectionTestUtils.setField(biometricTestDataService, "ignoreAbisTestcases", ignoreAbisTestcases);
        ReflectionTestUtils.setField(biometricTestDataService, "sdkSampleTestdataSpecVer", SdkSpecVersions.SPEC_VER_0_9_0.getCode());
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
    public void addBiometricTestdataAbisTypeError() throws IOException {
        ResponseWrapper<AddBioTestDataResponseDto> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto = new BiometricTestDataDto();
        biometricTestDataDto.setPurpose(purposeAbis);

        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFileABIS.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFileABIS.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        List<TestCaseEntity> testCases = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseType(purposeAbis);
        testCaseEntity.setTestcaseJson("");
        testCases.add(testCaseEntity);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("ABIS3001");
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        testCaseDto.setOtherAttributes(null);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(testCaseCacheService.getAbisTestCases(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(testCases);
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(true);
        response = biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        assertNotNull(response);
        Assert.assertEquals(false, response.getErrors().isEmpty());
        Assert.assertEquals(1, response.getErrors().size());
        List<ServiceError> serviceErrorsList = new ArrayList<>();
        ServiceError serviceError = new ServiceError();
        serviceError.setErrorCode("TOOLKIT_TESTDATA_ERR_006,TOOLKIT_TESTDATA_ERR_004,ABIS3000");
        serviceError.setMessage("Testdata has invalid folder ABIS3000");
        serviceErrorsList.add(serviceError);
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(serviceErrorsList, response.getErrors());
    }

    @Test
    public void addBiometricTestdataAbisTypeException() throws IOException {
        ResponseWrapper<AddBioTestDataResponseDto> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto = new BiometricTestDataDto();
        biometricTestDataDto.setPurpose(purposeAbis);

        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFileABIS.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFileABIS.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        List<TestCaseEntity> testCases = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseType(purposeAbis);
        testCaseEntity.setTestcaseJson("");
        testCases.add(testCaseEntity);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("ABIS3001");
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        testCaseDto.setOtherAttributes(null);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenThrow(new DataIntegrityViolationException("Exception"));
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(testCaseCacheService.getAbisTestCases(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(testCases);
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(true);
        response = biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
    }
    @Test
    public void addBiometricTestdataABIS() throws IOException {
        ResponseWrapper<AddBioTestDataResponseDto> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto = new BiometricTestDataDto();
        biometricTestDataDto.setPurpose(purposeAbis);

        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFile.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        List<TestCaseEntity> testCases = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseType(purposeAbis);
        testCaseEntity.setTestcaseJson("");
        testCases.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getAbisTestCases(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(testCases);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("ABIS3001");
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        testCaseDto.setOtherAttributes(null);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn(true);
        response = biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        assertNotNull(response);
        Assert.assertEquals(false, response.getErrors().isEmpty());
        Assert.assertEquals(1, response.getErrors().size());
    }

    @Test
    public void addBiometricTestdataTest() throws IOException {
        ResponseWrapper<AddBioTestDataResponseDto> response = new ResponseWrapper<>();
        BiometricTestDataDto biometricTestDataDto= new BiometricTestDataDto();
        biometricTestDataDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());

        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFile.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        List<TestCaseEntity> testCases = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseJson("");
        testCases.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getSdkTestCases(Mockito.anyString(), Mockito.anyString())).thenReturn(testCases);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("SDK2001");
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<String> sdkPurpose = new ArrayList<>();
        sdkPurpose.add(SdkPurpose.CHECK_QUALITY.getCode());
        otherAttributes.setSdkPurpose(sdkPurpose);
        testCaseDto.setOtherAttributes(otherAttributes);
        List<String> methodName = new ArrayList<>();
        methodName.add(MethodName.CHECK_QUALITY.getCode());
        methodName.add(MethodName.MATCH.getCode());
        testCaseDto.setMethodName(methodName);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        response = biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
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
        biometricTestDataDto.setPurpose(SdkPurpose.CHECK_QUALITY.getCode());
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        MockMultipartFile file = new MockMultipartFile("file", "testFile.zip", "multipart/form-data", inputFile);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        //when objectStore.exists is true
        biometricTestDataService.addBiometricTestdata(null, file);
        List<TestCaseEntity> testCases = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseJson("");
        testCases.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getSdkTestCases(Mockito.anyString(), Mockito.anyString())).thenReturn(testCases);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId("SDK2001");
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<String> sdkPurpose = new ArrayList<>();
        sdkPurpose.add(SdkPurpose.CHECK_QUALITY.getCode());
        otherAttributes.setSdkPurpose(sdkPurpose);
        testCaseDto.setOtherAttributes(otherAttributes);
        List<String> methodName = new ArrayList<>();
        methodName.add(MethodName.CHECK_QUALITY.getCode());
        testCaseDto.setMethodName(methodName);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        BiometricTestDataEntity inputEntity = new BiometricTestDataEntity();
        Mockito.when(mapper.convertValue(biometricTestDataDto, BiometricTestDataEntity.class)).thenReturn(inputEntity);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        biometricTestDataService.addBiometricTestdata(biometricTestDataDto, file);
        //status = false
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
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
     * This class tests the getSampleBioTestDataFile method
     */
    @Test
    public void getSampleBioTestDataFileABIS() throws IOException {
        String testId = "ABIS3000";
        String purpose = purposeAbis;
        InputStream inputStream = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntities.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getAbisTestCases(Mockito.anyString(), Mockito.anyString())).thenReturn(null);

        biometricTestDataService.getSampleBioTestDataFile(purpose);
        Mockito.when(testCaseCacheService.getAbisTestCases(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(testCaseEntities);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestId(testId);
        testCaseDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);

        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(inputStream);
        biometricTestDataService.getSampleBioTestDataFile(purpose);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(false);
        ResponseEntity<Resource> response = biometricTestDataService.getSampleBioTestDataFile(purpose);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }
    
    @Test
    public void getSampleBioTestDataFileTest() throws IOException {
        String testId = "SDK2001";
        String purpose = SdkPurpose.CHECK_QUALITY.getCode();
        InputStream inputStream = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setId("Testcase001");
        testCaseEntity.setSpecVersion("0.9.5");
        testCaseEntity.setTestcaseType("SDK");
        testCaseEntity.setTestcaseJson("{sadsadd:sadsadasd}");
        testCaseEntities.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getSdkTestCases(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        //when testCaseEntities = null
        biometricTestDataService.getSampleBioTestDataFile(purpose);
        Mockito.when(testCaseCacheService.getSdkTestCases(Mockito.anyString(), Mockito.anyString())).thenReturn(testCaseEntities);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        TestCaseDto testCaseDto = new TestCaseDto();
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<String> sdkPurpose = new ArrayList<>();
        sdkPurpose.add(SdkPurpose.CHECK_QUALITY.getCode());
        otherAttributes.setSdkPurpose(sdkPurpose);
        testCaseDto.setOtherAttributes(otherAttributes);
        testCaseDto.setTestId(testId);
        testCaseDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        testCaseDto.setTestCaseType("SDK");
        List<String> methodName = new ArrayList<>();
        methodName.add(MethodName.CHECK_QUALITY.getCode());
        methodName.add(MethodName.MATCH.getCode());
        testCaseDto.setMethodName(methodName);
        Mockito.when(mapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);

        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(inputStream);
        biometricTestDataService.getSampleBioTestDataFile(purpose);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        ResponseEntity<Resource> response = biometricTestDataService.getSampleBioTestDataFile(purpose);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
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
        Mockito.when(biometricTestDataRepository.findById(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
        // biometricTestDataEntity = null
        biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
        Mockito.when(biometricTestDataRepository.findById(Mockito.anyString(), Mockito.anyString())).thenReturn(biometricTestDataEntity);
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResponseEntity<Resource> response = biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /*
     * This class tests the getBiometricTestDataFile method in case of Exception
     */
    @Test
    public void getBiometricTestDataFileExceptionTest() throws IOException {
        String bioTestDataId = "123";
        ReflectionTestUtils.setField(biometricTestDataService, "biometricTestDataRepository", null);
        biometricTestDataService.getBiometricTestDataFile(bioTestDataId);
    }

    /*
     * This class tests the prepareReadMe
     */
    @Test
    public void prepareReadMeTest(){
        ReflectionTestUtils.setField(biometricTestDataService, "readmeIntro", "");
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setNegativeTestcase(true);
        testCaseDto.setTestName("name");
        testCaseDto.setTestId("123");
        testCaseDto.setTestDescription("description");
        testCaseDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<String> sdkPurpose = new ArrayList<>();
        sdkPurpose.add(SdkPurpose.CHECK_QUALITY.getCode());
        otherAttributes.setSdkPurpose(sdkPurpose);
        testCaseDto.setOtherAttributes(otherAttributes);
        ArrayList<String> modalities = new ArrayList<>();
        testCaseDto.otherAttributes.setModalities(modalities);
        List<TestCaseDto.ValidatorDef> validatorDefs = new ArrayList<>();
        TestCaseDto.ValidatorDef validatorDef = new TestCaseDto.ValidatorDef();
        validatorDef.setName("name");
        validatorDef.setDescription("description");
        validatorDefs.add(validatorDef);
        List<List<TestCaseDto.ValidatorDef>> validatorDefsList = new ArrayList<>();
        validatorDefsList.add(validatorDefs);
        testCaseDto.setValidatorDefs(validatorDefsList);
        List<String> methodName = new ArrayList<>();
        methodName.add(MethodName.CHECK_QUALITY.getCode());
        methodName.add(MethodName.MATCH.getCode());
        testCaseDto.setMethodName(methodName);
        ReflectionTestUtils.invokeMethod(biometricTestDataService, "prepareReadme", testCaseDto);
    }
    /*
     * This class tests the prepareReadMe
     */
    @Test
    public void generateSampleSdkTestDataExceptionTest(){
        String purpose = SdkPurpose.CHECK_QUALITY.getCode();
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        List<String> ignoreTestcasesList = new ArrayList<>();
        ZipOutputStream zipOutputStream = null;
        String fileName = "Readme.txt";
        ReflectionTestUtils.setField(biometricTestDataService, "testCaseCacheService", null);
        ReflectionTestUtils.invokeMethod(biometricTestDataService, "generateSampleSdkTestData", purpose,testCaseEntities,ignoreTestcasesList,zipOutputStream,fileName);
    }

    @Test
    public void generateSampleTestDataTest(){
        String purpose = SdkPurpose.CHECK_QUALITY.getCode();
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        List<String> ignoreTestcasesList = new ArrayList<>();
        ZipOutputStream zipOutputStream = null;
        String fileName = "Readme.txt";
        ReflectionTestUtils.setField(biometricTestDataService, "testCaseCacheService", null);
        ReflectionTestUtils.invokeMethod(biometricTestDataService, "generateSampleSdkTestData", purpose,testCaseEntities,ignoreTestcasesList,zipOutputStream,fileName);
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
