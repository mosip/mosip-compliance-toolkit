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


/**
 * This entity class defines the many named native queries and their mappings.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Entity
@Getter
@Setter
@ToString
@SqlResultSetMapping(name = "Mapping.ProjectSummaryEntity", classes = {
		@ConstructorResult(targetClass = ProjectSummaryEntity.class, columns = {
				@ColumnResult(name = "projectid", type = String.class),
				@ColumnResult(name = "projectname", type = String.class),
				@ColumnResult(name = "projecttype", type = String.class),
				@ColumnResult(name = "projectcrdate", type = LocalDateTime.class),
				@ColumnResult(name = "collectionscount", type = long.class),
				@ColumnResult(name = "collectionid", type = String.class),
				@ColumnResult(name = "runid", type = String.class),
				@ColumnResult(name = "rundate", type = LocalDateTime.class),
				@ColumnResult(name = "runstatus", type = String.class), }) })
@NamedNativeQuery(
		name = "ProjectSummaryEntity.getSummaryOfAllProjects", 
		resultClass = ProjectSummaryEntity.class, 
		query = "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT sbi.id AS projectId, sbi.NAME AS projectName, sbi.project_type AS projectType, sbi.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.sbi_projects sbi LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.sbi_project_id = sbi.id WHERE sbi.partner_id = :partnerId AND sbi.is_deleted <> 'true' GROUP BY sbi.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.sbiprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.sbi_project_id AS sbiProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.sbi_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.sbiprojectid "
		+ "union "
		+ "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT sdk.id AS projectId, sdk.NAME AS projectName, sdk.project_type AS projectType, sdk.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.sdk_projects sdk LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.sdk_project_id = sdk.id WHERE sdk.partner_id = :partnerId AND sdk.is_deleted <> 'true' GROUP BY sdk.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.sdkprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.sdk_project_id AS sdkProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.sdk_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.sdkprojectid "
		+ "union "
		+ "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT abis.id AS projectId, abis.NAME AS projectName, abis.project_type AS projectType, abis.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.abis_projects abis LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.abis_project_id = abis.id WHERE abis.partner_id = :partnerId AND abis.is_deleted <> 'true' GROUP BY abis.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.abisprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.abis_project_id AS abisProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.abis_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.abisprojectid", 
		resultSetMapping = "Mapping.ProjectSummaryEntity")
@NamedNativeQuery(
		name = "ProjectSummaryEntity.getSummaryOfAllSBIProjects", 
		resultClass = ProjectSummaryEntity.class, 
		query = "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT sbi.id AS projectId, sbi.NAME AS projectName, sbi.project_type AS projectType, sbi.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.sbi_projects sbi LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.sbi_project_id = sbi.id WHERE sbi.partner_id = :partnerId AND sbi.is_deleted <> 'true' GROUP BY sbi.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.sbiprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.sbi_project_id AS sbiProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.sbi_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.sbiprojectid ", 
		resultSetMapping = "Mapping.ProjectSummaryEntity")
@NamedNativeQuery(
		name = "ProjectSummaryEntity.getSummaryOfAllSDKProjects", 
		resultClass = ProjectSummaryEntity.class, 
		query = "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT sdk.id AS projectId, sdk.NAME AS projectName, sdk.project_type AS projectType, sdk.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.sdk_projects sdk LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.sdk_project_id = sdk.id WHERE sdk.partner_id = :partnerId AND sdk.is_deleted <> 'true' GROUP BY sdk.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.sdkprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.sdk_project_id AS sdkProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.sdk_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.sdkprojectid ", 
		resultSetMapping = "Mapping.ProjectSummaryEntity")
@NamedNativeQuery(
		name = "ProjectSummaryEntity.getSummaryOfAllABISProjects", 
		resultClass = ProjectSummaryEntity.class, 
		query = "SELECT projectid, projectname, projecttype, projectcrdate, collectionscount, collectionid, runid, rundate, runstatus FROM ( SELECT abis.id AS projectId, abis.NAME AS projectName, abis.project_type AS projectType, abis.cr_dtimes AS projectCrDate, Count(c.id) AS collectionsCount FROM toolkit.abis_projects abis LEFT JOIN ( SELECT c.* FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' ) c ON c.abis_project_id = abis.id WHERE abis.partner_id = :partnerId AND abis.is_deleted <> 'true' GROUP BY abis.id ) a LEFT JOIN ( SELECT * FROM ( SELECT results.*, Row_number() OVER ( PARTITION BY results.abisprojectid ORDER BY results.rundate DESC ) AS Rank FROM ( SELECT project_run.*, run_details.result_status AS runStatus FROM toolkit.test_run_details run_details RIGHT JOIN ( SELECT c.*, run.run_dtimes AS runDate, run.id AS runId FROM toolkit.test_run run RIGHT JOIN ( SELECT c.id AS collectionId, c.abis_project_id AS abisProjectId FROM toolkit.collections c WHERE c.partner_id = :partnerId AND c.is_deleted <> 'true' AND c.abis_project_id <> 'null' ) c ON run.collection_id = c.collectionid ORDER BY run.run_dtimes ) project_run ON project_run.runid = run_details.run_id ) results ) final_results WHERE rank <= 1 ) b ON a.projectid = b.abisprojectid", 
		resultSetMapping = "Mapping.ProjectSummaryEntity")
public class ProjectSummaryEntity {

	public ProjectSummaryEntity(String projectid, String projectname, String projecttype, LocalDateTime projectcrdate,
			long collectionscount, String collectionid, String runid, LocalDateTime rundate, String runstatus) {
		super();
		this.projectId = projectid;
		this.projectName = projectname;
		this.projectType = projecttype;
		this.projectCrDate = projectcrdate;
		this.collectionsCount = collectionscount;
		this.collectionId = collectionid;
		this.runId = runid;
		this.runDate = rundate;
		this.runStatus = runstatus;
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

	private String runStatus;
}
