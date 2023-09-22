package io.mosip.compliance.toolkit.controllers;

import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.*;
import io.mosip.compliance.toolkit.dto.collections.CollectionDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionRequestDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCaseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionTestCasesResponseDto;
import io.mosip.compliance.toolkit.dto.collections.CollectionsResponseDto;
import io.mosip.compliance.toolkit.service.CollectionsService;
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

import java.util.List;


@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class CollectionsControllerTest {

    @InjectMocks
    private CollectionsController collectionsController;

    @Mock
    private CollectionsService collectionsService;

    @Mock
    private RequestValidator requestValidator;

    @Mock
    private Errors errors;

    final static String id = "123";
    static String projectType;

    /*
     * This class tests the initBinder method
     */
    @Test
    public void initBinderTest(){
        WebDataBinder binder = new WebDataBinder(null);
        collectionsController.initBinder(binder);
    }

    /*
     * This class tests the getProjectCollections method
     */
    @Test
    public void getProjectCollectionsTest(){
        projectType="SBI";
        ResponseWrapper<CollectionsResponseDto> response = new ResponseWrapper<>();
        Mockito.when(collectionsService.getCollections(projectType, id)).thenReturn(response);
        collectionsController.getProjectCollections(projectType, id);
    }

    /*
     * This class tests the getTestcasesForCollection method
     */
    @Test
    public void getTestcasesForCollectionTest(){
        ResponseWrapper<CollectionTestCasesResponseDto> response = new ResponseWrapper<>();
        Mockito.when(collectionsService.getTestCasesForCollection(id)).thenReturn(response);
        collectionsController.getTestCasesForCollection(id);
    }

    /*
     * This class tests the getCollection method
     */
    @Test
    public void getCollectionTest(){
        ResponseWrapper<CollectionDto> response = new ResponseWrapper<>();
        Mockito.when(collectionsService.getCollectionById(id)).thenReturn(response);
        collectionsController.getCollection(id);
    }


    /*
     * This class tests the addCollection method
     */
    @Test
    public void addCollectionTest() throws Exception {
        ResponseWrapper<CollectionDto> response = new ResponseWrapper<>();
        RequestWrapper<CollectionRequestDto> request = new RequestWrapper<>();
        Mockito.when(collectionsService.addCollection(request.getRequest(),AppConstants.BLANK)).thenReturn(response);
        collectionsController.addCollection(request, errors);
    }

    /*
     * This class tests the addTestCasesForCollection method
     */
    @Test
    public void addTestCasesForCollectionTest() throws Exception {
        RequestWrapper<List<CollectionTestCaseDto>> requestWrapper = new RequestWrapper<>();
        ResponseWrapper<List<CollectionTestCaseDto>> response= new ResponseWrapper<>();
        Mockito.when(collectionsService.addTestCasesForCollection(requestWrapper.getRequest())).thenReturn(response);
        collectionsController.addTestCasesForCollection(requestWrapper, errors);
    }

}
