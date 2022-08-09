package io.mosip.compliance.toolkit.dto.sbi;

import java.util.ArrayList;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DeviceInfoDto {
	public ArrayList<String> specVersion;
    public String env;
    public String digitalId;
    public DigitalIdDto digitalIdDecoded;
    public String deviceId;
    public String deviceCode;
    public String purpose;
    public String serviceVersion;
    public String deviceStatus;
    public String firmware;
    public String certification;
    public ArrayList<Integer> deviceSubId;
    public String callbackId;
}