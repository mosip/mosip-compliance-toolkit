package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table testcase.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 *
 */
@Repository("TestCasesRepository")
public interface TestCasesRepository extends BaseRepository<TestCaseEntity, String> {

	@Query("SELECT e FROM TestCaseEntity e WHERE e.testcaseType='SBI' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseEntity> findAllSbiTestCaseBySpecVersion(String specVersion);

	@Query("SELECT e FROM TestCaseEntity e WHERE e.testcaseType='SDK' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseEntity> findAllSdkTestCaseBySpecVersion(String specVersion);

	@Query("SELECT e FROM TestCaseEntity e WHERE e.testcaseType='ABIS' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseEntity> findAllAbisTestCaseBySpecVersion(String specVersion);
}
