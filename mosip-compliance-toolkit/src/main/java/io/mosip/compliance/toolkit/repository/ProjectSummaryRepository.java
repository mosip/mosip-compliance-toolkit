package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.ProjectSummaryEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the joins across tables.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Repository("ProjectSummaryRepository")
public interface ProjectSummaryRepository extends BaseRepository<ProjectSummaryEntity, String> {

	@Query(name = "ProjectSummaryEntity.getSummaryOfAllProjects", nativeQuery = true)
	public List<ProjectSummaryEntity> getSummaryOfAllProjects(@Param("partnerId") String partnerId);

	@Query(name = "ProjectSummaryEntity.getSummaryOfAllSBIProjects", nativeQuery = true)
	public List<ProjectSummaryEntity> getSummaryOfAllSBIProjects(@Param("partnerId") String partnerId);

	@Query(name = "ProjectSummaryEntity.getSummaryOfAllSDKProjects", nativeQuery = true)
	public List<ProjectSummaryEntity> getSummaryOfAllSDKProjects(@Param("partnerId") String partnerId);

	@Query(name = "ProjectSummaryEntity.getSummaryOfAllABISProjects", nativeQuery = true)
	public List<ProjectSummaryEntity> getSummaryOfAllABISProjects(@Param("partnerId") String partnerId);

}
