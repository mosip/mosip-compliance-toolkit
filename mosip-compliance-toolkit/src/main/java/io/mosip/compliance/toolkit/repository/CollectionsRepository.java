package io.mosip.compliance.toolkit.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("CollectionRepository")
public interface CollectionsRepository extends BaseRepository<CollectionEntity, String>{

	@Query("SELECT e FROM CollectionEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public CollectionEntity getCollectionById(String collectionId, String partnerId);
	
	@Query("SELECT e.partnerId FROM CollectionEntity e  WHERE e.id= ?1 AND e.isDeleted<>'true' ORDER BY e.crDate DESC")
	public String getPartnerById(String collectionId);
}
