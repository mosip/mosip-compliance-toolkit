package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
