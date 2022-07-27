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
@SqlResultSetMapping(name = "Mapping.CollectionsSummaryEntity", classes = {
		@ConstructorResult(targetClass = CollectionTestrunEntity.class, columns = {
				@ColumnResult(name = "collectionid", type = String.class),
				@ColumnResult(name = "projectid", type = String.class),
				@ColumnResult(name = "name", type = String.class),
				@ColumnResult(name = "crdtimes", type = LocalDateTime.class),
				@ColumnResult(name = "rundtimes", type = LocalDateTime.class)}) })
@NamedNativeQuery(
		name = "CollectionsSummaryEntity.getCollectionsOfSbiProject", 
		resultClass = CollectionTestrunEntity.class, 
		query = "SELECT c.id AS collectionid, c.sbi_project_id AS projectid, c.name AS name, c.cr_dtimes AS crdtimes, tr.run_dtimes AS rundtimes FROM collections AS c LEFT JOIN test_run AS tr ON (c.id = tr.collection_id) WHERE c.sbi_project_id = :projectId AND c.partner_id = :partnerId AND c.is_deleted<>'true' AND (tr.run_dtimes = (SELECT MAX(run_dtimes) FROM test_run AS tr2 WHERE tr2.collection_id = c.id) OR tr.run_dtimes IS NULL)", 
		resultSetMapping = "Mapping.CollectionsSummaryEntity")
@NamedNativeQuery(
		name = "CollectionsSummaryEntity.getCollectionsOfSdkProject", 
		resultClass = CollectionTestrunEntity.class, 
		query = "SELECT c.id AS collectionid, c.sdk_project_id AS projectid, c.name AS name, c.cr_dtimes AS crdtimes, tr.run_dtimes AS rundtimes FROM collections AS c LEFT JOIN test_run AS tr ON (c.id = tr.collection_id) WHERE c.sdk_project_id = :projectId AND c.partner_id = :partnerId AND c.is_deleted<>'true' AND (tr.run_dtimes = (SELECT MAX(run_dtimes) FROM test_run AS tr2 WHERE tr2.collection_id = c.id) OR tr.run_dtimes IS NULL)", 
		resultSetMapping = "Mapping.CollectionsSummaryEntity")
@NamedNativeQuery(
		name = "CollectionsSummaryEntity.getCollectionsOfAbisProject", 
		resultClass = CollectionTestrunEntity.class, 
		query = "SELECT c.id AS collectionid, c.abis_project_id AS projectid, c.name AS name, c.cr_dtimes AS crdtimes, tr.run_dtimes AS rundtimes FROM collections AS c LEFT JOIN test_run AS tr ON (c.id = tr.collection_id) WHERE c.abis_project_id = :projectId AND c.partner_id = :partnerId AND c.is_deleted<>'true' AND (tr.run_dtimes = (SELECT MAX(run_dtimes) FROM test_run AS tr2 WHERE tr2.collection_id = c.id) OR tr.run_dtimes IS NULL)", 
		resultSetMapping = "Mapping.CollectionsSummaryEntity")
public class CollectionTestrunEntity {
	
	public CollectionTestrunEntity(String collectionId, String projectId, String name, LocalDateTime crDtimes, LocalDateTime runDtimes) {
		super();
		this.collectionId = collectionId;
		this.projectId = projectId;
		this.name = name;
		this.crDtimes = crDtimes;
		this.runDtimes = runDtimes;
	}
	
	@Id
	private String collectionId;
	private String projectId;
	private String name;
	private LocalDateTime crDtimes;
	private LocalDateTime runDtimes;
}
