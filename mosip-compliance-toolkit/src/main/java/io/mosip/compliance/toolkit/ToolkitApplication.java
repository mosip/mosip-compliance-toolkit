package io.mosip.compliance.toolkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"io.mosip.compliance.*", "io.mosip.commons.*",
		"${mosip.auth.adapter.impl.basepackage}", "io.mosip.kernel.idvalidator.rid.*" })
@EnableCaching
public class ToolkitApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolkitApplication.class, args);
	}

}
