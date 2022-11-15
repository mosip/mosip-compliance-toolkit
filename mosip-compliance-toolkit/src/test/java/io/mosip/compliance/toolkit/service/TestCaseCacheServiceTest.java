package io.mosip.compliance.toolkit.service;

import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;


@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
@RunWith(SpringRunner.class)
@WebMvcTest
public class TestCaseCacheServiceTest {

    @InjectMocks
    private TestCaseCacheService testCaseCacheService;

    @Mock
    private TestCasesRepository testCasesRepository;

    @Test
    public void getSbiTestCasesTest() {
        String type = ProjectTypes.SBI.getCode();
        String specVersion = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        testCaseCacheService.getSbiTestCases(type, specVersion);
    }

    @Test
    public void getSdkTestCasesTest() {
        String type = ProjectTypes.SBI.getCode();
        String specVersion = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        testCaseCacheService.getSdkTestCases(type, specVersion);
    }

    @Test
    public void getAbisTestCasesTest() {
        String type = ProjectTypes.SBI.getCode();
        String specVersion = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        testCaseCacheService.getAbisTestCases(type, specVersion);
    }


    @Test
    public void saveTestCaseTest() {
        TestCaseEntity entity = new TestCaseEntity();
        testCaseCacheService.saveTestCase(entity);
    }

    @Test
    public void updateTestCaseTest() {
        TestCaseEntity entity = new TestCaseEntity();
        testCaseCacheService.updateTestCase(entity);
    }
}
