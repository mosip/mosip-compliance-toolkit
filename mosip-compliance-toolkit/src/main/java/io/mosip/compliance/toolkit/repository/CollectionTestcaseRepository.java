package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.CollectionTestcaseEntity;
import io.mosip.compliance.toolkit.entity.CollectionTestcasePK;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestcaseCollectionRepository")
public interface CollectionTestcaseRepository extends BaseRepository<CollectionTestcaseEntity, CollectionTestcasePK> {

	@Query("SELECT t.testcaseJson FROM CollectionTestcaseEntity ctm JOIN ctm.collection c JOIN ctm.testcase t WHERE c.id =:collectionId AND c.partnerId =:partnerId AND c.isDeleted<>'true'")
	public List<String> getTestcasesByCollectionId(@Param("collectionId") String collectionId,
			@Param("partnerId") String partnerId);

}
