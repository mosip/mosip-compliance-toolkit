package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.*;
import io.mosip.compliance.toolkit.dto.testcases.*;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
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
    private ApplicationContext context;

    Gson gson = new GsonBuilder().create();

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    public ObjectMapperConfig objectMapperConfig;

    @Mock
    public ObjectStoreAdapter objectStore;

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
    public void getSbiTestCasesTest() throws Exception {
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
     * This class tests the getSbiTestCases method in case of exception
     */
    @Test
    public void getSbiTestCasesExceptionTest() throws Exception {
        //toolkit exception
        String specVersion = SbiSpecVersions.SPEC_VER_0_9_5.getCode();
        String purpose = Purposes.REGISTRATION.getCode();
        String deviceType = DeviceTypes.FINGER.getCode();
        String deviceSubType = DeviceSubTypes.SLAP.getCode();
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(null);
        testCasesService.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType);
        //exception
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn( schemaResponse);
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        testCaseEntities.add(null);
        Mockito.when(testCaseCacheService.getSbiTestCases(AppConstants.SBI, specVersion)).thenReturn(testCaseEntities);
        testCasesService.getSbiTestCases(specVersion, purpose, deviceType, deviceSubType);
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
     * This class tests the getSdkTestCases method in case of exception
     */
    @Test
    public void getSdkTestCasesExceptionTest() throws Exception {
        //toolkit exception
        String specVersion = SdkSpecVersions.SPEC_VER_0_9_0.getCode();
        String sdkPurpose = SdkPurpose.CHECK_QUALITY.getCode();
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn(null);
        testCasesService.getSdkTestCases(specVersion, sdkPurpose);
        //exception
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(null, null, "testcase_schema.json")).thenReturn( schemaResponse);
        List<TestCaseEntity> testCaseEntities = new ArrayList<>();
        testCaseEntities.add(null);
        Mockito.when(testCaseCacheService.getSdkTestCases(AppConstants.SDK, specVersion)).thenReturn(testCaseEntities);
        testCasesService.getSdkTestCases(specVersion, sdkPurpose);
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
     * This class tests the validArrayLengths method
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
     * This class tests the validArrayLengths method
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
     * This class tests the validArrayLengths method
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
     * This class tests the validArrayLengths method
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
     * This class tests the validArrayLengths method
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
     * This class tests the isValidTestCaseId method
     */
    @Test(expected = Exception.class)
    public void isValidTestCaseIdTest(){
        TestCaseDto testCaseDto = new TestCaseDto();
        testCaseDto.setTestCaseType(AppConstants.SDK);
        testCaseDto.setTestId("SBI1000");
        ReflectionTestUtils.invokeMethod(testCasesService, "isValidTestCaseId", testCaseDto);
    }

    /*
     * This class tests the performRequestValidations method
     */
    @Test
    public void performRequestValidationsTest() throws Exception {
        //type SDK
        ValidateRequestSchemaDto requestDto = new ValidateRequestSchemaDto();
        requestDto.setTestCaseType(AppConstants.SDK);
        requestDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(AppConstants.SDK.toLowerCase(), requestDto.getSpecVersion(), requestDto.getRequestSchema()+".json")).thenReturn(schemaResponse);
        TestCasesService testCasesServiceSpy = Mockito.spy(testCasesService);
        ValidationResultDto validationResultDto = new ValidationResultDto();
        validationResultDto.setStatus(AppConstants.SUCCESS);
        Mockito.doReturn(validationResultDto).when(testCasesServiceSpy).validateJsonWithSchema(requestDto.getMethodRequest(), schemaResponse);
        testCasesService.performRequestValidations(requestDto);
        //type SBI
        requestDto.setTestCaseType(AppConstants.SBI);
        requestDto.setSpecVersion(SbiSpecVersions.SPEC_VER_0_9_5.getCode());
        Mockito.when(resourceCacheService.getSchema(AppConstants.SBI.toLowerCase(), requestDto.getSpecVersion(), requestDto.getRequestSchema()+".json")).thenReturn(schemaResponse);
        Assert.assertEquals(AppConstants.SUCCESS, testCasesServiceSpy.performRequestValidations(requestDto).getResponse().getStatus());
    }

    /*
     * This class tests the performRequestValidations method in case of exception
     */
    @Test
    public void performRequestValidationsExceptionTest() throws Exception {
        ValidateRequestSchemaDto requestDto = new ValidateRequestSchemaDto();
        requestDto.setRequestSchema("requestSchema");
        requestDto.setTestCaseType(AppConstants.SDK);
        requestDto.setSpecVersion(SdkSpecVersions.SPEC_VER_0_9_0.getCode());
        String schemaResponse = "schemaResponse";
        Mockito.when(resourceCacheService.getSchema(AppConstants.SDK.toLowerCase(), requestDto.getSpecVersion(), requestDto.getRequestSchema()+".json")).thenReturn(schemaResponse);
        testCasesService.performRequestValidations(requestDto);
    }

    /*
     * This class tests the performValidations method
     */
    @Test
    public void performValidationsTest(){
        ValidationInputDto requestDto = new ValidationInputDto();
        List<ValidatorDefDto> validatorDefs = new ArrayList<>();
        ValidatorDefDto validatorDefDto = new ValidatorDefDto();
        validatorDefDto.setName("ResponseMismatchValidator");
        validatorDefDto.setDescription("Description");
        validatorDefs.add(validatorDefDto);
        requestDto.setValidatorDefs(validatorDefs);
        testCasesService.performValidations(requestDto);
    }

    /*
     * This class tests the performValidations method in case of exception
     */
    @Test
    public void performValidationsException(){
        ValidationInputDto requestDto = new ValidationInputDto();
        requestDto.setValidatorDefs(null);
        testCasesService.performValidations(requestDto);
    }

    /*
     * This class tests the getBiometricType
     */
    @Test
    public void getBiometricTypeTest(){
        String type = AppConstants.FINGER;
        ReflectionTestUtils.invokeMethod(testCasesService, "getBiometricType", type);
        type = AppConstants.FACE;
        ReflectionTestUtils.invokeMethod(testCasesService, "getBiometricType", type);
        type = AppConstants.IRIS;
        BiometricType biometricType = ReflectionTestUtils.invokeMethod(testCasesService, "getBiometricType", type);
        Assert.assertEquals(BiometricType.IRIS, biometricType);
    }

    /*
     * This class tests the getTestCaseById
     */
    @Test
    public void getTestCaseByIdTest() throws JsonProcessingException {
        String testCaseId = "SBI1000";
        Mockito.when(testCasesRepository.getTestCasesById(testCaseId)).thenReturn(null);
        testCasesService.getTestCaseById(testCaseId);
        String testCaseJson = "testCaseJson";
        Mockito.when(testCasesRepository.getTestCasesById(testCaseId)).thenReturn(testCaseJson);
        TestCaseDto testCase = new TestCaseDto();
        Mockito.when(objectMapper.readValue(testCaseJson, TestCaseDto.class)).thenReturn(testCase);
        ResponseWrapper<TestCaseDto> responseWrapper =  testCasesService.getTestCaseById(testCaseId);
        Assert.assertEquals(testCase, responseWrapper.getResponse());
    }

    /*
     * This class tests the getTestCaseById in case of Exception
     */
    @Test
    public void getTestCaseByIdExceptionTest() throws JsonProcessingException {
        String testCaseId = "SBI1000";
        Mockito.when(testCasesRepository.getTestCasesById(testCaseId)).thenReturn(null);
        ReflectionTestUtils.setField(testCasesService, "testCasesRepository", null);
        testCasesService.getTestCaseById(testCaseId);
    }

    /*
     * This class tests the generateRequestForSDKTestcase
     */
    @Test
    @Ignore
    public void generateRequestForSDKTestcaseExceptionTest() throws Exception {
        SdkRequestDto requestDto = new SdkRequestDto();
        requestDto.setMethodName(MethodName.CHECK_QUALITY.getCode());
        String json = "{\n" +
                "\t\"name\": \"John\",\n" +
                "\t\"age\": 30,\n" +
                "\t\"car\": null\n" +
                "}";
        ObjectNode objectNode = (ObjectNode) objectMapper.readValue(json,
                ObjectNode.class);
        ReflectionTestUtils.setField(testCasesService, "objectMapper", objectMapper);
        FileInputStream inputFile = new FileInputStream( "src/test/java/io/mosip/compliance/toolkit/testFile.zip");
        Mockito.when(objectStore.exists(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(objectStore.getObject(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(inputFile);
        testCasesService.generateRequestForSDKTestcase(requestDto);
    }

    /*
     * This class tests the getSdkPurpose
     */
    @Test
    public void getSdkPurposeTest(){
        String methodName = MethodName.MATCH.getCode();
        ReflectionTestUtils.invokeMethod(testCasesService, "getSdkPurpose", methodName);
        methodName = MethodName.SEGMENT.getCode();
        ReflectionTestUtils.invokeMethod(testCasesService, "getSdkPurpose", methodName);
        methodName = MethodName.CHECK_QUALITY.getCode();
        ReflectionTestUtils.invokeMethod(testCasesService, "getSdkPurpose", methodName);
        methodName = MethodName.EXTRACT_TEMPLATE.getCode();
        ReflectionTestUtils.invokeMethod(testCasesService, "getSdkPurpose", methodName);
        methodName = MethodName.CONVERT_FORMAT.getCode();
        ReflectionTestUtils.invokeMethod(testCasesService, "getSdkPurpose", methodName);
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
