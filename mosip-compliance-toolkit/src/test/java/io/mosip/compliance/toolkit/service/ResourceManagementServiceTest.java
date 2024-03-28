package io.mosip.compliance.toolkit.service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.AbisSpecVersions;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.compliance.toolkit.entity.MasterTemplatesEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    MasterTemplatesRepository masterTemplatesRepository;

    private static final String UNDERSCORE = "_";

    private static final String SBI_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SBI;

    private static final String SDK_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.SDK;

    private static final String ABIS_SCHEMA = AppConstants.SCHEMAS + UNDERSCORE + AppConstants.ABIS;


    @Test
    public void uploadResourceFileTest() throws IOException {
        ResponseWrapper<Boolean> response=new ResponseWrapper<>();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String type = AppConstants.MOSIP_DEFAULT;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, file);

        type = AppConstants.SCHEMAS;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = SBI_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = SDK_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        response = resourceManagementService.uploadResourceFile(type, version, jsonFile);

        type = ABIS_SCHEMA;
        version = AbisSpecVersions.SPEC_VER_0_9_0.getCode();
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, jsonFile);
    }

    @Test
    public void uploadResourceFileTest1() throws IOException {
        ResponseWrapper<Boolean> responseWrapper=new ResponseWrapper<>();
        String type = AppConstants.MOSIP_DEFAULT;
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

        type = AppConstants.SCHEMAS;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

        type = SBI_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

        type = SDK_SCHEMA;
        inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        jsonFile = new MockMultipartFile("file", "testFile.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

    }

    @Test
    public void uploadResourceFileTest2() throws IOException {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        String type = SBI_SCHEMA;
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "schema.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper = resourceManagementService.uploadResourceFile(type, version, multipartFile);
    }

    @Test
    public void uploadResourceFileTest3() throws IOException {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        String type = AppConstants.SCHEMAS;
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "testcase_schema.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper = resourceManagementService.uploadResourceFile(type, version, multipartFile);
    }

    @Test
    public void uploadResourceFileTest4() throws IOException {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        String type = SDK_SCHEMA;
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "schema.json", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper = resourceManagementService.uploadResourceFile(type, version, multipartFile);
    }

    @Test
    public void uploadResourceFileTest5() throws IOException {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        String type = null;
        String version = null;
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", null, "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper = resourceManagementService.uploadResourceFile(type, version, multipartFile);
    }

    @Test
    public void uploadResourceFileTest6() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "MOSIP_DEFAULT_ABIS_FACE.zip", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        String version = AbisSpecVersions.SPEC_VER_0_9_0.getCode();
        String type = AppConstants.MOSIP_DEFAULT;
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceManagementService.uploadResourceFile(type, version, file);
    }

    @Test
    public void uploadResourceFileExceptionTest() throws IOException {
        ResponseWrapper<Boolean> responseWrapper=new ResponseWrapper<>();
        String type = AppConstants.MOSIP_DEFAULT;
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", null, "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

    }

    @Test
    public void uploadResourceFileExceptionTest1() throws IOException {
        ResponseWrapper<Boolean> responseWrapper=new ResponseWrapper<>();
        String type = SBI_SCHEMA;
        String version = null;
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

    }

    @Test
    public void uploadResourceFileExceptionTest2() throws IOException {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        String type = "abc";
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper = resourceManagementService.uploadResourceFile(type, version, multipartFile);
    }

    @Test
    public void uploadResourceFileExceptionTest3() throws IOException {
        ResponseWrapper<Boolean> responseWrapper=new ResponseWrapper<>();
        String type = ABIS_SCHEMA;
        String version = AbisSpecVersions.SPEC_VER_0_9_0.getCode();
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile multipartFile = new MockMultipartFile("file", null, "multipart/form-data", inputFile);
        ReflectionTestUtils.setField(resourceManagementService,"scanDocument",false);
        Mockito.when(objectStore.putObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        responseWrapper=resourceManagementService.uploadResourceFile(type,version,multipartFile);

    }

    @Test(expected = Exception.class)
    public void isVirusScanSuccessTest() throws Exception{
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        MockMultipartFile file = new MockMultipartFile("file", "MOSIP_DEFAULT_CHECK_QUALITY.zip", "multipart/form-data", inputFile);
        ReflectionTestUtils.invokeMethod(resourceManagementService,"isVirusScanSuccess",file);
    }

    @Test
    public void uploadTemplateTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.vm";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = "eng";
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MasterTemplatesEntity masterTemplateEntity = new MasterTemplatesEntity();
        when(masterTemplatesRepository.save(masterTemplateEntity)).thenReturn(masterTemplateEntity);
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateTest1() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.vm";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = "eng";
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MasterTemplatesEntity masterTemplateEntity = new MasterTemplatesEntity();
        when(masterTemplatesRepository.save(masterTemplateEntity)).thenReturn(masterTemplateEntity);
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateInvalidBodyExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.zip";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = "eng";
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateInvalidParamExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.zip";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = null;
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateVirusExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.zip";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", true);
        String langcode = "eng";
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateMultipleExtensionExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template.zip.jar";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = "eng";
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    @Test
    public void uploadTemplateNoExtensionExceptionTest() throws IOException {
        FileInputStream inputFile = new FileInputStream("src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        String templateName = "terms_and_conditions_template";
        String version = "v1";
        MockMultipartFile file = new MockMultipartFile("file", templateName, "multipart/form-data", inputFile);
        Mockito.when(virusScan.scanDocument((byte[]) Mockito.any())).thenReturn(false);
        ReflectionTestUtils.setField(resourceManagementService, "scanDocument", false);
        String langcode = "eng";
        resourceManagementService.uploadTemplate(langcode, templateName, version, file);
    }

    private MosipUserDto getMosipUserDto() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
