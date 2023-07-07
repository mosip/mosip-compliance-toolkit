package io.mosip.compliance.toolkit.repository;

import io.mosip.compliance.toolkit.entity.AbisDataShareTokenEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("AbisDataShareTokenRepository")
public interface AbisDataShareTokenRepository extends BaseRepository<AbisDataShareTokenEntity, String> {
    @Query("SELECT t FROM AbisDataShareTokenEntity t WHERE t.partnerId = :partnerId AND t.testCaseId = :testCaseId AND t.testRunId = :testRunId")
    Optional<AbisDataShareTokenEntity> findByAllIds(@Param("partnerId") String partnerId,
            @Param("testCaseId") String testCaseId, @Param("testRunId") String testRunId);

    @Modifying
    @Query("UPDATE AbisDataShareTokenEntity t SET t.result = :result WHERE t.token = :token")
    int updateResultByToken(@Param("result") String result, @Param("token") String token);
}
