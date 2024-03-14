package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static io.mosip.compliance.toolkit.constants.AppConstants.NO;

@Data
@Getter
@Setter
public class PartnerConsentDto {

    private String partnerId;

    private String orgName;

    private String consentForSdkAbisBiometrics = NO;

    private String consentForSbiBiometrics = NO;

    private String crBy;

    private LocalDateTime crDtimes;

    private String updBy;

    private LocalDateTime updDtimes;

}