package io.mosip.compliance.toolkit.controllers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class BiometricTestDataControllerTest {

    @InjectMocks
    private BiometricTestDataController biometricTestDataController;

    @Mock
    private BiometricTestDataService biometricTestDataService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Errors errors;

    private static final String BIOMETRIC_TESTDATA_POST_ID = "biometric.testdata.post";

    /*
     * This class tests the getListOfBiometricTestData method
     */
    @Test
    public void getListOfBiometricTestDataTest(){
        ResponseWrapper<List<BiometricTestDataDto>> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataService.getListOfBiometricTestData()).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getListOfBiometricTestData());
    }

    /*
     * This class tests the addBiometricTestData
     */
    @Test
    public void addBiometricTestDataTest() throws IOException {
        MultipartFile file = null;
        String strRequestWrapper = "";
        RequestWrapper<BiometricTestDataDto> request = new RequestWrapper<>();
        request.setId(BIOMETRIC_TESTDATA_POST_ID);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(mapper);
        Mockito.when(mapper.readValue(strRequestWrapper, new TypeReference<RequestWrapper<BiometricTestDataDto>>() {
        })).thenReturn(request);
        biometricTestDataController.addBiometricTestData(file, strRequestWrapper, errors);
    }

    /*
     * This class tests the addBiometricTestData method in case of Exception
     */
    @Test
    public void addBiometricTestDataExceptionTest() throws IOException {
        MultipartFile file = null;
        String strRequestWrapper = "";
        RequestWrapper<BiometricTestDataDto> request = new RequestWrapper<>();
        biometricTestDataController.addBiometricTestData(file, strRequestWrapper, errors);
    }

    @Test
    public void getBiometricTestDataFileTest(){
        String id = "123";
        ResponseEntity<Resource> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
        Mockito.when(biometricTestDataService.getBiometricTestDataFile(id)).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getBiometricTestDataFile(id));
    }

    /*
     * This class tests the getBioTestDataFileNames
     */
    @Test
    public void getBioTestDataFileNamesTest(){
        String purpose = "Auth";
        ResponseWrapper<List<String>> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataService.getBioTestDataFileNames(purpose)).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getBioTestDataFileNames(purpose));
    }

    /*
     * This class tests the getDefaultBioTestData
     */
    @Test
    public void getDefaultBioTestDataTest(){
        String purpose = "Auth";
        ResponseEntity<Resource> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
        Mockito.when(biometricTestDataService.getSampleBioTestDataFile(purpose)).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getSampleBioTestDataFile(purpose));
    }

}
