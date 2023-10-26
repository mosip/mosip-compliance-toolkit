package io.mosip.compliance.toolkit.controllers;


import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto;
import org.jboss.jandex.Main;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class MainControllerTest {

    @InjectMocks
    private MainController mainController;

    @Mock
    SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Test(expected = NullPointerException.class)
    public void getConfigValuesTestException(){
        mainController.getConfigValues();
    }

    @Test
    public void getConfigValuesTest(){
        MosipUserDto mosipUserDto = getMosipUserDto();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ABIS_PARTNER"));
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        authUserDetails.setAuthorities(authorities);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        mainController.getConfigValues();
    }

    @Test
    public void getConfigValuesTest1(){
        MosipUserDto mosipUserDto = getMosipUserDto();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("abc"));
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        authUserDetails.setAuthorities(authorities);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        mainController.getConfigValues();
    }

    @Test
    public void isAbisPartnerTest() {
        MosipUserDto mosipUserDto = getMosipUserDto();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ABIS_PARTNER"));
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        authUserDetails.setAuthorities(authorities);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        boolean isAbisPartner = ReflectionTestUtils.invokeMethod(mainController, "isAbisPartner");
        assertTrue(isAbisPartner);
    }

    @Test
    public void isAbisPartnerFalseTest() {
        MosipUserDto mosipUserDto = getMosipUserDto();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("abc"));
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "token");
        authUserDetails.setAuthorities(authorities);
        SecurityContextHolder.setContext(securityContext);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(authUserDetails);
        boolean isAbisPartner = ReflectionTestUtils.invokeMethod(mainController, "isAbisPartner");
        assertFalse(isAbisPartner);
    }

    private MosipUserDto getMosipUserDto(){
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("123");
        mosipUserDto.setMail("abc@gmail.com");
        return mosipUserDto;
    }


}
