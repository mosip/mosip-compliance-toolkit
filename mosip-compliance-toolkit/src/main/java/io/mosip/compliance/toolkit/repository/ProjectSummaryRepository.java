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

	//@Query("select sbi.id as projectId,sbi.name as projectName,sbi.projectType as projectType,sbi.crDate as projectCrDate,count(c.id) as collectionsCount, c.id as collectionId from SbiProjectEntity sbi LEFT JOIN CollectionEntity c on c.sbiProjectId = sbi.id where sbi.partnerId=?1 group by sbi.id, c.id")
	@Query(name = "ProjectSummaryEntity.getProjectsWithCollectionsCount", nativeQuery = true)
	public List<ProjectSummaryEntity> getProjectsWithCollectionsCount(@Param("partnerId") String partnerId);

}
