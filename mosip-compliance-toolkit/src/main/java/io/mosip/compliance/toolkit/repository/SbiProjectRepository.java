package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table sdk_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Repository("SbiProjectRepository")
public interface SbiProjectRepository extends BaseRepository<SbiProjectEntity, String> {

	@Query("SELECT e FROM SbiProjectEntity e  WHERE e.partnerId= ?1 and e.projectType='SBI' and e.isDeleted<>'true' order by e.crDate desc")
	public List<SbiProjectEntity> findAllByPartnerId(String partnerId);
}
