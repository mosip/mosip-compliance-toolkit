package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Component
@Entity
@Getter
@Setter
@ToString
@Table(name = "collection_testcase_mapping", schema = "toolkit")
@IdClass(CollectionTestCasePK.class)
public class CollectionTestCaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 480806380514910771L;

	@Id
	@Column(name = "collection_id")
	private String collectionId;
	
	@Id
	@Column(name = "testcase_id")
	private String testcaseId;

	@OneToOne(targetEntity = CollectionEntity.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "collection_id", updatable = false, insertable = false)
	private CollectionEntity collection;

	@OneToOne(targetEntity = TestCaseEntity.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "testcase_id", updatable = false, insertable = false)
	private TestCaseEntity testcase;
}
