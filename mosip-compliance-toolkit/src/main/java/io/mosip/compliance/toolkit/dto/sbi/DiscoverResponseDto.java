package io.mosip.compliance.toolkit.dto.sbi;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DiscoverResponseDto {
	public String deviceId;
    public String deviceStatus;
    public String certification;
    public String serviceVersion;
    public String callbackId;
    public String digitalId;
    public DigitalIdDto digitalIdDecoded;
    public String deviceCode;
    public String purpose;
    public ErrorDto error;
    public ArrayList<String> specVersion;
    public ArrayList<String> deviceSubId;
}