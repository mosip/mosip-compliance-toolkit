package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.dto.testrun.TestCaseSummaryDto;
import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunDetailsRepository")
public interface TestRunDetailsRepository extends BaseRepository<TestRunDetailsEntity, TestRunDetailsPK> {

	@Query("SELECT new io.mosip.compliance.toolkit.dto.testrun.TestCaseSummaryDto(t.id AS testId, t.testcaseType AS testCaseType, t.specVersion AS specVersion, trd.methodRequest AS methodRequest, trd.methodResponse AS methodResponse, trd.resultStatus AS resultStatus, trd.resultDescription AS resultDescription) FROM TestRunDetailsEntity AS trd LEFT JOIN TestCaseEntity AS t ON (trd.testcaseId = t.id) WHERE trd.runId = ?1 AND trd.isDeleted<>'true'")
	public List<TestCaseSummaryDto> getTestRunSummary(String runId);
}
