package io.mosip.compliance.toolkit.entity;

import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity
@Table(name = "datashare_tokens", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
@IdClass(AbisDataShareTokenPK.class)
public class AbisDataShareTokenEntity {
    @Id
    @Column(name = "partner_id")
    private String partnerId;
    @Id
    @Column(name = "testcase_id")
    private String testCaseId;
    @Id
    @Column(name = "testrun_id")
    private String testRunId;
    @Column(name = "token")
    private String token;
    @Column(name = "result")
    private String result = "";

    public AbisDataShareTokenEntity(String partnerId, String ctkTestCaseId, String ctkTestRunId, String token) {
    }
}
