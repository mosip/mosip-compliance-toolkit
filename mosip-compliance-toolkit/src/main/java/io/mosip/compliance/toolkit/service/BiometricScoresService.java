package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import io.mosip.compliance.toolkit.dto.report.BiometricScores.BiometricScoresTable;
import io.mosip.compliance.toolkit.dto.report.BiometricScores.BiometricScoresTable.BiometricScoresRow;
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

	String[] scoreRanges = new String[] { "0-10", "11-20", "21-30", "31-40", "41-50", "51-60", "61-70", "71-80",
			"81-90", "91-100" };

	@Value("#{'${mosip.toolkit.quality.assessment.age.groups}'.split(',')}")
	private List<String> ageGroups;
	
	@Value("#{'${mosip.toolkit.quality.assessment.occupations}'.split(',')}")
	private List<String> occupations;

	@Value("#{'${mosip.toolkit.quality.assessment.races}'.split(',')}")
	private List<String> races;

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
			for (String name : sdkNames) {
				BiometricScores biometricScores = new BiometricScores();
				biometricScores.setSdkName(name);
				List<BiometricScoresTable> tables = new ArrayList<BiometricScoresTable>();
				int childAgeGroupIndex = 0;
				for (String ageGroup : ageGroups) {
					BiometricScoresTable table = new BiometricScoresTable();
					table.setAgeGroup(ageGroup);
					List<BiometricScoresRow> rows = new ArrayList<BiometricScoresRow>();
					if (childAgeGroupIndex == 0) { // child age group
						logFingerQuery(name, ageGroup, null);
						List<BiometricScoresSummaryEntity> childScores = biometricScoresSummaryRepository
								.getBiometricScoresForChildFinger(partnerId, projectId, testRunId, name, biometricType,
										ageGroup);
						rows = populateBiometricScoresRows(scoreRanges, childScores, null);
						if (childScores != null && childScores.size() > 0) {
							if (biometricScores.getVersion() == null) {
								biometricScores.setVersion(getSdkVersion(childScores));
							}
						}
						table.setChildAgeGroup(true);
					} else { // other non child age groups
						Map<String, List<BiometricScoresSummaryEntity>> occupationsMap = new HashMap<String, List<BiometricScoresSummaryEntity>>();
						for (String occupation : occupations) {
							logFingerQuery(name, ageGroup, occupation);
							List<BiometricScoresSummaryEntity> occupationScores = biometricScoresSummaryRepository
									.getBiometricScoresForFinger(partnerId, projectId, testRunId, name, biometricType,
											ageGroup, occupation);
							occupationsMap.put(occupation, occupationScores);
							if (occupationScores != null && occupationScores.size() > 0) {
								if (biometricScores.getVersion() == null) {
									biometricScores.setVersion(getSdkVersion(occupationScores));
								}
							}
						}
						rows = populateBiometricScoresRows(scoreRanges, null, occupationsMap);
						table.setChildAgeGroup(false);
					}
					table.setRows(rows);
					tables.add(table);
					childAgeGroupIndex++;
				}
				biometricScores.setTables(tables);
				biometricScoresList.add(biometricScores);
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
			for (String name : sdkNames) {
				BiometricScores biometricScores = new BiometricScores();
				biometricScores.setSdkName(name);
				List<BiometricScoresTable> tables = new ArrayList<BiometricScoresTable>();
				for (String ageGroup : ageGroups) {
					BiometricScoresTable table = new BiometricScoresTable();
					table.setAgeGroup(ageGroup);
					List<BiometricScoresRow> rows = new ArrayList<BiometricScoresRow>();
					Map<String, List<BiometricScoresSummaryEntity>> racesMap = new HashMap<String, List<BiometricScoresSummaryEntity>>();
					for (String race : races) {
						logFaceQuery(name, ageGroup, race);
						List<BiometricScoresSummaryEntity> raceScores = biometricScoresSummaryRepository
								.getBiometricScoresForFace(partnerId, projectId, testRunId, name, biometricType,
										ageGroup, race);
						racesMap.put(race, raceScores);
						if (raceScores != null && raceScores.size() > 0) {
							if (biometricScores.getVersion() == null) {
								biometricScores.setVersion(getSdkVersion(raceScores));
							}
						}
					}
					rows = populateBiometricScoresRows(scoreRanges, null, racesMap);
					table.setRows(rows);
					tables.add(table);
				}
				biometricScores.setTables(tables);
				biometricScoresList.add(biometricScores);
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

			for (String name : sdkNames) {
				BiometricScores biometricScores = new BiometricScores();
				biometricScores.setSdkName(name);
				List<BiometricScoresTable> tables = new ArrayList<BiometricScoresTable>();
				BiometricScoresTable table = new BiometricScoresTable();
				List<BiometricScoresRow> rows = new ArrayList<BiometricScoresRow>();
				Map<String, List<BiometricScoresSummaryEntity>> ageGroupsMap = new HashMap<String, List<BiometricScoresSummaryEntity>>();
				for (String ageGroup : ageGroups) {
					table.setAgeGroup(ageGroup);
					logIrisQuery(name, ageGroup);
					List<BiometricScoresSummaryEntity> ageGroupScores = biometricScoresSummaryRepository
							.getBiometricScoresForIris(partnerId, projectId, testRunId, name, biometricType, ageGroup);
					if (ageGroupScores != null && ageGroupScores.size() > 0) {
						if (biometricScores.getVersion() == null) {
							biometricScores.setVersion(getSdkVersion(ageGroupScores));
						}
					}
					ageGroupsMap.put(ageGroup, ageGroupScores);
				}
				rows = populateBiometricScoresRows(scoreRanges, null, ageGroupsMap);
				table.setRows(rows);
				tables.add(table);
				biometricScores.setTables(tables);
				biometricScoresList.add(biometricScores);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getIrisBiometricScoresList method of BiometricScoresService Service - " + ex.getMessage());
			throw ex;
		}
		return biometricScoresList;
	}

	private String getSdkVersion(List<BiometricScoresSummaryEntity> biometricScoresSummaryEntityList) {
		if (biometricScoresSummaryEntityList != null && !biometricScoresSummaryEntityList.isEmpty()) {
			for (BiometricScoresSummaryEntity entity : biometricScoresSummaryEntityList) {
				if (entity.getVersion() != null) {
					return entity.getVersion();
				}
			}
		}
		return null;
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

	private String getSdkUrlsJsonString(String modality) throws JsonProcessingException, JsonMappingException {
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

	private List<BiometricScoresRow> populateBiometricScoresRows(String[] scoreRanges,
			List<BiometricScoresSummaryEntity> childScores, Map<String, List<BiometricScoresSummaryEntity>> scoresMap) {
		List<BiometricScoresRow> scoresList = new ArrayList<BiometricScoresRow>();
		for (String scoreRange : scoreRanges) {
			BiometricScoresRow row = new BiometricScoresRow();
			row.setBioScoreRange(scoreRange);
			if (childScores != null && childScores.size() > 0) {
				row.setMaleChildScore(getTotalScore(scoreRange, childScores, true));
				row.setFemaleChildScore(getTotalScore(scoreRange, childScores, false));
			}
			if (scoresMap != null) {
				for (Map.Entry<String, List<BiometricScoresSummaryEntity>> entry : scoresMap.entrySet()) {
					log.info("key: " + entry.getKey());
					String key = entry.getKey();
					List<BiometricScoresSummaryEntity> scores = entry.getValue();
					Map<String, String> maleScoreMap = null;
					Map<String, String> femaleScoreMap = null;
					if (row.getMaleScores() == null) {
						maleScoreMap = new HashMap<String, String>();
					} else {
						maleScoreMap = row.getMaleScores();
					}
					if (row.getFemaleScores() == null) {
						femaleScoreMap = new HashMap<String, String>();
					} else {
						femaleScoreMap = row.getFemaleScores();
					}
					maleScoreMap.put(key, getTotalScore(scoreRange, scores, true));
					femaleScoreMap.put(key, getTotalScore(scoreRange, scores, false));
					row.setMaleScores(maleScoreMap);
					row.setFemaleScores(femaleScoreMap);
				}
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
