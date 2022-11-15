package io.mosip.compliance.toolkit.service;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class ResourceCacheServiceTest {

    @InjectMocks
    private ResourceCacheService resourceCacheService;

    @Mock
    private ObjectStoreAdapter objectStore;

    @Mock
    private InputStream inputStream;

    @Test
    public void getSchemaTest() throws IOException {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        resourceCacheService.getSchema(null,version,fileName);
        resourceCacheService.getSchema(type,version,fileName);
    }

    @Test
    public void putSchemaTest() throws FileNotFoundException {
        String type = ProjectTypes.SBI.getCode();
        String version = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String fileName = "testFile";
        resourceCacheService.putSchema(null,version,fileName,inputStream);
        resourceCacheService.putSchema(type,version,fileName,inputStream);
    }
}
