package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.dto.PartnerConsentDto;
import io.mosip.compliance.toolkit.service.ConsentService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.ResponseWrapper;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class ConsentControllerTest {

    @InjectMocks
    private ConsentController consentController;

    @Mock
    private ConsentService consentService;

    @Mock
    private Errors errors;

    @Mock
    RequestValidator requestValidator;

    private static final String BIOMETRIC_CONSENT_POST_ID = "biometric.consent.post";

    /*
     * This method tests the setConsent
     */

    @Test
    public void setConsentTest() {
        ResponseWrapper<PartnerConsentDto> response = new ResponseWrapper<>();
        when(consentService.setConsent()).thenReturn(response);
        Assert.assertEquals(response, consentController.setConsent());
    }

    /*
     * This method tests the isConsentGiven
     */

    @Test
    public void isConsentGivenTest() {
        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
        when(consentService.isConsentGiven(anyString())).thenReturn(response);
        Assert.assertEquals(response, consentController.isConsentGiven("v1"));
    }
}