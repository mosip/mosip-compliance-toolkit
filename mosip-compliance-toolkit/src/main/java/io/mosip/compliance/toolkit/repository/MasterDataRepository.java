package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.MasterDataEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("MasterDataRepository")
public interface MasterDataRepository extends BaseRepository<MasterDataEntity, String>{
	
	@Query("SELECT e FROM MasterDataEntity e  WHERE e.name= ?1 order by e.crDate desc")
	public List<MasterDataEntity> findAllByName(String name);
	
}
