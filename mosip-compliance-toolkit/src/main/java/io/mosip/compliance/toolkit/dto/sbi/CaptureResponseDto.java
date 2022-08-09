package io.mosip.compliance.toolkit.dto.sbi;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CaptureResponseDto {
	public ArrayList<BiometricDto> biometrics;
}
