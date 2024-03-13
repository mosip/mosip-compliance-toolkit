package io.mosip.compliance.toolkit.controllers;

import java.io.IOException;
import java.util.List;

import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.service.UserProfileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class UserProfileControllerTest {

    @InjectMocks
    private UserProfileController userProfileController;

    @Mock
    private UserProfileService userProfileService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    private static final String BIOMETRIC_CONSENT_POST_ID = "biometric.consent.post";

    /*
     * This class tests the getBiometricsConsentTemplate method
     */
    @Test
    public void getBiometricsConsentTemplateTest() throws Exception {
        ResponseWrapper<String> response = new ResponseWrapper<>();
        when(userProfileService.getConsentTemplate()).thenReturn(response);
        Assert.assertEquals(response, userProfileController.getBiometricsConsentTemplate());
    }

    /*
     * This class tests the addBiometricTestData
     */

    @Test
    public void savePartnerConsentTest() throws Exception {
        ResponseWrapper<PartnerConsentDto> response = new ResponseWrapper<>();
        RequestWrapper<PartnerConsentDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId(BIOMETRIC_CONSENT_POST_ID);
        when(userProfileService.savePartnerConsent(requestWrapper.getRequest())).thenReturn(response);
        Assert.assertEquals(response, userProfileController.savePartnerConsent(requestWrapper, errors));
    }

    /*
     * This class tests the addBiometricTestData
     */

    @Test
    public void isConsentGivenTest() throws Exception {
        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
        when(userProfileService.getPartnerConsent(anyBoolean())).thenReturn(response);
        Assert.assertEquals(response, userProfileController.isConsentGiven(true));
    }
}
