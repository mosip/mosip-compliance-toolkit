package io.mosip.compliance.toolkit.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.roles.compliance.toolkit")
@Getter
@Setter
public class AuthorizedRolesDto {

	private List<String> uploadResource;

}
