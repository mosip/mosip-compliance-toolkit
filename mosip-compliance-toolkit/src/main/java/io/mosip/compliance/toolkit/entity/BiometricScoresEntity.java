package io.mosip.compliance.toolkit.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * This entity class defines the database table biometric_scores.
 */

@Component
@Entity
@Table(name = "biometric_scores", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BiometricScoresEntity {

    @Id
    private String id;

    @Column(name = "project_id")
    private String projectId;

    @Column(name = "partner_id")
    private String partnerId;

    @Column(name = "scores_json")
    private String scoresJson;

    @Column(name = "cr_dtimes")
    private LocalDateTime crDate;

    @Column(name = "cr_by")
    private String crBy;

    @Column(name = "testrun_id")
    private String testRunId;

    @Column(name = "testcase_id")
    private String testCaseId;

}