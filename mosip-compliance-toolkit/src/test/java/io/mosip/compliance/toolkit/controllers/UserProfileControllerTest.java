package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.service.UserProfileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;

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
    private Errors errors;

    @Mock
    RequestValidator requestValidator;

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
     * This class tests the savePartnerConsent
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
     * This class tests the getPartnerConsent
     */

    @Test
    public void getPartnerConsentTest() throws Exception {
        ResponseWrapper<PartnerConsentDto> response = new ResponseWrapper<>();
        when(userProfileService.getPartnerConsent()).thenReturn(response);
        Assert.assertEquals(response, userProfileController.getPartnerConsent());
    }
}
