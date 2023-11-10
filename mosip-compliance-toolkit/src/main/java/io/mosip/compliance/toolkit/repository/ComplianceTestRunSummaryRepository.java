package io.mosip.compliance.toolkit.repository;

import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("ComplianceTestRunSummaryRepository")
public interface ComplianceTestRunSummaryRepository extends BaseRepository<ComplianceTestRunSummaryEntity, ComplianceTestRunSummaryPK> {
}
