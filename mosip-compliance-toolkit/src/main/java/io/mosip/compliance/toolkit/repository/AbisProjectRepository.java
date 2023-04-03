package io.mosip.compliance.toolkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table abis_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Repository("AbisProjectRepository")
public interface AbisProjectRepository extends BaseRepository<AbisProjectEntity, String> {

	@Query("SELECT e FROM AbisProjectEntity e  WHERE e.partnerId= ?1 and e.projectType='ABIS' and e.isDeleted<>'true' order by e.crDate desc")
	public List<AbisProjectEntity> findAllByPartnerId(String partnerId);
	
	@Query("SELECT e FROM AbisProjectEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.projectType='ABIS' and e.isDeleted<>'true' order by e.crDate desc")
	public Optional<AbisProjectEntity> findById(String id, String partnerId);
}
