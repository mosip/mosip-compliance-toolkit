package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class PartnerConsentDto {

    private String partnerId;

    private String orgName;

    private String consentForSdkAbisBiometrics;

    private String consentForSbiBiometrics;

    private String crBy;

    private LocalDateTime crDtimes;

    private String updBy;

    private LocalDateTime updDtimes;

}