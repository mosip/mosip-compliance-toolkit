package io.mosip.compliance.toolkit.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.compliance.toolkit.dto.report.ComplianceTestRunSummaryDto;
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
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.WebApplicationContext;

import io.mosip.compliance.toolkit.dto.report.ReportRequestDto;
import io.mosip.compliance.toolkit.service.ReportService;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;

import jakarta.validation.constraints.AssertTrue;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class ReportControllerTest {

    @Mock
    private ReportService reportGeneratorService;

    @InjectMocks
    private ReportController reportGeneratorController;

    @Mock
    RequestValidator requestValidator;

    @Mock
    Errors errors;

    @Test
    public void initBinderTest() {
        WebDataBinder binder = new WebDataBinder(null);
        reportGeneratorController.initBinder(binder);
    }

    @Test
    public void generateDraftReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> value = new RequestWrapper<>();
        Errors errors = mock(Errors.class);
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setProjectId("123");
        reportRequestDto.setTestRunId("123");
        reportRequestDto.setCollectionId("123");
        value.setRequest(reportRequestDto);
        value.setId("abc");
        value.setRequesttime(LocalDateTime.now());
        value.setVersion("1.0");
        String origin = "abc";
        reportGeneratorController.generateDraftReport(value, origin, errors);
    }

    @Test
    public void generateDraftQAReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> value = new RequestWrapper<>();
        Errors errors = mock(Errors.class);
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestDto.setProjectType("SBI");
        reportRequestDto.setProjectId("abc123");
        reportRequestDto.setTestRunId("12345");
        reportRequestDto.setCollectionId("1acd23");
        value.setRequest(reportRequestDto);
        value.setId("abc");
        value.setRequesttime(LocalDateTime.now());
        value.setVersion("1.0");
        String origin = "abc";
        reportGeneratorController.generateDraftQAReport(value, origin, errors);
    }

    @Test
    public void isReportAlreadySubmittedTest() throws Exception {
        RequestWrapper<ReportRequestDto> reportRequestWrapper = new RequestWrapper<>();
        ResponseWrapper<Boolean> result = new ResponseWrapper<>();
        Mockito.when(reportGeneratorController.isReportAlreadySubmitted(reportRequestWrapper, errors)).thenReturn(result);
        reportGeneratorController.isReportAlreadySubmitted(reportRequestWrapper, errors);
    }

    @Test
    public void submitReportForReviewTest() throws Exception {
        RequestWrapper<ReportRequestDto> reportRequestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestWrapper.setId("partner.report.post");
        reportRequestWrapper.setRequest(reportRequestDto);
        reportGeneratorController.submitReportForReview(reportRequestWrapper, errors);
    }

    @Test
    public void getSubmittedReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> reportRequestWrapper = new RequestWrapper<>();
        ReportRequestDto reportRequestDto = new ReportRequestDto();
        reportRequestWrapper.setId("partner.report.post");
        reportRequestWrapper.setRequest(reportRequestDto);
        reportGeneratorController.getSubmittedReport(reportRequestWrapper, errors);
    }

    @Test
    public void getSubmittedReportListTest() throws Exception {
        reportGeneratorController.getSubmittedReportList();
        verify(reportGeneratorService).getReportList(eq(false), eq(null));
    }

    @Test
    public void getPartnerReportListTest() throws Exception {
        String reportStatus = "review";
        reportGeneratorController.getPartnerReportList(reportStatus);
        verify(reportGeneratorService).getReportList(eq(true), eq(reportStatus));
    }

    @Test
    public void getPartnerReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        String partnerId = "abc";
        reportGeneratorController.getPartnerReport(partnerId, requestWrapper, errors);
        verify(reportGeneratorService).getSubmittedReport(eq(partnerId), eq(requestWrapper.getRequest()), eq(false));
    }

    @Test
    public void approvePartnerReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId("partner.report.post");
        String partnerId = "abc";
        reportGeneratorController.approvePartnerReport(partnerId, requestWrapper, errors);
    }

    @Test
    public void rejectPartnerReportTest() throws Exception {
        RequestWrapper<ReportRequestDto> requestWrapper = new RequestWrapper<>();
        requestWrapper.setId("partner.report.post");
        String partnerId = "abc";
        reportGeneratorController.rejectPartnerReport(partnerId, requestWrapper, errors);
    }
}
