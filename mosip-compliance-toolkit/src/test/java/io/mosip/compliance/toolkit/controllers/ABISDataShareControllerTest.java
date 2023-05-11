package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.dto.abis.DataShareRequestDto;
import io.mosip.compliance.toolkit.dto.abis.DataShareResponseWrapperDto;
import io.mosip.compliance.toolkit.service.ABISDataShareService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
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
        Mockito.when(abisDataShareService.getDataShareUrl(dataShareRequestDto)).thenReturn(response);
        abisDataShareController.getDataShareUrl(value, errors);
    }
}