package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionTestCaseEntity;
import io.mosip.compliance.toolkit.entity.CollectionTestCasePK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestCaseCollectionRepository")
public interface CollectionTestCaseRepository extends BaseRepository<CollectionTestCaseEntity, CollectionTestCasePK> {

	@Query("SELECT t.testcaseJson FROM CollectionTestCaseEntity ctm JOIN ctm.collection c JOIN ctm.testcase t WHERE c.id =:collectionId AND c.partnerId =:partnerId AND c.isDeleted<>true")
	public List<String> getTestCasesByCollectionId(@Param("collectionId") String collectionId,
			@Param("partnerId") String partnerId);

}
