package io.mosip.compliance.toolkit.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Getter
@Setter
public class ValidatorDefDto  implements Serializable {
	public String name;
	public String description;
}
