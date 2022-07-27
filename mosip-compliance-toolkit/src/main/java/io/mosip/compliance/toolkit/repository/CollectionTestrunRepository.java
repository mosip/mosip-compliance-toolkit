package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionTestrunEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("CollectionsSummaryRepository")
public interface CollectionTestrunRepository extends BaseRepository<CollectionTestrunEntity, String>{

	@Query(name = "CollectionsSummaryEntity.getCollectionsOfSbiProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfSbiProjects(
			@Param("projectId") String projectId, @Param("partnerId") String partnerId);
	
	@Query(name = "CollectionsSummaryEntity.getCollectionsOfSdkProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfSdkProjects(
			@Param("projectId") String projectId, @Param("partnerId") String partnerId);
	
	@Query(name = "CollectionsSummaryEntity.getCollectionsOfAbisProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfAbisProjects(
			@Param("projectId") String projectId, @Param("partnerId") String partnerId);

}
