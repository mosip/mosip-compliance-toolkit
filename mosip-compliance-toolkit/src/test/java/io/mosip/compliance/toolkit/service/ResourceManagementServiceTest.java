package io.mosip.compliance.toolkit.service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ResourceManagementServiceTest {

    @InjectMocks
    private ResourceManagementService resourceManagementService;

    @Mock
    VirusScanner<Boolean, InputStream> virusScan;

    @Mock
    private ObjectStoreAdapter objectStore;

    private static final String UNDERSCORE = "_";

    private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SBI;

    private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SDK;


    @Test
    public void uploadResourceFileTest() throws IOException {
        ResponseWrapper<Boolean> response=new ResponseWrapper<>();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = AppConstants.MOSIP_DEFAULT;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, file);

        type = AppConstants.SCHEMAS;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = SBI_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = SDK_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        response = resourceManagementService.uploadResourceFile(type, version, jsonFile);
    }

    @Test
    public void uploadResourceFileTest1() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile txtFile = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = AppConstants.MOSIP_DEFAULT;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResponseWrapper<Boolean> responseWrapper = resourceManagementService.uploadResourceFile(type, version, txtFile);
        Assert.assertEquals(responseWrapper.getResponse(), false);
    }

    @Test
    public void uploadResourceFileTest2() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = SBI_SCHEMA;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResponseWrapper<Boolean> responseWrapper = resourceManagementService.uploadResourceFile(type, version, jsonFile);
        Assert.assertEquals(responseWrapper.getResponse(), false);
    }

    @Test
    public void uploadResourceFileTest3() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = SDK_SCHEMA;;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ReflectionTestUtils.invokeMethod(resourceManagementService,"uploadResourceFile",type,version,jsonFile);
    }

    @Test
    public void uploadResourceFileExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", null, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = null;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResponseWrapper<Boolean> responseWrapper = resourceManagementService.uploadResourceFile(type, version, file);
        Assert.assertEquals(responseWrapper.getResponse(), false);
    }

    @Test
    public void uploadResourceFileExceptionTest1() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "testFile.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(true);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = SBI_SCHEMA;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        ResponseWrapper<Boolean> responseWrapper = resourceManagementService.uploadResourceFile(type, version, file);
    }

    @Test(expected = Exception.class)
    public void isVirusScanSuccessTest() throws Exception{
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.invokeMethod(resourceManagementService,"isVirusScanSuccess",jsonFile);
    }
}
