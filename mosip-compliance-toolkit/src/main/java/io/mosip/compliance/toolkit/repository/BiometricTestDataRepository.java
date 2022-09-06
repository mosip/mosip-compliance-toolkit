package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("BiometricTestDataRepository")
public interface BiometricTestDataRepository extends BaseRepository<BiometricTestDataEntity, String> {

	@Query("SELECT e FROM BiometricTestDataEntity e  WHERE e.partnerId= ?1 and e.isDeleted<>'true' order by e.crDate desc")
	public List<BiometricTestDataEntity> findAllByPartnerId(String partnerId);

	@Query("SELECT e.fileId FROM BiometricTestDataEntity e  WHERE e.id= ?1 AND e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public String findFileNameById(String id, String partnerId);

}
