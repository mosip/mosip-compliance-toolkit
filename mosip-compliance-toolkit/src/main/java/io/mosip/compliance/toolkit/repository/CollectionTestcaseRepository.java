package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.dto.CollectionTestcaseDto;
import io.mosip.compliance.toolkit.entity.CollectionTestcaseEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestcaseCollectionRepository")
public interface CollectionTestcaseRepository extends BaseRepository<CollectionTestcaseEntity, String> {

	@Query("SELECT new io.mosip.compliance.toolkit.dto.CollectionTestcaseDto(c.id, t.id, t.testcaseJson, t.testcaseType, t.specVersion) FROM toolkit.collection_testcase_mapping ctm JOIN ctm.collection c JOIN ctm.testcase t WHERE ctm.collection_id =:collectionId AND c.partner_id =:partnerId AND c.is_deleted<>'true'")
	public List<CollectionTestcaseDto> getTestcasesByCollectionId(@Param("collectionId") String collectionId,
			@Param("partnerId") String partnerId);

}
