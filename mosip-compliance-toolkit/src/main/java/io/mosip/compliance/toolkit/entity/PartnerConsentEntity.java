package io.mosip.compliance.toolkit.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

@Component
@Entity
@Table(name = "partner_consent", schema = "toolkit")
@Data
@NoArgsConstructor
@ToString
public class PartnerConsentEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "org_name")
    private String orgName;

    @Column(name = "consent_given")
    private String consentGiven;

    @Column(name = "consent_given_dtimes")
    private LocalDateTime consentGivenDtimes;

    @Column(name = "cr_by")
    private String crBy;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDtimes;

    @Column(name = "upd_by")
    private String updBy;

    @Column(name = "upd_dtimes")
    private LocalDateTime updDtimes;

}