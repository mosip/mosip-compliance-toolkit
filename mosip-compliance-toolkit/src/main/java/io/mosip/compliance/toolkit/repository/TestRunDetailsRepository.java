package io.mosip.compliance.toolkit.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsPK;
import io.mosip.compliance.toolkit.entity.TestRunPartialDetailsEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunDetailsRepository")
public interface TestRunDetailsRepository extends BaseRepository<TestRunDetailsEntity, TestRunDetailsPK> {

	@Query("SELECT e FROM TestRunDetailsEntity e  WHERE e.runId= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDtimes desc")
	public List<TestRunDetailsEntity> getTestRunDetails(String runId, String partnerId);
	
	@Query("SELECT e FROM TestRunPartialDetailsEntity e  WHERE e.runId= ?1 and e.partnerId= ?2 and e.isDeleted<>'true' order by e.crDtimes desc")
	public List<TestRunPartialDetailsEntity> getTestRunPartialDetails(String runId, String partnerId);

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO toolkit.test_run_details_archive (SELECT * FROM toolkit.test_run_details trd WHERE trd.run_id = ?1 AND trd.partner_id = ?2)", nativeQuery = true)
	public void copyTestRunDetailsToArchive(String runId, String partnerId);

	@Modifying
	@Transactional
	@Query("DELETE FROM TestRunDetailsEntity e WHERE e.runId = ?1 AND e.partnerId = ?2")
	public void deleteById(String runId, String partnerId);
	
	@Query("SELECT e FROM TestRunDetailsEntity e  WHERE e.runId= ?1 and e.partnerId= ?2 and e.testcaseId= ?3 and e.methodId= ?4 and e.isDeleted<>'true' order by e.crDtimes desc")
	public TestRunDetailsEntity getMethodDetails(String runId, String partnerId, String testcaseId, String methodId);
	
}
