package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.report.BiometricScores;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FaceBioScoresTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FaceBioScoresTable.FaceBioScoresRow;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FingerBioScoresTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.FingerBioScoresTable.FingerBioScoresRow;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.IrisBioScoresTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.IrisBioScoresTable.IrisBioScoresRow;
import io.mosip.compliance.toolkit.entity.BiometricScoresEntity;
import io.mosip.compliance.toolkit.entity.BiometricScoresSummaryEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricScoresRepository;
import io.mosip.compliance.toolkit.repository.BiometricScoresSummaryRepository;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
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

	@Value("${mosip.toolkit.sbi.qualitycheck.finger.sdk.urls}")
	private String fingerSdkUrlsJsonStr;

	@Value("${mosip.toolkit.sbi.qualitycheck.face.sdk.urls}")
	private String faceSdkUrlsJsonStr;

	@Value("${mosip.toolkit.sbi.qualitycheck.iris.sdk.urls}")
	private String irisSdkUrlsJsonStr;

	@Autowired
	ObjectMapperConfig objectMapperConfig;

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
			List<String> sdkNames = getSdkNames(AppConstants.BIOMETRIC_SCORES_FINGER);
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
						logFingerQuery(name, ageGroup, null);
						List<BiometricScoresSummaryEntity> childScores = biometricScoresSummaryRepository
								.getBiometricScoresForChildFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup);
						rows = populateFingerBioScoresRows(scoreRanges, childScores, null, null);
						table.setChildAgeGroup(true);
					} else { // other non child age groups
						logFingerQuery(name, ageGroup, occupations[0]);
						List<BiometricScoresSummaryEntity> labourerScores = biometricScoresSummaryRepository
								.getBiometricScoresForFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup, occupations[0]);
						logFingerQuery(name, ageGroup, occupations[1]);
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
					"In getFingerBiometricScoresList method of BiometricScoresService Service - " + ex.getMessage());
			throw ex;
		}
		return biometricScoresList;
	}

	public List<BiometricScores> getFaceBiometricScoresList(String partnerId, String projectId, String testRunId)
			throws Exception {
		List<BiometricScores> biometricScoresList = new ArrayList<BiometricScores>();
		try {
			String biometricType = AppConstants.BIOMETRIC_SCORES_FACE;
			List<String> sdkNames = getSdkNames(AppConstants.BIOMETRIC_SCORES_FACE);
			String[] ageGroups = new String[]{"child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)"};
			String[] race = new String[]{"asian", "african", "european"};
			String[] scoreRanges = new String[]{"0-10", "11-20", "21-30", "31-40", "41-50", "51-60", "61-70", "71-80",
					"81-90", "91-100"};
			for (String name : sdkNames) {
				BiometricScores faceBiometricScores = new BiometricScores();
				faceBiometricScores.setSdkName(name);
				List<FaceBioScoresTable> faceBioScoresTables = new ArrayList<FaceBioScoresTable>();
				for (String ageGroup : ageGroups) {
					FaceBioScoresTable table = new FaceBioScoresTable();
					table.setAgeGroup(ageGroup);
					List<FaceBioScoresRow> rows = new ArrayList<FaceBioScoresRow>();
					logFaceQuery(name, ageGroup, race[0]);
					List<BiometricScoresSummaryEntity> asianScores = biometricScoresSummaryRepository
							.getBiometricScoresForFace(partnerId, projectId, testRunId, name, biometricType,
									ageGroup, race[0]);
					logFaceQuery(name, ageGroup, race[1]);
					List<BiometricScoresSummaryEntity> africanScores = biometricScoresSummaryRepository
							.getBiometricScoresForFace(partnerId, projectId, testRunId, name, biometricType,
									ageGroup, race[1]);
					logFaceQuery(name, ageGroup, race[2]);
					List<BiometricScoresSummaryEntity> europeanScores = biometricScoresSummaryRepository
							.getBiometricScoresForFace(partnerId, projectId, testRunId, name, biometricType,
									ageGroup, race[2]);
					rows = populateFaceBioScoresRows(scoreRanges, asianScores, africanScores, europeanScores);

					table.setRows(rows);
					faceBioScoresTables.add(table);
				}
				faceBiometricScores.setFaceTables(faceBioScoresTables);
				biometricScoresList.add(faceBiometricScores);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getFaceBiometricScoresList method of BiometricScoresService Service - " + ex.getMessage());
			throw ex;
		}
		return biometricScoresList;
	}

	public List<BiometricScores> getIrisBiometricScoresList(String partnerId, String projectId, String testRunId)
			throws Exception {
		List<BiometricScores> biometricScoresList = new ArrayList<BiometricScores>();
		try {
			String biometricType = AppConstants.BIOMETRIC_SCORES_IRIS;
			List<String> sdkNames = getSdkNames(AppConstants.BIOMETRIC_SCORES_IRIS);
			String[] ageGroups = new String[]{"child(5-12)", "adult(12-40)", "mature(40-59)", "senior(60+)"};
			String[] scoreRanges = new String[]{"0-10", "11-20", "21-30", "31-40", "41-50", "51-60", "61-70", "71-80",
					"81-90", "91-100"};
			for (String name : sdkNames) {
				BiometricScores irisBiometricScores = new BiometricScores();
				irisBiometricScores.setSdkName(name);

				IrisBioScoresTable irisBioScoresTable = new IrisBioScoresTable();
				List<IrisBioScoresRow> rows = new ArrayList<IrisBioScoresRow>();
				logIrisQuery(name, ageGroups[0]);
				List<BiometricScoresSummaryEntity> childScores = biometricScoresSummaryRepository
						.getBiometricScoresForIris(partnerId, projectId, testRunId, name, biometricType,
								ageGroups[0]);
				logIrisQuery(name, ageGroups[1]);
				List<BiometricScoresSummaryEntity> adultScores = biometricScoresSummaryRepository
						.getBiometricScoresForIris(partnerId, projectId, testRunId, name, biometricType,
								ageGroups[1]);
				logIrisQuery(name, ageGroups[2]);
				List<BiometricScoresSummaryEntity> matureScores = biometricScoresSummaryRepository
						.getBiometricScoresForIris(partnerId, projectId, testRunId, name, biometricType,
								ageGroups[2]);
				logIrisQuery(name, ageGroups[3]);
				List<BiometricScoresSummaryEntity> seniorScores = biometricScoresSummaryRepository
						.getBiometricScoresForIris(partnerId, projectId, testRunId, name, biometricType,
								ageGroups[3]);
				rows = populateIrisBioScoresRows(scoreRanges, childScores, adultScores, matureScores, seniorScores);
				irisBioScoresTable.setRows(rows);

				irisBiometricScores.setIrisTable(irisBioScoresTable);
				biometricScoresList.add(irisBiometricScores);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getIrisBiometricScoresList method of BiometricScoresService Service - " + ex.getMessage());
			throw ex;
		}
		return biometricScoresList;
	}

	private List<String> getSdkNames(String modality) throws JsonProcessingException {
		List<String> sdkNames = new ArrayList<>();
		sdkNames.add(AppConstants.SBI);
		String sdkUrls = getSdkUrlsJsonString(modality);
		ArrayNode sdkUrlsArr = (ArrayNode) objectMapperConfig.objectMapper().readValue(sdkUrls, ArrayNode.class);
		for (JsonNode item : sdkUrlsArr) {
			String sdkName = item.get("name").asText();
			sdkNames.add(sdkName);
		}
		return sdkNames;
	}

	private String getSdkUrlsJsonString(String modality)
			throws JsonProcessingException, JsonMappingException {
		String sdkUrlsJsonStr = "";
		if (AppConstants.BIOMETRIC_SCORES_FACE.equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = faceSdkUrlsJsonStr;
		} else if (AppConstants.BIOMETRIC_SCORES_FINGER.equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = fingerSdkUrlsJsonStr;
		} else if (AppConstants.BIOMETRIC_SCORES_IRIS.equalsIgnoreCase(modality)) {
			sdkUrlsJsonStr = irisSdkUrlsJsonStr;
		} else {
			throw new ToolkitException(ToolkitErrorCodes.INVALID_MODALITY.getErrorCode(),
					ToolkitErrorCodes.INVALID_MODALITY.getErrorMessage());
		}
		return sdkUrlsJsonStr;
	}


	private void logFingerQuery(String name, String ageGroup, String occupation) {
		log.info("fetching bio scores for sdk: " + name);
		log.info("fetching bio scores for ageGroup: " + ageGroup);
		if (occupation == null) {
			log.info("fetching bio scores for occupation: null");
		} else {
			log.info("fetching bio scores for occupation: " + occupation);
		}
	}

	private void logFaceQuery(String name, String ageGroup, String race) {
		log.info("fetching bio scores for sdk: " + name);
		log.info("fetching bio scores for ageGroup: " + ageGroup);
		log.info("fetching bio scores for race: " + race);
	}

	private void logIrisQuery(String name, String ageGroup) {
		log.info("fetching bio scores for sdk: " + name);
		log.info("fetching bio scores for ageGroup: " + ageGroup);
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

	private List<FaceBioScoresRow> populateFaceBioScoresRows(String[] scoreRanges,
			List<BiometricScoresSummaryEntity> asianScores, List<BiometricScoresSummaryEntity> africanScores,
			List<BiometricScoresSummaryEntity> europeanScores) {
		List<FaceBioScoresRow> scoresList = new ArrayList<FaceBioScoresRow>();
		for (String scoreRange : scoreRanges) {
			FaceBioScoresRow row = new FaceBioScoresRow();
			row.setBioScoreRange(scoreRange);
			if (asianScores != null && asianScores.size() > 0) {
				row.setMaleAsianScore(getTotalScore(scoreRange, asianScores, true));
				row.setFemaleAsianScore(getTotalScore(scoreRange, asianScores, false));
			}
			if (africanScores != null && africanScores.size() > 0) {
				row.setMaleAfricanScore(getTotalScore(scoreRange, africanScores, true));
				row.setFemaleAfricanScore(getTotalScore(scoreRange, africanScores, false));
			}
			if (europeanScores != null && europeanScores.size() > 0) {
				row.setMaleEuropeanScore(getTotalScore(scoreRange, europeanScores, true));
				row.setFemaleEuropeanScore(getTotalScore(scoreRange, europeanScores, false));
			}
			scoresList.add(row);
		}
		return scoresList;
	}

	private List<IrisBioScoresRow> populateIrisBioScoresRows(String[] scoreRanges, List<BiometricScoresSummaryEntity> childScores,
		    List<BiometricScoresSummaryEntity> adultScores, List<BiometricScoresSummaryEntity> matureScores,
			List<BiometricScoresSummaryEntity> seniorScores) {
		List<IrisBioScoresRow> scoresList = new ArrayList<IrisBioScoresRow>();
		for (String scoreRange : scoreRanges) {
			IrisBioScoresRow row = new IrisBioScoresRow();
			row.setBioScoreRange(scoreRange);
			if (childScores != null && childScores.size() > 0) {
				row.setChildScore(getTotalScore(scoreRange, childScores, true));
			}
			if (adultScores != null && adultScores.size() > 0) {
				row.setAdultScore(getTotalScore(scoreRange, adultScores, true));
			}
			if (matureScores != null && matureScores.size() > 0) {
				row.setMatureScore(getTotalScore(scoreRange, matureScores, true));
			}
			if (seniorScores != null && seniorScores.size() > 0) {
				row.setSeniorScore(getTotalScore(scoreRange, seniorScores, true));
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
