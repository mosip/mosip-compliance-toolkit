package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.TestcaseCollectionEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("TestcaseCollectionRepository")
public interface TestcaseCollectionRepository extends BaseRepository<TestcaseCollectionEntity, String> {

	@Query(name = "TestcaseCollectionEntity.getTestcasesByCollectionId", nativeQuery = true)
	public List<TestcaseCollectionEntity> getTestcasesByCollectionId(@Param("collectionId") String collectionId,
			@Param("partnerId") String partnerId);

}
