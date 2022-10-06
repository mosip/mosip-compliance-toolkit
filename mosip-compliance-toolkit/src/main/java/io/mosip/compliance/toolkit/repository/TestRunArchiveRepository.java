package io.mosip.compliance.toolkit.repository;

import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestRunArchiveEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestRunArchiveRepository")
public interface TestRunArchiveRepository extends BaseRepository<TestRunArchiveEntity, String> {

}
