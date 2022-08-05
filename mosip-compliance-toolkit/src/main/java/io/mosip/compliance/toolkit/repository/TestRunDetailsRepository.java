package io.mosip.compliance.toolkit.repository;

import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunDetailsEntity;
import io.mosip.compliance.toolkit.entity.TestRunDetailsPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunDetailsRepository")
public interface TestRunDetailsRepository extends BaseRepository<TestRunDetailsEntity, TestRunDetailsPK> {

}
