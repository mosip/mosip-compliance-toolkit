package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
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

    @Test
    public void getBiometricTestdataTest(){
        ResponseWrapper<List<BiometricTestDataDto>> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataService.getBiometricTestdata()).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getBiometricTestdata());
    }

    @Test
    public void addBiometricTestDataExceptionTest() throws IOException {
        MultipartFile file = null;
        String strRequestWrapper = "";
        RequestWrapper<BiometricTestDataDto> request = new RequestWrapper<>();
        biometricTestDataController.addBiometricTestData(file, strRequestWrapper, null);
    }

    @Test
    public void getBioTestDataFileNamesTest(){
        ResponseWrapper<List<String>> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataService.getBioTestDataFileNames()).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getBioTestDataFileNames());
    }

    @Test
    public void getDefaultBioTestDataTest(){
        ResponseEntity<Resource> response = new ResponseEntity<>(HttpStatus.ACCEPTED);
        Mockito.when(biometricTestDataService.getDefaultBioTestData()).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.getDefaultBioTestData());
    }

    @Test
    public void addDefaultBioTestDataTest(){
        MultipartFile file = null;
        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
        Mockito.when(biometricTestDataService.addDefaultBioTestData(file)).thenReturn(response);
        Assert.assertEquals(response, biometricTestDataController.addDefaultBioTestData(file));
    }
}
