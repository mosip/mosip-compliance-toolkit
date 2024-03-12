package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.*;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("PartnerProfileRepository")
public interface PartnerProfileRepository
        extends BaseRepository<PartnerProfileEntity, PartnerProfileEntityPK> {
}
