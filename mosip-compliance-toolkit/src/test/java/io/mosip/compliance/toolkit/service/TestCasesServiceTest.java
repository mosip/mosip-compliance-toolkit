package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class TestCasesServiceTest {

    @InjectMocks
    private TestCasesService testCasesService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private ResourceCacheService resourceCacheService;

    @Mock
    private TestCaseCacheService testCaseCacheService;

    @Mock
    private TestCasesRepository testCasesRepository;

    @Mock
    private ObjectMapper objectMapper;

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
        ReflectionTestUtils.invokeMethod(testCasesService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest(){
        String result = ReflectionTestUtils.invokeMethod(testCasesService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getSbiTestCases method
     */
    @Test
    public void getSbiTestCases() throws Exception {
        String specVersion = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String purpose = Purposes.REGISTRATION.getCode();
        String deviceType = DeviceTypes.FINGER.getCode();
        String deviceSubType = DeviceSubTypes.SLAP.getCode();
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(schemaResponse);
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseJson("testCaseJson");
        testCaseEntities.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getSbiTestCases(AppConstants.SBI, specVersion)).thenReturn(testCaseEntities);
        TestCasesService testCasesServiceSpy = Mockito.spy(testCasesService);
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        Mockito.doReturn(validationResultDto).when(testCasesServiceSpy).validateJsonWithSchema(testCaseEntity.getTestcaseJson(), schemaResponse);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setInactive(false);
        testCaseDto.setSpecVersion(SbiSpecVersions.SPEC_VER_0_9_5.getCode());
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<Object> purposes = new ArrayList<>();
        purposes.add(Purposes.REGISTRATION.getCode());
        otherAttributes.setPurpose(purposes);
        ArrayList<Object> biometricTypes = new ArrayList<>();
        biometricTypes.add(BiometricType.FINGER.value());
        otherAttributes.setBiometricTypes(biometricTypes);
        ArrayList<Object> deviceSubTypes = new ArrayList<>();
        deviceSubTypes.add(DeviceSubTypes.SLAP.getCode());
        otherAttributes.setDeviceSubTypes(deviceSubTypes);
        testCaseDto.setOtherAttributes(otherAttributes);
        Mockito.when(objectMapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        testCasesServiceSpy.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType);
    }


    /*
     * This class tests the getSdkTestCases method
     */
    @Test
    public void getSdkTestCases() throws Exception {
        String specVersion = SdkSpecVersions.SPEC_VER_0_9_0.getCode();
        String sdkPurpose = SdkPurpose.CHECK_QUALITY.getCode();
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(schemaResponse);
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        testCaseEntity.setTestcaseJson("testCaseJson");
        testCaseEntities.add(testCaseEntity);
        Mockito.when(testCaseCacheService.getSdkTestCases(AppConstants.SDK, specVersion)).thenReturn(testCaseEntities);
        TestCasesService testCasesServiceSpy = Mockito.spy(testCasesService);
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        Mockito.doReturn(validationResultDto).when(testCasesServiceSpy).validateJsonWithSchema(testCaseEntity.getTestcaseJson(), schemaResponse);
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setInactive(false);
        testCaseDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        TestCaseDto.OtherAttributes otherAttributes = new TestCaseDto.OtherAttributes();
        ArrayList<String> sdkPurposes = new ArrayList<>();
        sdkPurposes.add(SdkPurpose.CHECK_QUALITY.getCode());
        otherAttributes.setSdkPurpose(sdkPurposes);
        testCaseDto.setOtherAttributes(otherAttributes);
        Mockito.when(objectMapper.readValue(testCaseEntity.getTestcaseJson(), TestCaseDto.class)).thenReturn(testCaseDto);
        testCasesServiceSpy.getSdkTestCases(specVersion, sdkPurpose);
    }

    /*
     * This class tests the saveTestCases method
     */
    @Test
    public void saveTestCasesTest() throws Exception {
        List<TestCaseDto> values = new ArrayList<>();
        TestCaseDto testCaseDto = new TestCaseDto();
        values.add(testCaseDto);
        testCaseDto.setTestCaseType(AppConstants.SDK);
        testCaseDto.setTestId(AppConstants.SDK + "1000");
        List<String> methodNames = new ArrayList<>();
        methodNames.add(MethodName.MATCH.getCode());
        testCaseDto.setMethodName(methodNames);
        List<String> requestSchemas = new ArrayList<>();
        requestSchemas.add("requestSchema");
        testCaseDto.setRequestSchema(requestSchemas);
        List<String> responseSchemas = new ArrayList<>();
        responseSchemas.add("responseSchema");
        testCaseDto.setResponseSchema(responseSchemas);
        List<List<TestCaseDto.ValidatorDef>> validatorDefs = new ArrayList<>();
        List<TestCaseDto.ValidatorDef> validatorDef = new ArrayList<>();
        TestCaseDto.ValidatorDef validatorDef1 = new TestCaseDto.ValidatorDef();
        validatorDef.add(validatorDef1);
        validatorDefs.add(validatorDef);
        testCaseDto.setValidatorDefs(validatorDefs);
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(schemaResponse);
        String jsonValue = "jsonValue";
        Mockito.when(objectMapper.writeValueAsString(testCaseDto)).thenReturn(jsonValue);
        TestCasesService testCasesServiceSpy = Mockito.spy(testCasesService);
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        Mockito.doReturn(validationResultDto).when(testCasesServiceSpy).validateJsonWithSchema(jsonValue, schemaResponse);
        TestCaseEntity testCaseEntity = new TestCaseEntity();
        //checkTestCaseEntity is empty
        Optional<TestCaseEntity> checkTestCaseEntity = Optional.empty();
        Mockito.when(testCasesRepository.findById(testCaseDto.getTestId())).thenReturn(checkTestCaseEntity);
        testCasesServiceSpy.saveTestCases(values);
        //checkTestCaseEntity
        checkTestCaseEntity = Optional.of(testCaseEntity);
        Mockito.when(testCasesRepository.findById(testCaseDto.getTestId())).thenReturn(checkTestCaseEntity);
        ResponseWrapper<TestCaseResponseDto> testCaseResponseDtoRW= testCasesServiceSpy.saveTestCases(values);
        Assert.assertEquals(null, testCaseResponseDtoRW.getResponse().getTestCases().get(0));
    }

    /*
     * This class tests the saveTestCases in case of Exception method
     */
    @Test
    public void saveTestCasesExceptionTest() throws Exception {
        List<TestCaseDto> values = new ArrayList<>();
        TestCaseDto testCaseDto = new TestCaseDto();
        values.add(testCaseDto);
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(null);
        TestCasesService testCasesServiceSpy = Mockito.spy(testCasesService);
        testCasesServiceSpy.saveTestCases(values);
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(schemaResponse);
        String jsonValue = "jsonValue";
        Mockito.when(objectMapper.writeValueAsString(testCaseDto)).thenReturn(jsonValue);
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.FAILURE);
        Mockito.doReturn(validationResultDto).when(testCasesServiceSpy).validateJsonWithSchema(jsonValue, schemaResponse);
        testCasesServiceSpy.saveTestCases(values);
    }

    /*
     * This method is used to get validateArrayLengths in class
     */
    @Test(expected = Exception.class)
    public void validateArrayLengthsTest1(){
        TestCaseDto testCaseDto = new TestCaseDto();
        List<String> methodNames = new ArrayList<>();
        methodNames.add(MethodName.MATCH.getCode());
        methodNames.add(MethodName.CHECK_QUALITY.getCode());
        testCaseDto.setMethodName(methodNames);
        //type match fail
        testCaseDto.setTestCaseType(AppConstants.SBI);
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
        methodNames.add(MethodName.EXTRACT_TEMPLATE.getCode());
        testCaseDto.setMethodName(methodNames);
        testCaseDto.setTestCaseType(AppConstants.SDK);
        //size>2
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
    }

    /*
     * This method is used to get validateArrayLengths in class
     */
    @Test(expected = Exception.class)
    public void validateArrayLengthsTest2(){
        TestCaseDto testCaseDto = new TestCaseDto();
        List<String> methodNames = new ArrayList<>();
        methodNames.add(MethodName.MATCH.getCode());
        methodNames.add(MethodName.CHECK_QUALITY.getCode());
        methodNames.add(MethodName.EXTRACT_TEMPLATE.getCode());
        testCaseDto.setMethodName(methodNames);
        testCaseDto.setTestCaseType(AppConstants.SDK);
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
    }

    /*
     * This method is used to get validateArrayLengths in class
     */
    @Test(expected = Exception.class)
    public void validateArrayLengthsTest3(){
        TestCaseDto testCaseDto = new TestCaseDto();
        List<String> methodNames = new ArrayList<>();
        methodNames.add(MethodName.MATCH.getCode());
        testCaseDto.setMethodName(methodNames);
        List<String> requestSchemas = new ArrayList<>();
        testCaseDto.setRequestSchema(requestSchemas);
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
    }

    /*
     * This method is used to get validateArrayLengths in class
     */
    @Test(expected = Exception.class)
    public void validateArrayLengthsTest4(){
        TestCaseDto testCaseDto = new TestCaseDto();
        List<String> methodNames = new ArrayList<>();
        testCaseDto.setMethodName(methodNames);
        List<String> requestSchemas = new ArrayList<>();
        testCaseDto.setRequestSchema(requestSchemas);
        List<String> responseSchemas = new ArrayList<>();
        responseSchemas.add("responseSchema1");
        responseSchemas.add("responseSchema2");
        testCaseDto.setResponseSchema(responseSchemas);
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
    }

    /*
     * This method is used to get validateArrayLengths in class
     */
    @Test(expected = Exception.class)
    public void validateArrayLengthsTest5(){
        TestCaseDto testCaseDto = new TestCaseDto();
        List<String> methodNames = new ArrayList<>();
        testCaseDto.setMethodName(methodNames);
        List<String> requestSchemas = new ArrayList<>();
        testCaseDto.setRequestSchema(requestSchemas);
        List<String> responseSchemas = new ArrayList<>();
        testCaseDto.setResponseSchema(responseSchemas);
        List<List<TestCaseDto.ValidatorDef>> validatorDefs = new ArrayList<>();
        List<TestCaseDto.ValidatorDef> validatorDef = new ArrayList<>();
        validatorDefs.add(validatorDef);
        testCaseDto.setValidatorDefs(validatorDefs);
        ReflectionTestUtils.invokeMethod(testCasesService, "validateArrayLengths", testCaseDto);
    }

    /*
     * This method is used to get isValidTestCaseId in class
     */
    @Test(expected = Exception.class)
    public void isValidTestCaseId(){
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestCaseType(AppConstants.SDK);
        testCaseDto.setTestId("SBI1000");
        ReflectionTestUtils.invokeMethod(testCasesService, "isValidTestCaseId", testCaseDto);
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
