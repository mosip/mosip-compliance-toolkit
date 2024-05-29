package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionTestCasePK implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4750230301287075064L;

	private String collectionId;

	private String testcaseId;
}
