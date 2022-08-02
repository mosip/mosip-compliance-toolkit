package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.dto.CollectionDto;
import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.compliance.toolkit.entity.CollectionSummaryEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("CollectionsRepository")
public interface CollectionsSummaryRepository extends BaseRepository<CollectionSummaryEntity, String> {

	@Query(name = "CollectionSummaryEntity.getCollectionsOfSbiProject", nativeQuery = true)
	public List<CollectionSummaryEntity> getCollectionsOfSbiProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

	@Query(name = "CollectionSummaryEntity.getCollectionsOfSdkProject", nativeQuery = true)
	public List<CollectionSummaryEntity> getCollectionsOfSdkProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

	@Query(name = "CollectionSummaryEntity.getCollectionsOfAbisProject", nativeQuery = true)
	public List<CollectionSummaryEntity> getCollectionsOfAbisProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

	@Query("SELECT e FROM CollectionEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public CollectionEntity getCollectionById(String collectionId, String partnerId);

}
