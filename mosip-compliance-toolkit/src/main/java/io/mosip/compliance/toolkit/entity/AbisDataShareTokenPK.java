package io.mosip.compliance.toolkit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbisDataShareTokenPK implements Serializable {
    private String partnerId;
    private String testCaseId;
    private String testRunId;
}
