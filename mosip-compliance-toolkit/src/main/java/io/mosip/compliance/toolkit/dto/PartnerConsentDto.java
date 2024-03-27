package io.mosip.compliance.toolkit.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class PartnerConsentDto {

    private String partnerId;

    private String orgName;

    private String consentGiven;

    private LocalDateTime consentGivenDtimes;

    private String crBy;

    private LocalDateTime crDtimes;

    private String updBy;

    private LocalDateTime updDtimes;

}