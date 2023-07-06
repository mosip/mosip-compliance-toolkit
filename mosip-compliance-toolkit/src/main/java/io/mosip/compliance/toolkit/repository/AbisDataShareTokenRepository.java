package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.AbisDataShareTokenEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository("AbisDataShareTokenRepository")
public interface AbisDataShareTokenRepository extends BaseRepository<AbisDataShareTokenEntity, String> {
}
