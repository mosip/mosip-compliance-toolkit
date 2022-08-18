package io.mosip.compliance.toolkit.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.compliance.toolkit.entity.TestRunHistoryEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunRepository")
public interface TestRunRepository extends BaseRepository<TestRunEntity, String> {

	@Query("SELECT tr FROM TestRunEntity AS tr INNER JOIN CollectionEntity c ON (c.id = tr.collectionId) WHERE tr.id = ?1 AND c.partnerId = ?2 AND c.isDeleted<>'true' AND tr.isDeleted<>'true'")
	public TestRunEntity getTestRunById(String runId, String partnerId);

	@Modifying
	@Transactional
	@Query("UPDATE TestRunEntity e SET e.executionDtimes= ?1, e.updBy= ?2, e.updDtimes= ?3 WHERE e.id = ?4")
	public int updateExecutionDateById(LocalDateTime excutionDtimes, String upBy, LocalDateTime updDtimes, String id);

	@Query("SELECT c.partnerId FROM TestRunEntity t INNER JOIN CollectionEntity c ON (c.id = t.collectionId) WHERE t.id = ?1  AND c.isDeleted<>'true'")
	public String getPartnerIdByRunId(String id);

	@Query("SELECT new io.mosip.compliance.toolkit.entity.TestRunHistoryEntity(tr.id as runId, MAX(tr.runDtimes) AS last_run_time, COUNT(DISTINCT trd.testcaseId) AS testcase_count, COUNT(CASE WHEN trd.resultStatus = 'success' THEN 1 ELSE NULL END) as passcase_count, COUNT(CASE WHEN trd.resultStatus = 'failure' THEN 1 ELSE NULL END) as failcase_count) FROM TestRunEntity AS tr LEFT JOIN TestRunDetailsEntity AS trd ON (tr.id = trd.runId) LEFT JOIN CollectionEntity AS c ON (tr.collectionId = c.id) WHERE tr.collectionId = ?1 AND c.partnerId = ?2 AND c.isDeleted<>'true' AND tr.isDeleted<>'true' AND trd.isDeleted<>'true' GROUP BY (tr.id)")
	public List<TestRunHistoryEntity> getTestRunHistoryByCollectionId(Pageable pageable, String collectionId,
			String partnerId);

	@Query("SELECT (CASE WHEN (COUNT(trd.testcaseId) < 1) THEN false WHEN (COUNT(trd.testcaseId))=(COUNT(CASE WHEN trd.resultStatus='success' THEN 1 ELSE NULL END)) THEN true ELSE false END) AS resultStatus FROM TestRunDetailsEntity AS trd WHERE trd.runId = ?1")
	public boolean getTestRunStatus(String runId);
}
