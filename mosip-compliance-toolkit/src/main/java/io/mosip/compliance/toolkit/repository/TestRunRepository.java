package io.mosip.compliance.toolkit.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunEntity;
import io.mosip.compliance.toolkit.entity.TestRunHistoryEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunRepository")
public interface TestRunRepository extends BaseRepository<TestRunEntity, String> {

	@Query("SELECT tr FROM TestRunEntity tr WHERE tr.id = ?1 AND tr.partnerId = ?2 AND tr.isDeleted<>'true'")
	public TestRunEntity getTestRunById(String runId, String partnerId);

	@Modifying
	@Transactional
	@Query("UPDATE TestRunEntity e SET e.executionDtimes= ?1,e.executionStatus= ?2, e.runStatus= ?3, e.updBy= ?4, e.updDtimes= ?5 WHERE e.id = ?6 and e.partnerId= ?7 AND e.isDeleted<>'true'")
	public int updateTestRunById(LocalDateTime excutionDtimes, String executionStatus, String runStatus, String upBy, LocalDateTime updDtimes, String id,
			String partnerId);

	@Query("SELECT e.partnerId FROM TestRunEntity e WHERE e.id = ?1 AND e.isDeleted<>'true' and e.partnerId= ?2")
	public String getPartnerIdByRunId(String id, String partnerId);

	@Query("SELECT new io.mosip.compliance.toolkit.entity.TestRunHistoryEntity(tr.id as runId, MAX(tr.runDtimes) AS last_run_time, COUNT(DISTINCT trd.testcaseId) AS testcase_count, COUNT(CASE WHEN trd.resultStatus = 'success' THEN 1 ELSE NULL END) as passcase_count, COUNT(CASE WHEN trd.resultStatus = 'failure' THEN 1 ELSE NULL END) as failcase_count) FROM TestRunEntity AS tr LEFT JOIN TestRunDetailsEntity AS trd ON (tr.id = trd.runId) WHERE tr.collectionId = ?1 AND tr.partnerId = ?2 AND tr.isDeleted<>'true' AND (trd.isDeleted<>'true' OR trd.isDeleted IS NULL) GROUP BY (tr.id) ORDER BY last_run_time DESC")
	public Page<TestRunHistoryEntity> getTestRunHistoryByCollectionId(Pageable pageable, String collectionId,
			String partnerId);

	@Query("SELECT COUNT(ct.testcaseId) FROM TestRunEntity AS tr LEFT JOIN CollectionTestCaseEntity AS ct ON (tr.collectionId = ct.collectionId) WHERE tr.id = ?1 AND tr.isDeleted<>'true' GROUP BY (ct.collectionId)")
	public int getTestCaseCount(String runId);

	@Query(value = "SELECT id FROM toolkit.test_run WHERE collection_id = ?1 AND partner_Id = ?3 AND is_deleted<>'true' ORDER BY run_dtimes DESC OFFSET ?2", nativeQuery = true)
	public List<String> getRunIdsWithOffset(String collectionId, int i, String partnerId);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO toolkit.test_run_archive (SELECT * FROM toolkit.test_run tr WHERE tr.id = ?1 AND tr.partner_id = ?2)", nativeQuery = true)
	public void copyTestRunToArchive(String runId, String partnerId);

	@Modifying
	@Transactional
	@Query("DELETE FROM TestRunEntity e WHERE e.id = ?1 AND e.partnerId = ?2")
	public void deleteById(String runId, String partnerId);
}
