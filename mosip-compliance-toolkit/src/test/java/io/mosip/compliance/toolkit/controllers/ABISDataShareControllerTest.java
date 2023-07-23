package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.abis.DataShareExpireRequest;
import io.mosip.compliance.toolkit.dto.abis.DataShareRequestDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareResponseWrapperDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareSaveTokenRequest;
import io.mosip.compliance.toolkit.service.ABISDataShareService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import static org.mockito.ArgumentMatchers.any;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class ABISDataShareControllerTest {

    @InjectMocks
    private ABISDataShareController abisDataShareController;

    @Mock
    private ABISDataShareService abisDataShareService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    /*
     * This class tests the initBinder method
     */
    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        abisDataShareController.initBinder(binder);
    }

    /*
     * This class tests the getDataShareUrl method
     */
    @Test
    public void getDataShareUrlTest() throws Exception {
        RequestWrapper<DataShareRequestDto> value = new RequestWrapper<>();
        DataShareRequestDto dataShareRequestDto = new DataShareRequestDto();
        value.setRequest(dataShareRequestDto);
        ResponseWrapper<DataShareResponseWrapperDto> response = new ResponseWrapper<>();
        when(abisDataShareService.createDataShareUrl(dataShareRequestDto)).thenReturn(response);
        abisDataShareController.createDataShareUrl(value, errors);
    }

    @Test
    public void expireDataShareUrlTest() {
        RequestWrapper<DataShareExpireRequest> value = new RequestWrapper<>();
        DataShareExpireRequest dataShareExpireRequest = new DataShareExpireRequest();
        value.setRequest(dataShareExpireRequest);
        ResponseWrapper<Boolean> response = new ResponseWrapper<>();
        when(abisDataShareService.expireDataShareUrl(dataShareExpireRequest)).thenReturn(response);
        abisDataShareController.expireDataShareUrl(value, errors);
    }

    @Test
    public void saveDataShareTokenTest() throws Exception {
        RequestWrapper<DataShareSaveTokenRequest> request = new RequestWrapper<>();
        DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
        request.setRequest(dataShareSaveTokenRequest);
        ResponseWrapper<String> response = new ResponseWrapper<>();
        when(abisDataShareService.saveDataShareToken(request)).thenReturn(response);
        abisDataShareController.saveDataShareToken(request, errors);
    }

    @Test
    public void testInvalidateDataShareToken() throws Exception {
        RequestWrapper<DataShareSaveTokenRequest> requestWrapper = new RequestWrapper<>();
        DataShareSaveTokenRequest dataShareSaveTokenRequest = new DataShareSaveTokenRequest();
        requestWrapper.setRequest(dataShareSaveTokenRequest);

        ResponseWrapper<String> expectedResponse = new ResponseWrapper<>();
        expectedResponse.setResponse("success");
        doNothing().when(requestValidator).validateId(any(), any(), any());
        when(abisDataShareService.invalidateDataShareToken(requestWrapper)).thenReturn(expectedResponse);

        ResponseWrapper<String> actualResponse = abisDataShareController.invalidateDataShareToken(requestWrapper, errors);

        assertEquals(expectedResponse, actualResponse);

        verify(abisDataShareService).invalidateDataShareToken(requestWrapper);
    }

}
