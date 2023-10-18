package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.BiometricScoresEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository("BiometricScoresRepository")
public interface BiometricScoresRepository extends BaseRepository<BiometricScoresEntity, String> {
}
