package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.compliance.toolkit.entity.PartnerProfileEntityPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository("PartnerProfileRepository")
public interface PartnerProfileRepository
        extends BaseRepository<PartnerProfileEntity, PartnerProfileEntityPK> {
}