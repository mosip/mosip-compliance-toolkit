package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.SdkSpecVersions;
import io.mosip.compliance.toolkit.dto.AddBioTestDataResponseDto;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntityPK;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.repository.PartnerProfileRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private PartnerProfileRepository partnerProfileRepository;

    @Mock
    private ObjectMapperConfig objectMapperConfig;

    @Mock
    ResourceCacheService resourceCacheService;

    @Mock
    private ObjectMapper mapper;

    private MosipUserDto mosipUserDto;

    /*
     * This class tests the getConsentTemplate method
     */
    @Test
    public void getConsentTemplateTest() throws Exception {
        ResponseWrapper<String> response = new ResponseWrapper<>();
        response = userProfileService.getConsentTemplate();
        Assert.assertNotNull(response.getResponse());
    }

    /*
     * This class tests the savePartnerConsent method
     */
    @Test
    public void savePartnerConsentTest() throws Exception {
        PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
        partnerConsentDto.setConsentForSbiBiometrics("YES");
        partnerConsentDto.setConsentForSdkAbisBiometrics("YES");
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
        partnerProfileEntity.setCrBy("abc");
        partnerProfileEntity.setCrDtimes(LocalDateTime.now());
        Optional<PartnerProfileEntity> optionalEntity = Optional.of(partnerProfileEntity);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerProfileRepository.findById(any(PartnerProfileEntityPK.class))).thenReturn(optionalEntity);
        ResponseWrapper<PartnerConsentDto> response = userProfileService.savePartnerConsent(partnerConsentDto);
        Assert.assertNotNull(response.getResponse());
    }

    @Test
    public void savePartnerConsentException() throws Exception {
        ResponseWrapper<PartnerConsentDto> responseWrapper = new ResponseWrapper<>();
        PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
        partnerConsentDto.setConsentForSbiBiometrics("YES");
        partnerConsentDto.setConsentForSdkAbisBiometrics("YES");
        userProfileService.savePartnerConsent(partnerConsentDto);
    }

    @Test
    public void savePartnerConsentTest1() throws Exception {
        PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
        partnerConsentDto.setConsentForSbiBiometrics("YES");
        partnerConsentDto.setConsentForSdkAbisBiometrics("YES");
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
        partnerProfileEntity.setCrBy("abc");
        partnerProfileEntity.setCrDtimes(LocalDateTime.now());
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerProfileRepository.findById(any(PartnerProfileEntityPK.class))).thenReturn(Optional.empty());
        ResponseWrapper<PartnerConsentDto> response = userProfileService.savePartnerConsent(partnerConsentDto);
        Assert.assertNotNull(response);
    }

    /*
     * This method is used to getPartnerConsent in class
     */
    @Test
    public void getPartnerConsentTest() throws Exception {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerProfileRepository.findById(any(PartnerProfileEntityPK.class))).thenReturn(Optional.empty());
        ResponseWrapper<Boolean> response = userProfileService.getPartnerConsent(false);
        Assert.assertNotNull(response);
    }

    @Test
    public void getPartnerConsentTestException() throws Exception {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        ResponseWrapper<Boolean> response = userProfileService.getPartnerConsent(false);
        Assert.assertNotNull(response);
    }

    @Test
    public void getPartnerConsentSbiTest() throws Exception {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
        partnerProfileEntity.setConsentForSbiBiometrics("YES");
        partnerProfileEntity.setConsentForSbiBiometrics("YES");
        partnerProfileEntity.setCrBy("abc");
        partnerProfileEntity.setCrDtimes(LocalDateTime.now());
        Optional<PartnerProfileEntity> optionalEntity = Optional.of(partnerProfileEntity);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerProfileRepository.findById(any(PartnerProfileEntityPK.class))).thenReturn(optionalEntity);
        ResponseWrapper<Boolean> response = userProfileService.getPartnerConsent(true);
        Assert.assertNotNull(response);
    }

    @Test
    public void getPartnerConsentSdkAbisTest() throws Exception {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerProfileEntity partnerProfileEntity = new PartnerProfileEntity();
        partnerProfileEntity.setConsentForSbiBiometrics("YES");
        partnerProfileEntity.setConsentForSbiBiometrics("YES");
        partnerProfileEntity.setCrBy("abc");
        partnerProfileEntity.setCrDtimes(LocalDateTime.now());
        Optional<PartnerProfileEntity> optionalEntity = Optional.of(partnerProfileEntity);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerProfileRepository.findById(any(PartnerProfileEntityPK.class))).thenReturn(optionalEntity);
        ResponseWrapper<Boolean> response = userProfileService.getPartnerConsent(false);
        Assert.assertNotNull(response);
    }

    /*
     * This method is used to get MosipUserDto in class
     */
    private MosipUserDto getMosipUserDto() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }
}
