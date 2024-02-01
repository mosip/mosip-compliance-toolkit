package io.mosip.compliance.toolkit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.compliance.toolkit.entity.BiometricScoresSummaryEntity;
import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;

@Repository("BiometricScoresSummaryRepository")
public interface BiometricScoresSummaryRepository extends BaseRepository<BiometricScoresSummaryEntity, String> {

	@Query(name = "BiometricScoresSummaryEntity.getBiometricScoresForFinger", nativeQuery = true)
	public List<BiometricScoresSummaryEntity> getBiometricScoresForFinger(@Param("partnerId") String partnerId,
			@Param("projectId") String projectId, @Param("testRunId") String testRunId,
			@Param("name") String name, @Param("biometricType") String biometricType,
			@Param("ageGroup") String ageGroup, @Param("occupation") String occupation);

	@Query(name = "BiometricScoresSummaryEntity.getBiometricScoresForChildFinger", nativeQuery = true)
	public List<BiometricScoresSummaryEntity> getBiometricScoresForChildFinger(@Param("partnerId") String partnerId,
			@Param("projectId") String projectId, @Param("testRunId") String testRunId,
			@Param("name") String name, @Param("biometricType") String biometricType,
			@Param("ageGroup") String ageGroup);
	
	@Query(name = "BiometricScoresSummaryEntity.getBiometricScoresForFace", nativeQuery = true)
	public List<BiometricScoresSummaryEntity> getBiometricScoresForFace(@Param("partnerId") String partnerId,
			@Param("projectId") String projectId, @Param("testRunId") String testRunId,
			@Param("name") String name, @Param("biometricType") String biometricType,
			@Param("ageGroup") String ageGroup, @Param("race") String race);

	@Query(name = "BiometricScoresSummaryEntity.getBiometricScoresForIris", nativeQuery = true)
	public List<BiometricScoresSummaryEntity> getBiometricScoresForIris(@Param("partnerId") String partnerId,
		   @Param("projectId") String projectId, @Param("testRunId") String testRunId,
		   @Param("name") String name, @Param("biometricType") String biometricType,
		   @Param("ageGroup") String ageGroup);

}
