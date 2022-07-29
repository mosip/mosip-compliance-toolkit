package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestCaseProjectEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

/**
 * This repository class defines the database table testcase.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 *
 */
@Repository("TestCasesProjectRepository")
public interface TestCasesProjectRepository extends BaseRepository<TestCaseProjectEntity, String> {

	@Query("SELECT e FROM TestCaseProjectEntity e WHERE e.testcaseType='SBI' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseProjectEntity> findAllSbiTestCaseBySpecVersion(String specVersion);

	@Query("SELECT e FROM TestCaseProjectEntity e WHERE e.testcaseType='SDK' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseProjectEntity> findAllSdkTestCaseBySpecVersion(String specVersion);

	@Query("SELECT e FROM TestCaseProjectEntity e WHERE e.testcaseType='ABIS' and e.specVersion= ?1 order by e.id asc")
	public List<TestCaseProjectEntity> findAllAbisTestCaseBySpecVersion(String specVersion);
}
