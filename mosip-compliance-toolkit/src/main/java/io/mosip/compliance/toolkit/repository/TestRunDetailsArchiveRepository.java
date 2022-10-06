package io.mosip.compliance.toolkit.repository;

import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunDetailsArchiveEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunDetailsArchiveRepository")
public interface TestRunDetailsArchiveRepository extends BaseRepository<TestRunDetailsArchiveEntity, TestRunDetailsPK> {

}
