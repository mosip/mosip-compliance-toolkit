package io.mosip.compliance.toolkit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.compliance.toolkit.dto.MasterTemplatesDto;
import io.mosip.compliance.toolkit.entity.MasterTemplatesEntity;
import io.mosip.compliance.toolkit.repository.MasterTemplatesRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import java.sql.SQLException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class TemplateServiceTest {

    @InjectMocks
    private TemplateService templateService;

    @Mock
    private MasterTemplatesRepository masterTemplatesRepository;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ObjectMapperConfig objectMapperConfig;

    /*
     * This class tests the getTemplate method
     */
    @Test
    public void getTemplateTest() {
        MasterTemplatesEntity masterTemplatesEntity = new MasterTemplatesEntity();
        masterTemplatesEntity.setId("123");
        Optional<MasterTemplatesEntity> optionalEntity = Optional.of(masterTemplatesEntity);
        when(masterTemplatesRepository.getTemplate(anyString(), anyString(), anyString())).thenReturn(optionalEntity);
        MasterTemplatesDto masterTemplatesDto = new MasterTemplatesDto();
        Mockito.when(objectMapperConfig.objectMapper()).thenReturn(objectMapper);
        Mockito.when(objectMapper.convertValue(eq(MasterTemplatesEntity.class), eq(MasterTemplatesDto.class))).thenReturn(masterTemplatesDto);
        ResponseWrapper<MasterTemplatesDto> response = templateService.getTemplate("eng", "abc", "v1");
        Assert.assertNotNull(response);
    }

    @Test
    public void getTemplateExceptionTest() {
        when(masterTemplatesRepository.getTemplate(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        ResponseWrapper<MasterTemplatesDto> response = templateService.getTemplate("eng", "abc", "v1");
        Assert.assertNotNull(response);
    }

    @Test
    public void getTemplateTestInvalidParamException() {
        ResponseWrapper<MasterTemplatesDto> response = templateService.getTemplate(null, "abc", "v1");
        Assert.assertNotNull(response);
    }

    @Test
    public void getTemplateExceptionTest1() {
        MasterTemplatesEntity masterTemplatesEntity = new MasterTemplatesEntity();
        Optional<MasterTemplatesEntity> optionalEntity = Optional.of(masterTemplatesEntity);
        when(masterTemplatesRepository.getTemplate(anyString(), anyString(), anyString())).thenReturn(optionalEntity);
        ResponseWrapper<MasterTemplatesDto> response = templateService.getTemplate("eng", "abc", "v1");
        Assert.assertNotNull(response);
    }

    /*
     * This class tests the getLatestTemplateVersion method
     */
    @Test
    public void getLatestTemplateVersionTest() {
        when(masterTemplatesRepository.getLatestTemplateVersion(anyString())).thenReturn("v12");
        ResponseWrapper<String> response = templateService.getLatestTemplateVersion("abc");
        Assert.assertNotNull(response);
    }

    @Test
    public void getLatestTemplateVersionTestException() {
        when(masterTemplatesRepository.getLatestTemplateVersion(anyString())).thenReturn("vsdfu");
        ResponseWrapper<String> response = templateService.getLatestTemplateVersion("abc");
        Assert.assertNotNull(response);
    }

    @Test
    public void getLatestTemplateVersionTestException1() {
        when(masterTemplatesRepository.getLatestTemplateVersion(anyString())).thenReturn(null);
        ResponseWrapper<String> response = templateService.getLatestTemplateVersion("abc");
        Assert.assertNotNull(response);
    }

    @Test
    public void getLatestTemplateVersionTestException2() {
        when(masterTemplatesRepository.getLatestTemplateVersion(anyString())).thenThrow(NullPointerException.class);
        ResponseWrapper<String> response = templateService.getLatestTemplateVersion("abc");
        Assert.assertNotNull(response);
    }
}
