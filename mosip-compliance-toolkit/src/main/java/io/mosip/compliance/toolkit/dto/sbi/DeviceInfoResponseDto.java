package io.mosip.compliance.toolkit.dto.sbi;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DeviceInfoResponseDto {
	public String deviceInfo;
    public DeviceInfoDto deviceInfoDecoded;
    public ErrorDto error;
}