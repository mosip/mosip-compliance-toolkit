package io.mosip.compliance.toolkit.controllers;


import org.jboss.jandex.Main;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
public class MainControllerTest {

    @InjectMocks
    private MainController mainController;

    @Test
    public void getConfigValuesTest(){
        mainController.getConfigValues();
    }
}
