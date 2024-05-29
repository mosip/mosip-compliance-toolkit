package io.mosip.compliance.toolkit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.SdkProjectEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table sdk_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Repository("SdkProjectRepository")
public interface SdkProjectRepository extends BaseRepository<SdkProjectEntity, String> {

	@Query("SELECT e FROM SdkProjectEntity e  WHERE e.partnerId= ?1 and e.projectType='SDK' and e.isDeleted<>true order by e.crDate desc")
	public List<SdkProjectEntity> findAllByPartnerId(String partnerId);

	@Query("SELECT e FROM SdkProjectEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.projectType='SDK' and e.isDeleted<>true order by e.crDate desc")
	public Optional<SdkProjectEntity> findById(String id, String partnerId);

	@Query("SELECT e.name FROM SdkProjectEntity e  WHERE e.id= ?1 and e.partnerId= ?2 and e.isDeleted<>true order by e.crDate desc")
	public String getProjectNameById(String projectId, String partnerId);
}
