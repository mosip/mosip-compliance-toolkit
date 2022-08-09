package io.mosip.compliance.toolkit.dto.sbi;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class DigitalIdDto {

	public String serialNo;
    public String make;
    public String model;
    public String type;
    public String deviceSubType;
    public String deviceProviderId;
    public String deviceProvider;
    public LocalDateTime dateTime;
}