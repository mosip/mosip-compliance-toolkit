package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.PartnerProfileEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("PartnerProfileRepository")
public interface PartnerProfileRepository extends BaseRepository<PartnerProfileEntity, String> {
    @Query("SELECT e FROM PartnerProfileEntity e WHERE e.partnerId = :partnerId")
    public Optional<PartnerProfileEntity> findByPartnerId(@Param("partnerId") String partnerId);
}