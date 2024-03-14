package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntityPK;
import io.mosip.compliance.toolkit.repository.PartnerProfileRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
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
import org.springframework.web.context.WebApplicationContext;


import java.time.LocalDateTime;
import java.util.Optional;

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
    ResourceCacheService resourceCacheService;

    @Mock
    ObjectMapperConfig objectMapperConfig;

    @Mock
    ObjectMapper objectMapper;

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
        when(partnerProfileRepository.save(any(PartnerProfileEntity.class))).thenReturn(partnerProfileEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        Mockito.when(objectMapper.convertValue(eq(PartnerProfileEntity.class), eq(PartnerConsentDto.class))).thenReturn(partnerConsentDto);
        ResponseWrapper<PartnerConsentDto> response = userProfileService.savePartnerConsent(partnerConsentDto);
        Assert.assertNotNull(response);
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
    public void savePartnersConsentTest() throws Exception {
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
        ResponseWrapper<PartnerConsentDto> response = userProfileService.getPartnerConsent();
        Assert.assertNotNull(response);
    }

    @Test
    public void getPartnerConsentTestException() throws Exception {
        ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
        ResponseWrapper<PartnerConsentDto> response = userProfileService.getPartnerConsent();
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
        ResponseWrapper<PartnerConsentDto> response = userProfileService.getPartnerConsent();
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
        ResponseWrapper<PartnerConsentDto> response = userProfileService.getPartnerConsent();
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
