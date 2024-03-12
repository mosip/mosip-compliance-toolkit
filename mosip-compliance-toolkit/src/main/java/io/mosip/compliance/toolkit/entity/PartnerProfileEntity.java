package io.mosip.compliance.toolkit.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.*;
import org.springframework.stereotype.Component;

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
