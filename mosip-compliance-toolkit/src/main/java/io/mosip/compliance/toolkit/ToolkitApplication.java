package io.mosip.compliance.toolkit;

import io.mosip.kernel.auditmanager.dto.AuthorizedRolesDto;
import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.kernel.virusscanner.*", "io.mosip.compliance.*", "io.mosip.commons.*",
		"io.mosip.kernel.authcodeflowproxy.*","${mosip.auth.adapter.impl.basepackage}"},excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = AuthorizedRolesDto.class) })
@EnableCaching
@Import(value = {HibernateDaoConfig.class})
public class ToolkitApplication {

	public static void main(String[] args) {
		// Launch the application
		SpringApplication.run(ToolkitApplication.class, args);
	}
}
