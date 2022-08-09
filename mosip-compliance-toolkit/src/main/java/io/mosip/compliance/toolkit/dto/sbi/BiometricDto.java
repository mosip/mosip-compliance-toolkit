package io.mosip.compliance.toolkit.dto.sbi;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BiometricDto {
	public String specVersion;
    public String data;
    public DataDto dataDecoded;
    public String sessionKey;
    public String thumbprint;
    public String hash;
    public ErrorDto error;
    
    @Data
    public static class DataDto
    {
    	public String digitalId;
        public DigitalIdDto digitalIdDecoded;
        public String deviceCode;
        public String deviceServiceVersion;
        public String bioType;
        public String bioSubType;
        public String purpose;
        public String env;
        public String domainUri;
        public String bioValue;
        public String transactionId;
        public LocalDateTime timestamp;
        public String requestedScore;
        public String qualityScore;
    }

}