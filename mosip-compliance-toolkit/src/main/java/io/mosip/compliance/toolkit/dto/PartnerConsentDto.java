package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PartnerConsentDto {

    private String consentForSdkAbisBiometrics;

    private String consentForSbiBiometrics;

}
