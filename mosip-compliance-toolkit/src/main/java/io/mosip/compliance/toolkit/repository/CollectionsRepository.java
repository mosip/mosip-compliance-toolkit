package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("CollectionRepository")
public interface CollectionsRepository extends BaseRepository<CollectionEntity, String>{

	@Query("SELECT e FROM CollectionEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public CollectionEntity getCollectionById(String collectionId, String partnerId);
	
	@Query("SELECT e FROM CollectionEntity e  WHERE LOWER(e.name)= LOWER(?1) and e.sbiProjectId= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public List<CollectionEntity> getSbiCollectionByName(String name, String sbiProjectId, String partnerId);
	
	@Query("SELECT e FROM CollectionEntity e  WHERE LOWER(e.name)= LOWER(?1) and e.sdkProjectId= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public List<CollectionEntity> getSdkCollectionByName(String name, String sdkProjectId, String partnerId);

	@Query("SELECT e FROM CollectionEntity e  WHERE LOWER(e.name)= LOWER(?1) and e.abisProjectId= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public List<CollectionEntity> getAbisCollectionByName(String name, String abisProjectId, String partnerId);
	
	@Query("SELECT e.partnerId FROM CollectionEntity e  WHERE e.id= ?1 AND e.isDeleted<>'true' ORDER BY e.crDate DESC")
	public String getPartnerById(String collectionId);

	@Query("SELECT e.name FROM CollectionEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public String getCollectionNameById(String collectionId, String partnerId);

	@Query("SELECT e.id FROM CollectionEntity e WHERE e.sbiProjectId= ?1 and e.collectionType= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public String getSbiComplianceCollectionId(String sbiprojectId, String collectionType, String partnerId);

	@Query("SELECT e.id FROM CollectionEntity e WHERE e.sdkProjectId= ?1 and e.collectionType= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public String getSdkComplianceCollectionId(String sdkProjectId, String collectionType, String partnerId);

	@Query("SELECT e.id FROM CollectionEntity e WHERE e.abisProjectId= ?1 and e.collectionType= ?2 and e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public String getAbisComplianceCollectionId(String abisProjectId, String collectionType, String partnerId);
	
}
