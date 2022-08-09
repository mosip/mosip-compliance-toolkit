package io.mosip.compliance.toolkit.dto.sbi;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ErrorDto {
	public String errorCode;
    public String errorInfo;
}