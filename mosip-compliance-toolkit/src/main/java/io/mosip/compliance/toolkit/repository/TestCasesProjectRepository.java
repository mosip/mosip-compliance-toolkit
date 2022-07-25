package io.mosip.compliance.toolkit.repository;

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
}
