package io.mosip.compliance.toolkit.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.NamedNativeQuery;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class CollectionTestcaseEntity {

	@Id
	@Column(name = "collection_id")
	private String collectionId;

	@Column(name = "testcase_id")
	private String testcaseId;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cid_fk", referencedColumnName = "collectionId")
	private CollectionEntity collection;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tid_fk", referencedColumnName = "testcaseId")
	private TestCaseEntity testcase;
}
