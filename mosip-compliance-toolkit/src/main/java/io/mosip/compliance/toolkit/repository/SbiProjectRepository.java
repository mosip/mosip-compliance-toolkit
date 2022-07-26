package io.mosip.compliance.toolkit.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.SbiProjectEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table sdk_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Repository("SbiProjectRepository")
@Transactional(rollbackOn = { Exception.class })
public interface SbiProjectRepository extends BaseRepository<SbiProjectEntity, String> {

	@Query("SELECT e FROM SbiProjectEntity e  WHERE e.partnerId= ?1 and e.id= ?2 and e.projectType='SBI' and e.isDeleted<>'true' order by e.crDate desc")
	public List<SbiProjectEntity> findAllByPartnerId(String partnerId, String id);
	
	@Query("UPDATE SbiProjectEntity e set e.isDeleted='true', e.delTime= ?1 Where e.id= ?2")
	public List<SbiProjectEntity> deleteByProjectId(LocalDateTime delDtimes, String projectId);
}
