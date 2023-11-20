package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryEntity;
import io.mosip.compliance.toolkit.entity.ComplianceTestRunSummaryPK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("ComplianceTestRunSummaryRepository")
public interface ComplianceTestRunSummaryRepository
		extends BaseRepository<ComplianceTestRunSummaryEntity, ComplianceTestRunSummaryPK> {

	@Query("SELECT e FROM ComplianceTestRunSummaryEntity e  WHERE e.reportStatus= ?1 and e.isDeleted<>'true' order by e.crDtimes desc")
	public List<ComplianceTestRunSummaryEntity> findAllByReportStatus(String reportStatus);

	@Query("SELECT e FROM ComplianceTestRunSummaryEntity e  WHERE e.partnerId= ?1 and e.isDeleted<>'true' and e.reportStatus<>'draft' order by e.crDtimes desc")
	public List<ComplianceTestRunSummaryEntity> findAllBySubmittedReportsPartnerId(String partnerId);

}
