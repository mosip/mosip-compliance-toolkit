package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.PartnerConsentEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("PartnerConsentRepository")
public interface PartnerConsentRepository extends BaseRepository<PartnerConsentEntity, String> {
    @Query("SELECT e FROM PartnerConsentEntity e WHERE e.partnerId = :partnerId")
    public Optional<PartnerConsentEntity> findByPartnerId(@Param("partnerId") String partnerId);
}