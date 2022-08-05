package io.mosip.compliance.toolkit.repository;

import java.time.LocalDateTime;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunRepository")
public interface TestRunRepository extends BaseRepository<TestRunEntity, String> {

	@Modifying
	@Transactional
	@Query("UPDATE TestRunEntity e SET e.executionDtimes= ?1, e.updBy= ?2, e.updDtimes= ?3 WHERE e.id = ?4")
	public int updateExecutionDateById(LocalDateTime excutionDtimes, String upBy, LocalDateTime updDtimes, String id);
	
	@Query("SELECT c.partnerId FROM TestRunEntity t INNER JOIN CollectionEntity c ON (c.id = t.collectionId) WHERE t.id = ?1  AND c.isDeleted<>'true'")
	public String getPartnerIdByRunId(String id);
}
