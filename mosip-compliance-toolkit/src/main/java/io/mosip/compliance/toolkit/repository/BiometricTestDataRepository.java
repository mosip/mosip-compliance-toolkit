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

	@Query("SELECT e FROM BiometricTestDataEntity e  WHERE e.id= ?1 AND e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDate desc")
	public BiometricTestDataEntity findById(String id, String partnerId);

	@Query("SELECT e.name FROM BiometricTestDataEntity e  WHERE e.fileId IN ?1 AND e.purpose= ?2 AND e.partnerId= ?3 and e.isDeleted<>'true' order by e.crDate desc")
	public List<String> findTestDataNamesByFileIds(String[] filenames, String purpose, String partnerId);

	@Query("SELECT e FROM BiometricTestDataEntity e  WHERE e.name= ?1 AND e.partnerId= ?2 AND e.isDeleted<>'true' order by e.crDate desc")
	public BiometricTestDataEntity findByTestDataName(String name, String partnerId);
}