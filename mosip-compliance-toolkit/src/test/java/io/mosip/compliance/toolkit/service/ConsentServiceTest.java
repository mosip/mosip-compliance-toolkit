package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.entity.PartnerConsentEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.compliance.toolkit.repository.PartnerConsentRepository;
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
public class ConsentServiceTest {

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private PartnerConsentRepository partnerConsentRepository;

    @Mock
    private MasterTemplatesRepository masterTemplatesRepository;

    @Mock
    ResourceCacheService resourceCacheService;

    @Mock
    ObjectMapperConfig objectMapperConfig;

    @Mock
    ObjectMapper objectMapper;

    private MosipUserDto mosipUserDto;


    /*
     * This class tests the setConsent method
     */
    @Test
    public void setConsentTest() {
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerConsentEntity partnerConsentEntity = new PartnerConsentEntity();
        partnerConsentEntity.setId("123");
        partnerConsentEntity.setCrBy("abc");
        partnerConsentEntity.setCrDtimes(LocalDateTime.now());
        Optional<PartnerConsentEntity> optionalEntity = Optional.of(partnerConsentEntity);
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerConsentRepository.findByPartnerId(anyString())).thenReturn(optionalEntity);
        when(partnerConsentRepository.save(any(PartnerConsentEntity.class))).thenReturn(partnerConsentEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
        Mockito.when(objectMapper.convertValue(eq(PartnerConsentEntity.class), eq(PartnerConsentDto.class))).thenReturn(partnerConsentDto);
        ResponseWrapper<PartnerConsentDto> response = consentService.setConsent();
        Assert.assertNotNull(response);
    }

    @Test
    public void setConsentTest1() {
        Mockito.when(resourceCacheService.getOrgName(anyString())).thenReturn("abc");
        PartnerConsentEntity partnerConsentEntity = new PartnerConsentEntity();
        partnerConsentEntity.setId("123");
        partnerConsentEntity.setCrBy("abc");
        partnerConsentEntity.setCrDtimes(LocalDateTime.now());
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(partnerConsentRepository.findByPartnerId(anyString())).thenReturn(Optional.empty());
        when(partnerConsentRepository.save(any(PartnerConsentEntity.class))).thenReturn(partnerConsentEntity);
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        PartnerConsentDto partnerConsentDto = new PartnerConsentDto();
        Mockito.when(objectMapper.convertValue(eq(PartnerConsentEntity.class), eq(PartnerConsentDto.class))).thenReturn(partnerConsentDto);
        ResponseWrapper<PartnerConsentDto> response = consentService.setConsent();
        Assert.assertNotNull(response);
    }

    @Test
    public void setConsentException(){
        consentService.setConsent();
    }

    /*
     * This method is used to isConsentGiven in class
     */
    @Test
    public void isConsentGivenTest(){
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(masterTemplatesRepository.getLatestTemplateTimestamp(anyString(),anyString())).thenReturn(LocalDateTime.now());
        PartnerConsentEntity partnerConsentEntity = new PartnerConsentEntity();
        partnerConsentEntity.setConsentGivenDtimes(LocalDateTime.now());
        Optional<PartnerConsentEntity> optionalEntity = Optional.of(partnerConsentEntity);
        when(partnerConsentRepository.findByPartnerId(anyString())).thenReturn(optionalEntity);

        ResponseWrapper<Boolean> response = consentService.isConsentGiven("v1");
        Assert.assertNotNull(response);
    }

    @Test
    public void isConsentGivenTestException() {
        ResponseWrapper<Boolean> response = consentService.isConsentGiven("v1");
        Assert.assertNotNull(response);
    }

    @Test
    public void isConsentGivenTemplateExceptionTest(){
        MosipUserDto mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "123");
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(masterTemplatesRepository.getLatestTemplateTimestamp(anyString(),anyString())).thenReturn(null);
        PartnerConsentEntity partnerConsentEntity = new PartnerConsentEntity();
        partnerConsentEntity.setConsentGivenDtimes(LocalDateTime.now());
        Optional<PartnerConsentEntity> optionalEntity = Optional.of(partnerConsentEntity);
        when(partnerConsentRepository.findByPartnerId(anyString())).thenReturn(optionalEntity);

        ResponseWrapper<Boolean> response = consentService.isConsentGiven("v1");
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