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
@SqlResultSetMapping(name = "Mapping.ProjectSummaryEntity", classes = {
		@ConstructorResult(targetClass = ProjectSummaryEntity.class, columns = {
				@ColumnResult(name = "projectId", type = String.class),
				@ColumnResult(name = "projectName", type = String.class),
				@ColumnResult(name = "projectType", type = String.class),
				@ColumnResult(name = "projectCrDate", type = LocalDateTime.class),
				@ColumnResult(name = "collectionsCount", type = long.class),
				@ColumnResult(name = "collectionId", type = String.class),
				@ColumnResult(name = "runDate", type = LocalDateTime.class),
				@ColumnResult(name = "runId", type = String.class),
				@ColumnResult(name = "resultStatus", type = String.class),}) })
@NamedNativeQuery(name = "ProjectSummaryEntity.getProjectsWithCollectionsCount", resultClass = ProjectSummaryEntity.class, 
		query = "select project_run.*, run_details.result_status as resultStatus from toolkit.test_run_details run_details "
				+ "right join (select project.*,run.run_dtimes as runDate, run.id as runId from toolkit.test_run run right join ( "
				+ "select sbi.id as projectId,sbi.name as projectName,sbi.project_type as projectType,sbi.cr_dtimes   "
				+ "as projectCrDate,count(c.id) as collectionsCount, c.id as collectionId from toolkit.sbi_projects sbi  "
				+ "LEFT JOIN toolkit.collections c on c.sbi_project_id = sbi.id where sbi.partner_id= :partnerId "
				+ "and sbi.is_deleted<>'true' group by sbi.id, c.id) as project on project.collectionId = run.collection_id) as project_run "
				+ "on project_run.runId = run_details.run_id "
				+ "union "
				+ "select project_run.*, run_details.result_status as runStatus from toolkit.test_run_details run_details "
				+ "right join (select project.*,run.run_dtimes as runDate, run.id as runId from toolkit.test_run run right join ( "
				+ "select sdk.id as projectId,sdk.name as projectName,sdk.project_type as projectType,sdk.cr_dtimes   "
				+ "as projectCrDate,count(c.id) as collectionsCount, c.id as collectionId from toolkit.sdk_projects sdk  "
				+ "LEFT JOIN toolkit.collections c on c.sdk_project_id = sdk.id where sdk.partner_id= :partnerId "
				+ "and sdk.is_deleted<>'true' group by sdk.id, c.id) as project on project.collectionId = run.collection_id) as project_run "
				+ "on project_run.runId = run_details.run_id "
				+ "union "
				+ "select project_run.*, run_details.result_status as runStatus from toolkit.test_run_details run_details "
				+ "right join (select project.*,run.run_dtimes as runDate, run.id as runId from toolkit.test_run run right join ( "
				+ "select abis.id as projectId,abis.name as projectName,abis.project_type as projectType,abis.cr_dtimes   "
				+ "as projectCrDate,count(c.id) as collectionsCount, c.id as collectionId from toolkit.abis_projects abis  "
				+ "LEFT JOIN toolkit.collections c on c.abis_project_id = abis.id where abis.partner_id= :partnerId "
				+ "and abis.is_deleted<>'true' group by abis.id, c.id) as project on project.collectionId = run.collection_id) as project_run "
				+ "on project_run.runId = run_details.run_id"
		,resultSetMapping = "Mapping.ProjectSummaryEntity")
public class ProjectSummaryEntity {

	public ProjectSummaryEntity(String projectId, String projectName, String projectType, LocalDateTime projectCrDate,
			long collectionsCount, String collectionId, LocalDateTime runDate, String runId, String resultStatus) {
		super();
		this.projectId = projectId;
		this.projectName = projectName;
		this.projectType = projectType;
		this.projectCrDate = projectCrDate;
		this.collectionsCount = collectionsCount;
		this.collectionId = collectionId;
		this.runDate = runDate;
		this.runId = runId;
		this.resultStatus = resultStatus;
	}
	
	@Id
	private String projectId;

	private String projectName;

	private String projectType;

	private LocalDateTime projectCrDate;

	private long collectionsCount;

	private String collectionId;

	private LocalDateTime runDate;
	
	private String runId;
	
	private String resultStatus;
}
