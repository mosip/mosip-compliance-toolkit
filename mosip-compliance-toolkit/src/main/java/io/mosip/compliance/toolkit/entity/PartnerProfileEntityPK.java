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
public class PartnerProfileEntityPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String partnerId;

    private String orgName;

}