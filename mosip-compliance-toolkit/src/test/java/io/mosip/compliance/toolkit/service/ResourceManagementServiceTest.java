package io.mosip.compliance.toolkit.service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ResourceManagementServiceTest {

    @InjectMocks
    private ResourceManagementService resourceManagementService;

    @Mock
    private ObjectStoreAdapter objectStore;

    private static final String UNDERSCORE = "_";

    private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SBI;

    private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SDK;


    @Test
    public void uploadResourceFileTest() throws IOException {
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile txtFile = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = AppConstants.MOSIP_DEFAULT;
        resourceManagementService.uploadResourceFile(type, version, txtFile);

        type = AppConstants.SCHEMAS;
        inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = SBI_SCHEMA;
        inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);
        type = SDK_SCHEMA;

        inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ResponseWrapper<Boolean> responseWrapper =  resourceManagementService.uploadResourceFile(type, version, jsonFile);
        Assert.assertEquals(responseWrapper.getResponse(), false);
    }

    @Test
    public void uploadResourceFileExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", null, "multipart/form-data", inputFile);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = null;
        resourceManagementService.uploadResourceFile(type, version, file);
        type = "DEFAULT";
        resourceManagementService.uploadResourceFile(type, version, file);
        type = AppConstants.MOSIP_DEFAULT;
        resourceManagementService.uploadResourceFile(type, version, file);
        type = AppConstants.SCHEMAS;
        resourceManagementService.uploadResourceFile(type, version, file);
        type = SBI_SCHEMA;
        resourceManagementService.uploadResourceFile(type, version, file);
        type = SDK_SCHEMA;
        resourceManagementService.uploadResourceFile(type, version, file);
        inputFile.close();
        inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        file = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "objectStore", null);
        resourceManagementService.uploadResourceFile(type, version, file);
    }
}
