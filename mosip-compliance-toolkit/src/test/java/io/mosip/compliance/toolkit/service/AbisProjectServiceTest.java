package io.mosip.compliance.toolkit.service;

import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.junit.Assert;
import org.junit.Before;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class AbisProjectServiceTest {

    @InjectMocks
    private AbisProjectService abisProjectService;

    @Mock
    private Authentication authentication;

    @Mock
    SecurityContext securityContext;

    @Mock
    private AbisProjectRepository abisProjectRepository;

    private MosipUserDto mosipUserDto;

    @Before
    public void before() {
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        mosipUserDto = getMosipUserDto();
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    /*
     * This class tests the authUserDetails method
     */
    @Test
    public void authUserDetailsTest() {
        ReflectionTestUtils.invokeMethod(abisProjectService, "authUserDetails");
    }

    /*
     * This class tests the getPartnerId method
     */
    @Test
    public void getPartnerIdTest() {
        String result = ReflectionTestUtils.invokeMethod(abisProjectService, "getPartnerId");
        Assert.assertEquals(mosipUserDto.getUserId(), result);
    }

    /*
     * This class tests the getUserBy method
     */
    @Test
    public void getUserByTest() {
        String result = ReflectionTestUtils.invokeMethod(abisProjectService, "getUserBy");
        Assert.assertEquals(mosipUserDto.getMail(), result);
    }

    /*
     * This class tests the getAbisProject method
     */
    @Test
    public void getAbisProjectTest() {
        String id = "123";
        AbisProjectEntity abisProjectEntity = new AbisProjectEntity();
        Optional<AbisProjectEntity> abisProjectEntityOpt = Optional.of(abisProjectEntity);
        Mockito.when(abisProjectRepository.findById(id, mosipUserDto.getUserId())).thenReturn(abisProjectEntityOpt);

        ResponseWrapper<AbisProjectDto> result = abisProjectService.getAbisProject(id);
        AbisProjectDto abisProjectDto = new AbisProjectDto();
        Assert.assertEquals(abisProjectDto, result.getResponse());
    }

    /*
     * This class tests the getAbisProject method in case of Exception
     */
    @Test
    public void getAbisProjectTestException() {
        ReflectionTestUtils.setField(abisProjectService, "abisProjectRepository", null);
        abisProjectService.getAbisProject("123");
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