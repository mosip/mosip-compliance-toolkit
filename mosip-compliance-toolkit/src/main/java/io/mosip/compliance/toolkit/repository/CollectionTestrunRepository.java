package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionTestrunEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("CollectionTestrunRepository")
public interface CollectionTestrunRepository extends BaseRepository<CollectionTestrunEntity, String> {

	@Query(name = "CollectionTestrunEntity.getCollectionsOfSbiProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfSbiProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

	@Query(name = "CollectionTestrunEntity.getCollectionsOfSdkProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfSdkProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

	@Query(name = "CollectionTestrunEntity.getCollectionsOfAbisProject", nativeQuery = true)
	public List<CollectionTestrunEntity> getCollectionsOfAbisProjects(@Param("projectId") String projectId,
			@Param("partnerId") String partnerId);

}
