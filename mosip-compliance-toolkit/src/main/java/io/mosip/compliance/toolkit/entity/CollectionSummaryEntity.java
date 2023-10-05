package io.mosip.compliance.toolkit.entity;

import java.time.LocalDateTime;

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
@SqlResultSetMapping(name = "Mapping.CollectionSummaryEntity", classes = {
		@ConstructorResult(targetClass = CollectionSummaryEntity.class, columns = {
				@ColumnResult(name = "collectionid", type = String.class),
				@ColumnResult(name = "projectid", type = String.class),
				@ColumnResult(name = "name", type = String.class),
				@ColumnResult(name = "testcasecount", type = int.class),
				@ColumnResult(name = "crdtimes", type = LocalDateTime.class),
				@ColumnResult(name = "rundtimes", type = LocalDateTime.class),
				@ColumnResult(name = "runid", type = String.class),
				@ColumnResult(name = "collectionType", type = String.class)}) })
@NamedNativeQuery(name = "CollectionSummaryEntity.getCollectionsOfSbiProject", resultClass = CollectionSummaryEntity.class, query = "SELECT c.id AS collectionid, c.sbi_project_id AS projectid, c.name AS name, count(DISTINCT ctm.testcase_id) AS testcasecount, c.cr_dtimes AS crdtimes, MAX(tr.run_dtimes) AS rundtimes, MAX(tr.id) AS runid, c.collection_type AS collectionType FROM toolkit.collections AS c LEFT JOIN toolkit.collection_testcase_mapping AS ctm ON(c.id = ctm.collection_id) LEFT JOIN (SELECT * FROM (SELECT tr.id, tr.run_dtimes, tr.collection_id, ROW_NUMBER() OVER (PARTITION BY tr.collection_id ORDER BY tr.run_dtimes DESC) AS rank FROM toolkit.test_run AS tr) AS a WHERE rank <= 1) AS tr ON (c.id = tr.collection_id) WHERE c.sbi_project_id =:projectId AND c.partner_id =:partnerId AND c.is_deleted<>'true' GROUP BY c.id", resultSetMapping = "Mapping.CollectionSummaryEntity")
@NamedNativeQuery(name = "CollectionSummaryEntity.getCollectionsOfSdkProject", resultClass = CollectionSummaryEntity.class, query = "SELECT c.id AS collectionid, c.sdk_project_id AS projectid, c.name AS name, count(DISTINCT ctm.testcase_id) AS testcasecount, c.cr_dtimes AS crdtimes, MAX(tr.run_dtimes) AS rundtimes, MAX(tr.id) AS runid, c.collection_type AS collectionType FROM toolkit.collections AS c LEFT JOIN toolkit.collection_testcase_mapping AS ctm ON(c.id = ctm.collection_id) LEFT JOIN (SELECT * FROM (SELECT tr.id, tr.run_dtimes, tr.collection_id, ROW_NUMBER() OVER (PARTITION BY tr.collection_id ORDER BY tr.run_dtimes DESC) AS rank FROM toolkit.test_run AS tr) AS a WHERE rank <= 1) AS tr ON (c.id = tr.collection_id) WHERE c.sdk_project_id =:projectId AND c.partner_id =:partnerId AND c.is_deleted<>'true' GROUP BY c.id", resultSetMapping = "Mapping.CollectionSummaryEntity")
@NamedNativeQuery(name = "CollectionSummaryEntity.getCollectionsOfAbisProject", resultClass = CollectionSummaryEntity.class, query = "SELECT c.id AS collectionid, c.abis_project_id AS projectid, c.name AS name, count(DISTINCT ctm.testcase_id) AS testcasecount, c.cr_dtimes AS crdtimes, MAX(tr.run_dtimes) AS rundtimes, MAX(tr.id) AS runid, c.collection_type AS collectionType FROM toolkit.collections AS c LEFT JOIN toolkit.collection_testcase_mapping AS ctm ON(c.id = ctm.collection_id) LEFT JOIN (SELECT * FROM (SELECT tr.id, tr.run_dtimes, tr.collection_id, ROW_NUMBER() OVER (PARTITION BY tr.collection_id ORDER BY tr.run_dtimes DESC) AS rank FROM toolkit.test_run AS tr) AS a WHERE rank <= 1) AS tr ON (c.id = tr.collection_id) WHERE c.abis_project_id =:projectId AND c.partner_id =:partnerId AND c.is_deleted<>'true' GROUP BY c.id", resultSetMapping = "Mapping.CollectionSummaryEntity")
public class CollectionSummaryEntity {

	public CollectionSummaryEntity(String collectionId, String projectId, String name, int testCaseCount,
			LocalDateTime crDtimes, LocalDateTime runDtimes, String runId, String collectionType) {
		super();
		this.collectionId = collectionId;
		this.projectId = projectId;
		this.name = name;
		this.testCaseCount = testCaseCount;
		this.crDtimes = crDtimes;
		this.runDtimes = runDtimes;
		this.runId = runId;
		this.collectionType = collectionType;
	}

	@Id
	private String collectionId;
	private String projectId;
	private String name;
	private int testCaseCount;
	private LocalDateTime crDtimes;
	private LocalDateTime runDtimes;
	private String runId;
	private String collectionType;

}
/*
 * */
