package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.dto.report.BiometricScores;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FingerBioScoresTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FingerBioScoresTable.FingerBioScoresRow;
import io.mosip.compliance.toolkit.entity.BiometricScoresEntity;
import io.mosip.compliance.toolkit.entity.BiometricScoresSummaryEntity;
import io.mosip.compliance.toolkit.repository.BiometricScoresRepository;
import io.mosip.compliance.toolkit.repository.BiometricScoresSummaryRepository;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class BiometricScoresService {

	@Autowired
	private BiometricScoresSummaryRepository biometricScoresSummaryRepository;

	@Autowired
	private BiometricScoresRepository biometricScoresRepository;

	@Autowired
	ResourceCacheService resourceCacheService;

	private Logger log = LoggerConfiguration.logConfig(SbiProjectService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	private String getUserBy() {
		String crBy = authUserDetails().getMail();
		return crBy;
	}

	public void addBiometricScores(String projectId, String testRunId, String testId, String scoresJson) {
		try {
			if (projectId != null && testRunId != null && testId != null && scoresJson != null) {
				LocalDateTime crDate = LocalDateTime.now();
				BiometricScoresEntity entity = new BiometricScoresEntity();
				entity.setId(RandomIdGenerator.generateUUID(AppConstants.SBI.toLowerCase(), "", 36));
				entity.setProjectId(projectId);
				entity.setPartnerId(getPartnerId());
				entity.setOrgName(resourceCacheService.getOrgName(getPartnerId()));
				entity.setScoresJson(scoresJson);
				entity.setCrDate(crDate);
				entity.setCrBy(getUserBy());
				entity.setTestRunId(testRunId);
				entity.setTestCaseId(testId);
				biometricScoresRepository.save(entity);
			} else {
				// only log the exception since this is a fail safe situation
				log.error("sessionId", "idType", "id",
						"Biometric scores could not be added for this quality assessment testcase: {}", testId);
			}
		} catch (Exception ex) {
			// only log the exception since this is a fail safe situation
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addBiometricScores method of BiometricScoresService Service - " + ex.getMessage());
		}
	}

	public List<BiometricScores> getFingerBiometricScoresList(String partnerId, String projectId, String testRunId)
			throws Exception {
		List<BiometricScores> biometricScoresList = new ArrayList<BiometricScores>();
		try {
			String biometricType = AppConstants.BIOMETRIC_SCORES_FINGER;
			String[] sdkNames = new String[] { "BQAT SDK", "SBI" };
			String[] ageGroups = new String[] { "child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)" };
			String[] occupations = new String[] { "labourer", "non-labourer" };
			String[] scoreRanges = new String[] { "0-10", "11-20", "21-30", "31-40", "41-50", "51-60", "61-70", "71-80",
					"81-90", "91-100" };
			for (String name : sdkNames) {
				BiometricScores fingerBiometricScores = new BiometricScores();
				fingerBiometricScores.setSdkName(name);
				List<FingerBioScoresTable> fingerBioScoresTables = new ArrayList<FingerBioScoresTable>();
				int childAgeGroupIndex = 0;
				for (String ageGroup : ageGroups) {
					FingerBioScoresTable table = new FingerBioScoresTable();
					table.setAgeGroup(ageGroup);
					List<FingerBioScoresRow> rows = new ArrayList<FingerBioScoresRow>();
					if (childAgeGroupIndex == 0) { // child age group
						logQuery(name, ageGroup, null);
						List<BiometricScoresSummaryEntity> childScores = biometricScoresSummaryRepository
								.getBiometricScoresForChildFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup);
						rows = populateFingerBioScoresRows(scoreRanges, childScores, null, null);
						table.setChildAgeGroup(true);
					} else { // other non child age groups
						logQuery(name, ageGroup, occupations[0]);
						List<BiometricScoresSummaryEntity> labourerScores = biometricScoresSummaryRepository
								.getBiometricScoresForFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup, occupations[0]);
						logQuery(name, ageGroup, occupations[1]);
						List<BiometricScoresSummaryEntity> nonLabourerScores = biometricScoresSummaryRepository
								.getBiometricScoresForFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup, occupations[1]);
						rows = populateFingerBioScoresRows(scoreRanges, null, labourerScores, nonLabourerScores);
						table.setChildAgeGroup(false);
					}
					table.setRows(rows);
					fingerBioScoresTables.add(table);
					childAgeGroupIndex++;
				}
				fingerBiometricScores.setFingerTables(fingerBioScoresTables);
				biometricScoresList.add(fingerBiometricScores);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addBiometricScores method of BiometricScoresService Service - " + ex.getMessage());
			throw ex;
		}
		return biometricScoresList;
	}

	private void logQuery(String name, String ageGroup, String occupation) {
		log.info("fetching bio scores for sdk: " + name);
		log.info("fetching bio scores for ageGroup: " + ageGroup);
		if (occupation == null) {
			log.info("fetching bio scores for occupation: null");
		} else {
			log.info("fetching bio scores for occupation: " + occupation);
		}
	}

	private List<FingerBioScoresRow> populateFingerBioScoresRows(String[] scoreRanges,
			List<BiometricScoresSummaryEntity> childScores, List<BiometricScoresSummaryEntity> labourerScores,
			List<BiometricScoresSummaryEntity> nonLabourerScores) {
		List<FingerBioScoresRow> scoresList = new ArrayList<FingerBioScoresRow>();
		for (String scoreRange : scoreRanges) {
			FingerBioScoresRow row = new FingerBioScoresRow();
			row.setBioScoreRange(scoreRange);
			if (childScores != null && childScores.size() > 0) {
				row.setMaleChildScore(getTotalScore(scoreRange, childScores, true));
				row.setFemaleChildScore(getTotalScore(scoreRange, childScores, false));
			}
			if (labourerScores != null && labourerScores.size() > 0) {
				row.setMaleLabourerScore(getTotalScore(scoreRange, labourerScores, true));
				row.setFemaleLabourerScore(getTotalScore(scoreRange, labourerScores, false));
			}
			if (nonLabourerScores != null && nonLabourerScores.size() > 0) {
				row.setMaleNonLabourerScore(getTotalScore(scoreRange, nonLabourerScores, true));
				row.setFemaleNonLabourerScore(getTotalScore(scoreRange, nonLabourerScores, false));
			}
			scoresList.add(row);
		}
		return scoresList;
	}

	private String getTotalScore(String scoreRange, List<BiometricScoresSummaryEntity> bioScores, boolean forMale) {
		int total = 0;
		for (BiometricScoresSummaryEntity bioScore : bioScores) {
			switch (scoreRange) {
			case "0-10":
				if (forMale) {
					total += bioScore.getMale_0_10();
				} else {
					total += bioScore.getFemale_0_10();
				}
				break;
			case "11-20":
				if (forMale) {
					total += bioScore.getMale_11_20();
				} else {
					total += bioScore.getFemale_11_20();
				}
				break;
			case "21-30":
				if (forMale) {
					total += bioScore.getMale_21_30();
				} else {
					total += bioScore.getFemale_21_30();
				}
				break;
			case "31-40":
				if (forMale) {
					total += bioScore.getMale_31_40();
				} else {
					total += bioScore.getFemale_31_40();
				}
				break;
			case "41-50":
				if (forMale) {
					total += bioScore.getMale_41_50();
				} else {
					total += bioScore.getFemale_41_50();
				}
				break;
			case "51-60":
				if (forMale) {
					total += bioScore.getMale_51_60();
				} else {
					total += bioScore.getFemale_51_60();
				}
				break;
			case "61-70":
				if (forMale) {
					total += bioScore.getMale_61_70();
				} else {
					total += bioScore.getFemale_61_70();
				}
				break;
			case "71-80":
				if (forMale) {
					total += bioScore.getMale_71_80();
				} else {
					total += bioScore.getFemale_71_80();
				}
				break;
			case "81-90":
				if (forMale) {
					total += bioScore.getMale_81_90();
				} else {
					total += bioScore.getFemale_81_90();
				}
				break;
			case "91-100":
				if (forMale) {
					total += bioScore.getMale_91_100();
				} else {
					total += bioScore.getFemale_91_100();
				}
				break;
			default:
			}
		}
		if (!forMale) {
			log.info("scoreRange: " + scoreRange + ", female, total " + total);
		} else {
			log.info("scoreRange: " + scoreRange + ", male, total " + total);
		}
		return String.valueOf(total);
	}

}
