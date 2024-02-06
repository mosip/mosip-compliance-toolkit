package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.entity.BiometricScoresSummaryEntity;
import io.mosip.compliance.toolkit.repository.BiometricScoresRepository;
import io.mosip.compliance.toolkit.repository.BiometricScoresSummaryRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import org.codehaus.jackson.node.ArrayNode;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class BiometricScoresServiceTest {

    @InjectMocks
    private BiometricScoresService biometricScoresService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    ResourceCacheService resourceCacheService;

    @Mock
    ObjectMapperConfig objectMapperConfig;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    BiometricScoresSummaryRepository biometricScoresSummaryRepository;

    private MosipUserDto mosipUserDto;

    @Before
    public void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        ReflectionTestUtils.invokeMethod(biometricScoresService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        String result = ReflectionTestUtils.invokeMethod(biometricScoresService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
        String result = ReflectionTestUtils.invokeMethod(biometricScoresService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    @Test
    public void addBiometricScoresTest() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(resourceCacheService.getOrgName("abc")).thenReturn("abc");
        SecurityContextHolder.setContext(securityContext);
        biometricScoresService.addBiometricScores("123", "abc", "100", "score");
    }

    @Test
    public void addBiometricScoresTest1() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(resourceCacheService.getOrgName("abc")).thenReturn("abc");
        SecurityContextHolder.setContext(securityContext);
        biometricScoresService.addBiometricScores(null, "abc", "100", "score");
    }

    private MosipUserDto getMosipUserDto() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }

    @Test
    public void getFingerBiometricScoresListTest() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        String expectedSdkUrlsJsonStr = "[{\"name\": \"BQAT SDK\"}]";
        setFieldValue(biometricScoresService, "fingerSdkUrlsJsonStr", expectedSdkUrlsJsonStr);
        List<String> ageGroups = Arrays.asList("child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)");
        List<String> occupations = Arrays.asList("labourer", "non-labourer");
        setFieldValue(biometricScoresService, "ageGroups", ageGroups);
        setFieldValue(biometricScoresService, "occupations", occupations);
        List<BiometricScoresSummaryEntity> biometricScoresSummaryEntityList = new ArrayList<>();
        BiometricScoresSummaryEntity childBiometricScoresSummaryEntity = new BiometricScoresSummaryEntity();
        childBiometricScoresSummaryEntity.setId("123");
        childBiometricScoresSummaryEntity.setMale_0_10(5);
        childBiometricScoresSummaryEntity.setMale_11_20(10);
        childBiometricScoresSummaryEntity.setMale_21_30(15);
        childBiometricScoresSummaryEntity.setMale_31_40(20);
        childBiometricScoresSummaryEntity.setMale_41_50(25);
        childBiometricScoresSummaryEntity.setMale_51_60(30);
        childBiometricScoresSummaryEntity.setMale_61_70(35);
        childBiometricScoresSummaryEntity.setMale_71_80(40);
        childBiometricScoresSummaryEntity.setMale_81_90(45);
        childBiometricScoresSummaryEntity.setMale_91_100(50);
        childBiometricScoresSummaryEntity.setFemale_0_10(5);
        childBiometricScoresSummaryEntity.setFemale_11_20(10);
        childBiometricScoresSummaryEntity.setFemale_21_30(15);
        childBiometricScoresSummaryEntity.setFemale_31_40(20);
        childBiometricScoresSummaryEntity.setFemale_41_50(25);
        childBiometricScoresSummaryEntity.setFemale_51_60(30);
        childBiometricScoresSummaryEntity.setFemale_61_70(35);
        childBiometricScoresSummaryEntity.setFemale_71_80(40);
        childBiometricScoresSummaryEntity.setFemale_81_90(45);
        childBiometricScoresSummaryEntity.setFemale_91_100(50);

        BiometricScoresSummaryEntity biometricScoresSummaryEntity = new BiometricScoresSummaryEntity();

        biometricScoresSummaryEntity.setId("456");
        biometricScoresSummaryEntity.setMale_0_10(5);
        biometricScoresSummaryEntity.setMale_11_20(10);
        biometricScoresSummaryEntity.setMale_21_30(15);
        biometricScoresSummaryEntity.setMale_31_40(20);
        biometricScoresSummaryEntity.setMale_41_50(25);
        biometricScoresSummaryEntity.setMale_51_60(30);
        biometricScoresSummaryEntity.setMale_61_70(35);
        biometricScoresSummaryEntity.setMale_71_80(40);
        biometricScoresSummaryEntity.setMale_81_90(45);
        biometricScoresSummaryEntity.setMale_91_100(50);
        biometricScoresSummaryEntity.setFemale_0_10(5);
        biometricScoresSummaryEntity.setFemale_11_20(10);
        biometricScoresSummaryEntity.setFemale_21_30(15);
        biometricScoresSummaryEntity.setFemale_31_40(20);
        biometricScoresSummaryEntity.setFemale_41_50(25);
        biometricScoresSummaryEntity.setFemale_51_60(30);
        biometricScoresSummaryEntity.setFemale_61_70(35);
        biometricScoresSummaryEntity.setFemale_71_80(40);
        biometricScoresSummaryEntity.setFemale_81_90(45);
        biometricScoresSummaryEntity.setFemale_91_100(50);

        biometricScoresSummaryEntityList.add(childBiometricScoresSummaryEntity);
        biometricScoresSummaryEntityList.add(biometricScoresSummaryEntity);
        when(biometricScoresSummaryRepository.getBiometricScoresForFinger(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(biometricScoresSummaryEntityList);

        biometricScoresService.getFingerBiometricScoresList("123", "abc", "100");
    }

    @Test
    public void getFaceBiometricScoresListTest() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        String expectedSdkUrlsJsonStr = "[{\"name\": \"BQAT SDK\"}]";
        setFieldValue(biometricScoresService, "faceSdkUrlsJsonStr", expectedSdkUrlsJsonStr);
        List<String> ageGroups = Arrays.asList("child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)");
        List<String> races = Arrays.asList("asian", "african");
        setFieldValue(biometricScoresService, "ageGroups", ageGroups);
        setFieldValue(biometricScoresService, "races", races);
        List<BiometricScoresSummaryEntity> biometricScoresSummaryEntityList = new ArrayList<>();
        BiometricScoresSummaryEntity biometricScoresSummaryEntity1 = new BiometricScoresSummaryEntity();
        biometricScoresSummaryEntity1.setId("123");
        biometricScoresSummaryEntity1.setMale_0_10(5);
        biometricScoresSummaryEntity1.setMale_11_20(10);
        biometricScoresSummaryEntity1.setMale_21_30(15);
        biometricScoresSummaryEntity1.setMale_31_40(20);
        biometricScoresSummaryEntity1.setMale_41_50(25);
        biometricScoresSummaryEntity1.setMale_51_60(30);
        biometricScoresSummaryEntity1.setMale_61_70(35);
        biometricScoresSummaryEntity1.setMale_71_80(40);
        biometricScoresSummaryEntity1.setMale_81_90(45);
        biometricScoresSummaryEntity1.setMale_91_100(50);
        biometricScoresSummaryEntity1.setFemale_0_10(5);
        biometricScoresSummaryEntity1.setFemale_11_20(10);
        biometricScoresSummaryEntity1.setFemale_21_30(15);
        biometricScoresSummaryEntity1.setFemale_31_40(20);
        biometricScoresSummaryEntity1.setFemale_41_50(25);
        biometricScoresSummaryEntity1.setFemale_51_60(30);
        biometricScoresSummaryEntity1.setFemale_61_70(35);
        biometricScoresSummaryEntity1.setFemale_71_80(40);
        biometricScoresSummaryEntity1.setFemale_81_90(45);
        biometricScoresSummaryEntity1.setFemale_91_100(50);

        BiometricScoresSummaryEntity biometricScoresSummaryEntity = new BiometricScoresSummaryEntity();

        biometricScoresSummaryEntity.setId("456");
        biometricScoresSummaryEntity.setMale_0_10(5);
        biometricScoresSummaryEntity.setMale_11_20(10);
        biometricScoresSummaryEntity.setMale_21_30(15);
        biometricScoresSummaryEntity.setMale_31_40(20);
        biometricScoresSummaryEntity.setMale_41_50(25);
        biometricScoresSummaryEntity.setMale_51_60(30);
        biometricScoresSummaryEntity.setMale_61_70(35);
        biometricScoresSummaryEntity.setMale_71_80(40);
        biometricScoresSummaryEntity.setMale_81_90(45);
        biometricScoresSummaryEntity.setMale_91_100(50);
        biometricScoresSummaryEntity.setFemale_0_10(5);
        biometricScoresSummaryEntity.setFemale_11_20(10);
        biometricScoresSummaryEntity.setFemale_21_30(15);
        biometricScoresSummaryEntity.setFemale_31_40(20);
        biometricScoresSummaryEntity.setFemale_41_50(25);
        biometricScoresSummaryEntity.setFemale_51_60(30);
        biometricScoresSummaryEntity.setFemale_61_70(35);
        biometricScoresSummaryEntity.setFemale_71_80(40);
        biometricScoresSummaryEntity.setFemale_81_90(45);
        biometricScoresSummaryEntity.setFemale_91_100(50);

        biometricScoresSummaryEntityList.add(biometricScoresSummaryEntity1);
        biometricScoresSummaryEntityList.add(biometricScoresSummaryEntity);
        when(biometricScoresSummaryRepository.getBiometricScoresForFace(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(biometricScoresSummaryEntityList);

        biometricScoresService.getFaceBiometricScoresList("123", "abc", "100");
    }

    @Test(expected = Exception.class)
    public void getFingerBiometricScoresListTestException() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        biometricScoresService.getFingerBiometricScoresList("123", "abc", "100");
    }

    @Test(expected = Exception.class)
    public void getFaceBiometricScoresListTestException() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        biometricScoresService.getFaceBiometricScoresList("123", "abc", "100");
    }

    @Test(expected = Exception.class)
    public void getIrisBiometricScoresListTestException() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        biometricScoresService.getIrisBiometricScoresList("123", "abc", "100");
    }

    @Test(expected = Exception.class)
    public void getSdkUrlsJsonStringTest() throws Exception {
        ReflectionTestUtils.invokeMethod(biometricScoresService, "getSdkUrlsJsonString", "123");
    }

    @Test
    public void getIrisBiometricScoresListTest() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        when(objectMapperConfig.objectMapper()).thenReturn(objectMapper1);
        String expectedSdkUrlsJsonStr = "[{\"name\": \"BQAT SDK\"}]";
        setFieldValue(biometricScoresService, "irisSdkUrlsJsonStr", expectedSdkUrlsJsonStr);
        List<String> ageGroups = Arrays.asList("child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)");
        setFieldValue(biometricScoresService, "ageGroups", ageGroups);
        List<BiometricScoresSummaryEntity> biometricScoresSummaryEntityList = new ArrayList<>();
        BiometricScoresSummaryEntity biometricScoresSummaryEntity1 = new BiometricScoresSummaryEntity();
        biometricScoresSummaryEntity1.setId("123");
        biometricScoresSummaryEntity1.setMale_0_10(5);
        biometricScoresSummaryEntity1.setMale_11_20(10);
        biometricScoresSummaryEntity1.setMale_21_30(15);
        biometricScoresSummaryEntity1.setMale_31_40(20);
        biometricScoresSummaryEntity1.setMale_41_50(25);
        biometricScoresSummaryEntity1.setMale_51_60(30);
        biometricScoresSummaryEntity1.setMale_61_70(35);
        biometricScoresSummaryEntity1.setMale_71_80(40);
        biometricScoresSummaryEntity1.setMale_81_90(45);
        biometricScoresSummaryEntity1.setMale_91_100(50);
        biometricScoresSummaryEntity1.setFemale_0_10(5);
        biometricScoresSummaryEntity1.setFemale_11_20(10);
        biometricScoresSummaryEntity1.setFemale_21_30(15);
        biometricScoresSummaryEntity1.setFemale_31_40(20);
        biometricScoresSummaryEntity1.setFemale_41_50(25);
        biometricScoresSummaryEntity1.setFemale_51_60(30);
        biometricScoresSummaryEntity1.setFemale_61_70(35);
        biometricScoresSummaryEntity1.setFemale_71_80(40);
        biometricScoresSummaryEntity1.setFemale_81_90(45);
        biometricScoresSummaryEntity1.setFemale_91_100(50);

        BiometricScoresSummaryEntity biometricScoresSummaryEntity = new BiometricScoresSummaryEntity();

        biometricScoresSummaryEntity.setId("456");
        biometricScoresSummaryEntity.setMale_0_10(5);
        biometricScoresSummaryEntity.setMale_11_20(10);
        biometricScoresSummaryEntity.setMale_21_30(15);
        biometricScoresSummaryEntity.setMale_31_40(20);
        biometricScoresSummaryEntity.setMale_41_50(25);
        biometricScoresSummaryEntity.setMale_51_60(30);
        biometricScoresSummaryEntity.setMale_61_70(35);
        biometricScoresSummaryEntity.setMale_71_80(40);
        biometricScoresSummaryEntity.setMale_81_90(45);
        biometricScoresSummaryEntity.setMale_91_100(50);
        biometricScoresSummaryEntity.setFemale_0_10(5);
        biometricScoresSummaryEntity.setFemale_11_20(10);
        biometricScoresSummaryEntity.setFemale_21_30(15);
        biometricScoresSummaryEntity.setFemale_31_40(20);
        biometricScoresSummaryEntity.setFemale_41_50(25);
        biometricScoresSummaryEntity.setFemale_51_60(30);
        biometricScoresSummaryEntity.setFemale_61_70(35);
        biometricScoresSummaryEntity.setFemale_71_80(40);
        biometricScoresSummaryEntity.setFemale_81_90(45);
        biometricScoresSummaryEntity.setFemale_91_100(50);

        biometricScoresSummaryEntityList.add(biometricScoresSummaryEntity1);
        biometricScoresSummaryEntityList.add(biometricScoresSummaryEntity);
        when(biometricScoresSummaryRepository.getBiometricScoresForIris(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(biometricScoresSummaryEntityList);

        biometricScoresService.getIrisBiometricScoresList("123", "abc", "100");
    }


    private void setFieldValue(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
