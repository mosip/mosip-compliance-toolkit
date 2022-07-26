package io.mosip.compliance.toolkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.mosip.compliance.*", "io.mosip.commons.*",
		"${mosip.auth.adapter.impl.basepackage}", "io.mosip.kernel.idvalidator.rid.*" })
public class ToolkitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolkitApplication.class, args);
	}

}
