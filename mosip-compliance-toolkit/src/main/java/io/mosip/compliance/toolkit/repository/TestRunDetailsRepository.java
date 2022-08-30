package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunDetailsRepository")
public interface TestRunDetailsRepository extends BaseRepository<TestRunDetailsEntity, TestRunDetailsPK> {

	@Query("SELECT e FROM TestRunDetailsEntity e  WHERE e.runId= ?1 and e.isDeleted<>'true' order by e.crDtimes desc")
	public List<TestRunDetailsEntity> getTestRunDetails(String runId);

	@Query("SELECT COUNT(CASE WHEN LOWER(trd.resultStatus)='success' THEN 1 ELSE NULL END) FROM TestRunDetailsEntity AS trd WHERE trd.runId = ?1 AND trd.isDeleted<>'true'")
	public int getTestRunSuccessCount(String runId);

}
