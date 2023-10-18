package io.mosip.compliance.toolkit.service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import io.mosip.compliance.toolkit.dto.report.PartnerDetailsDto;
import io.mosip.compliance.toolkit.util.PartnerManagerHelper;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.*;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ResourceCacheServiceTest {

    @InjectMocks
    private ResourceCacheService resourceCacheService;

    @Mock
    private ObjectStoreAdapter objectStore;

    @Mock
    PartnerManagerHelper partnerManagerHelper;

    @Mock
    private InputStream inputStream;

    @Test
    public void getSchemaTest() throws Exception {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";

        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceCacheService.getSchema(null,version,fileName);
        resourceCacheService.getSchema(type,version,fileName);
    }

    @Test(expected = Exception.class)
    public void getSchemaTest1() throws Exception {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        InputStream input = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        OngoingStubbing<InputStream> inputStreamOngoingStubbing = Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(inputStream);
        resourceCacheService.getSchema(type, version, fileName);
    }

    @Test(expected = Exception.class)
    public void getSchemaExceptionTest() throws Exception {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        InputStream input = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(input);
        resourceCacheService.getSchema(type, version, fileName);
    }

    @Test
    public void putSchemaTest() throws FileNotFoundException {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";
        InputStream input = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.txt");
        resourceCacheService.putSchema(null,version,fileName,inputStream);
        resourceCacheService.putSchema(type,version,fileName,inputStream);
    }

    @Test
    public void getOrgNameTest() throws IOException {
        String partnerId = "abc";
        PartnerDetailsDto partnerDetailsDto = new PartnerDetailsDto();
        PartnerDetailsDto.Partner partner = new PartnerDetailsDto.Partner();
        partner.setOrganizationName("abc");
        partnerDetailsDto.setId("123");
        partnerDetailsDto.setResponse(partner);
        Mockito.when(partnerManagerHelper.getPartnerDetails(partnerId)).thenReturn(partnerDetailsDto);
        String result = resourceCacheService.getOrgName(partnerId);
        assertEquals("abc", result);
    }

    @Test
    public void getOrgNameTestDefault() throws IOException {
        String partnerId = null;
        String result = resourceCacheService.getOrgName(partnerId);
        assertEquals("Not_Available", result);
    }
}
