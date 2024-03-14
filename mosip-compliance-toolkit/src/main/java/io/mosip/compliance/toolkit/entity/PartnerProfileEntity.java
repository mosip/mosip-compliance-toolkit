package io.mosip.compliance.toolkit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "partner_profile", schema = "toolkit")
@Data
@NoArgsConstructor
@ToString
@IdClass(PartnerProfileEntityPK.class)
public class PartnerProfileEntity {

    @Id
    @Column(name = "partner_id")
    private String partnerId;

    @Id
    @Column(name = "org_name")
    private String orgName;

    @Column(name = "consent_for_sdk_abis_biometrics")
    private String consentForSdkAbisBiometrics;

    @Column(name = "consent_for_sbi_biometrics")
    private String consentForSbiBiometrics;

    @Column(name = "cr_by")
    private String crBy;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "upd_by")
    private String updBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updDtimes;

}