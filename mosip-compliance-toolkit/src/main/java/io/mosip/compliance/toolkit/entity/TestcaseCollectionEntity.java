package io.mosip.compliance.toolkit.entity;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.NamedNativeQuery;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(name = "Mapping.TestcaseCollectionEntity", classes = {
		@ConstructorResult(targetClass = TestcaseCollectionEntity.class, columns = {
				@ColumnResult(name = "collectionid", type = String.class),
				@ColumnResult(name = "testcaseid", type = String.class),
				@ColumnResult(name = "testcasejson", type = String.class),
				@ColumnResult(name = "testcasetype", type = String.class),
				@ColumnResult(name = "specversion", type = String.class) }) })
@NamedNativeQuery(name = "TestcaseCollectionEntity.getTestcasesByCollectionId", resultClass = TestcaseCollectionEntity.class, query = "SELECT ctm.collection_id AS collectionid, ctm.testcase_id AS testcaseid, t.testcase_json AS testcasejson, t.testcase_type AS testcasetype, t.spec_version AS specversion FROM toolkit.collection_testcase_mapping AS ctm INNER JOIN toolkit.collections AS c ON (ctm.collection_id = c.id) INNER JOIN toolkit.testcase AS t ON (ctm.testcase_id = t.id) WHERE ctm.collection_id =:collectionId AND c.partner_id =:partnerId AND c.is_deleted<>'true'", resultSetMapping = "Mapping.TestcaseCollectionEntity")
public class TestcaseCollectionEntity {

	public TestcaseCollectionEntity(String collectionId, String testcaseId, String testcaseJson, String testcaseType,
			String specVersion) {
		super();
		this.collectionId = collectionId;
		this.testCaseId = testcaseId;
		this.testcaseJson = testcaseJson;
		this.testcaseType = testcaseType;
		this.specVersion = specVersion;
	}

	@Id
	private String collectionId;

	private String testCaseId;

	private String testcaseJson;

	private String testcaseType;

	private String specVersion;
}
