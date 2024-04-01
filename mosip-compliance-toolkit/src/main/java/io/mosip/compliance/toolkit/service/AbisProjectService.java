package io.mosip.compliance.toolkit.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import io.mosip.compliance.toolkit.util.ProjectHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AbisSpecVersions;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.projects.AbisProjectDto;
import io.mosip.compliance.toolkit.entity.AbisProjectEntity;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.AbisProjectRepository;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RandomIdGenerator;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
public class AbisProjectService {

	@Value("${mosip.toolkit.api.id.abis.project.get}")
	private String getAbisProjectId;
	@Value("${mosip.toolkit.api.id.abis.project.post}")
	private String getAbisProjectPostId;
	@Value("${mosip.toolkit.api.id.abis.project.put}")
	private String putAbisProjectId;

	@Autowired
	private AbisProjectRepository abisProjectRepository;

	@Autowired
	private BiometricTestDataRepository biometricTestDataRepository;

	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Autowired
	private CollectionsService collectionsService;

	@Autowired
	private ResourceCacheService resourceCacheService;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@Autowired
	private ProjectHelper projectHelper;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;

	private Logger log = LoggerConfiguration.logConfig(AbisProjectService.class);

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

	public ResponseWrapper<AbisProjectDto> getAbisProject(String id) {
		ResponseWrapper<AbisProjectDto> responseWrapper = new ResponseWrapper<>();
		AbisProjectDto abisProjectDto = null;
		try {
			Optional<AbisProjectEntity> optionalProjectEntity = abisProjectRepository.findById(id, getPartnerId());
			if (optionalProjectEntity.isPresent()) {
				AbisProjectEntity abisProjectEntity = optionalProjectEntity.get();

				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.registerModule(new JavaTimeModule());
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				abisProjectDto = objectMapper.convertValue(abisProjectEntity, AbisProjectDto.class);
				String username = abisProjectEntity.getUsername();
				String password = abisProjectEntity.getPassword();
				if (Objects.nonNull(username) && !username.isEmpty()) {
					String decodedUsername = new String(Base64.getDecoder().decode(username));
					abisProjectDto.setUsername(decodedUsername);
				}
				if (Objects.nonNull(password) && !password.isEmpty()) {
					String decodedPassword = new String(Base64.getDecoder().decode(password));
					abisProjectDto.setPassword(decodedPassword);
				}

			} else {
				String errorCode = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorCode();
				String errorMessage = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorMessage();
				responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorCode();
			String errorMessage = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getAbisProjectId);
		responseWrapper.setResponse(abisProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<AbisProjectDto> addAbisProject(AbisProjectDto abisProjectDto) {
		ResponseWrapper<AbisProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if (validInputRequest(abisProjectDto, true)) {
				if (isValidAbisProject(abisProjectDto)) {
					boolean isValidTestFile = false;
					String partnerId = this.getPartnerId();
					if (Objects.isNull(abisProjectDto.getBioTestDataFileName())) {
						isValidTestFile = false;
					} else if (abisProjectDto.getBioTestDataFileName().equals(AppConstants.MOSIP_DEFAULT)) {
						isValidTestFile = true;
					} else {
						BiometricTestDataEntity biometricTestData = biometricTestDataRepository
								.findByTestDataName(abisProjectDto.getBioTestDataFileName(), partnerId);
						String fileName = biometricTestData.getFileId();
						String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId;
						if (objectStore.exists(objectStoreAccountName, container, null, null, fileName)) {
							isValidTestFile = true;
						}
					}

					if (isValidTestFile) {
						LocalDateTime crDate = LocalDateTime.now();
						AbisProjectEntity entity = new AbisProjectEntity();
						entity.setId(RandomIdGenerator.generateUUID(abisProjectDto.getProjectType().toLowerCase(), "", 36));
						entity.setName(abisProjectDto.getName());
						entity.setProjectType(abisProjectDto.getProjectType());
						entity.setUrl(abisProjectDto.getUrl());
						entity.setUsername(Base64.getEncoder().encodeToString(abisProjectDto.getUsername().getBytes()));
						entity.setPassword(Base64.getEncoder().encodeToString(abisProjectDto.getPassword().getBytes()));
						entity.setInboundQueueName(abisProjectDto.getInboundQueueName());
						entity.setPartnerId(partnerId);
						entity.setOrgName(resourceCacheService.getOrgName(partnerId));
						entity.setCrBy(this.getUserBy());
						entity.setCrDate(crDate);
						entity.setDeleted(false);
						entity.setOutboundQueueName(abisProjectDto.getOutboundQueueName());
						entity.setModality(abisProjectDto.getModality());
						entity.setAbisHash(abisProjectDto.getAbisHash());
						entity.setWebsiteUrl(abisProjectDto.getWebsiteUrl());
						entity.setBioTestDataFileName(abisProjectDto.getBioTestDataFileName());
						entity.setAbisVersion(abisProjectDto.getAbisVersion());

						AbisProjectEntity outputEntity = abisProjectRepository.save(entity);
						// Add a default "ALL" collection for the newly created project
						collectionsService.addDefaultCollection(AppConstants.COMPLIANCE_COLLECTION, null, null,
								abisProjectDto, entity.getId());

						abisProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity, AbisProjectDto.class);
						abisProjectDto.setId(entity.getId());
						abisProjectDto.setPartnerId(entity.getPartnerId());
						abisProjectDto.setOrgName(entity.getOrgName());
						abisProjectDto.setCrBy(entity.getCrBy());
						abisProjectDto.setCrDate(entity.getCrDate());
					} else {
						String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
						String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage();
						responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
					}
				}
			}
		} catch (ToolkitException ex) {
			abisProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		} catch (DataIntegrityViolationException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addSAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorCode();
			String errorMessage = ToolkitErrorCodes.PROJECT_NAME_EXISTS.getErrorMessage() + " "
					+ abisProjectDto.getName();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		} catch (Exception ex) {
			abisProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.ABIS_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.ABIS_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(getAbisProjectPostId);
		responseWrapper.setResponse(abisProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<AbisProjectDto> updateAbisProject(AbisProjectDto abisProjectDto) {
		ResponseWrapper<AbisProjectDto> responseWrapper = new ResponseWrapper<>();
		try {
			if (Objects.nonNull(abisProjectDto)) {
				String projectId = abisProjectDto.getId();
				String partnerId = this.getPartnerId();
				if (validInputRequest(abisProjectDto, false)) {
					Optional<AbisProjectEntity> optionalAbisProjectEntity = abisProjectRepository.findById(projectId,
							getPartnerId());
					if (optionalAbisProjectEntity.isPresent()) {
						AbisProjectEntity entity = optionalAbisProjectEntity.get();
						LocalDateTime updDate = LocalDateTime.now();
						String url = abisProjectDto.getUrl();
						String userName = abisProjectDto.getUsername();
						String password = abisProjectDto.getPassword();
						String requestQueueName = abisProjectDto.getOutboundQueueName();
						String responseQueueName = abisProjectDto.getInboundQueueName();
						String abisHash = abisProjectDto.getAbisHash();
						String websiteUrl = abisProjectDto.getWebsiteUrl();
						String bioTestDataName = abisProjectDto.getBioTestDataFileName();
						// Updating ABIS project values
						if (Objects.nonNull(url) && !url.isEmpty()) {
							entity.setUrl(url);
						}
						if (Objects.nonNull(userName) && !userName.isEmpty()) {
							entity.setUsername(Base64.getEncoder().encodeToString(userName.getBytes()));
						}
						if (Objects.nonNull(password) && !password.isEmpty()) {
							entity.setPassword(Base64.getEncoder().encodeToString(password.getBytes()));
						}
						if (Objects.nonNull(requestQueueName) && !requestQueueName.isEmpty()) {
							entity.setOutboundQueueName(requestQueueName);
						}
						if (Objects.nonNull(responseQueueName) && !responseQueueName.isEmpty()) {
							entity.setInboundQueueName(responseQueueName);
						}
						if (Objects.nonNull(abisHash) && !abisHash.isEmpty() && !entity.getAbisHash().equals(abisHash)) {
							boolean canHashBeUpdated = projectHelper.checkIfHashCanBeUpdated(projectId, abisProjectDto.getProjectType(), partnerId);
							if (canHashBeUpdated) {
								entity.setAbisHash(abisHash);
							}
						}
						if (Objects.nonNull(websiteUrl) && !websiteUrl.isEmpty() && entity.getWebsiteUrl().equals("To_Be_Added")) {
							entity.setWebsiteUrl(websiteUrl);
						}
						if (Objects.nonNull(bioTestDataName) && !bioTestDataName.isEmpty()) {
							if (bioTestDataName.equals(AppConstants.MOSIP_DEFAULT)) {
								entity.setBioTestDataFileName(AppConstants.MOSIP_DEFAULT);
							} else {
								BiometricTestDataEntity biometricTestData = biometricTestDataRepository
										.findByTestDataName(bioTestDataName, partnerId);
								String fileName = biometricTestData.getFileId();
								String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/"
										+ AppConstants.ABIS;
								if (objectStore.exists(objectStoreAccountName, container, null, null, fileName)) {
									entity.setBioTestDataFileName(bioTestDataName);
								} else {
									String errorCode = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode();
									String errorMessage = ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE
											.getErrorMessage();
									responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
								}
							}
						}
						entity.setUpBy(this.getUserBy());
						entity.setUpdDate(updDate);
						AbisProjectEntity outputEntity = abisProjectRepository.save(entity);
						abisProjectDto = objectMapperConfig.objectMapper().convertValue(outputEntity, AbisProjectDto.class);
					} else {
						String errorCode = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorCode();
						String errorMessage = ToolkitErrorCodes.ABIS_PROJECT_NOT_AVAILABLE.getErrorMessage();
						responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
					}
				}
			}
		} catch (ToolkitException ex) {
			abisProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ex.getErrorCode();
			String errorMessage = ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		} catch (Exception ex) {
			abisProjectDto = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In updateAbisProject method of AbisProjectService Service - " + ex.getMessage());
			String errorCode = ToolkitErrorCodes.ABIS_PROJECT_UNABLE_TO_ADD.getErrorCode();
			String errorMessage = ToolkitErrorCodes.ABIS_PROJECT_UNABLE_TO_ADD.getErrorMessage() + " "
					+ ex.getMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode, errorMessage));
		}
		responseWrapper.setId(putAbisProjectId);
		responseWrapper.setResponse(abisProjectDto);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private boolean validInputRequest(AbisProjectDto abisProjectDto, boolean isAddProject) {
		if (isAddProject) {
			String projectName = abisProjectDto.getName();
			if (!Pattern.matches(AppConstants.REGEX_PATTERN, projectName)) {
				String exceptionErrorCode = ToolkitErrorCodes.INVALID_CHARACTERS.getErrorCode()
						+ AppConstants.COMMA_SEPARATOR
						+ ToolkitErrorCodes.PROJECT_NAME.getErrorCode();
				throw new ToolkitException(exceptionErrorCode, "Invalid characters are not allowed in project name");
			}
			String websiteUrl = abisProjectDto.getWebsiteUrl();
			if (!Pattern.matches(AppConstants.URL_REGEX_PATTERN, websiteUrl)) {
				String exceptionErrorCode = ToolkitErrorCodes.INVALID_URL.getErrorCode()
						+ AppConstants.COMMA_SEPARATOR
						+ ToolkitErrorCodes.WEBSITE_URL.getErrorCode();
				throw new ToolkitException(exceptionErrorCode, "Website URL will only allow http/https format");
			}
		}
		String abisUrl = abisProjectDto.getUrl();
		if (!Pattern.matches(AppConstants.ABIS_URL_REGEX_PATTERN, abisUrl)) {
			String errorCode = ToolkitErrorCodes.INVALID_URL.getErrorCode()
					+ AppConstants.COMMA_SEPARATOR
					+ ToolkitErrorCodes.ACTIVE_MQ_URL.getErrorCode();
			throw new ToolkitException(errorCode, "Active MQ URL will only allow wss/ws format");
		}
		return true;
	}

	/**
	 * Verifies all values of AbisProjectDto. validates ProjectType, SpecVersion
	 *
	 * @param AbisProjectDto
	 * @return boolean
	 */
	private boolean isValidAbisProject(AbisProjectDto abisProjectDto) throws ToolkitException {
		ToolkitErrorCodes errorCode = null;

		ProjectTypes projectTypesCode = ProjectTypes.fromCode(abisProjectDto.getProjectType());
		AbisSpecVersions specVersionCode = AbisSpecVersions.fromCode(abisProjectDto.getAbisVersion());

		switch (projectTypesCode) {
		case ABIS:
			switch (specVersionCode) {
			case SPEC_VER_0_9_0:
				break;
			default:
				errorCode = ToolkitErrorCodes.INVALID_ABIS_SPEC_VERSION;
				throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
			}
			break;
		default:
			errorCode = ToolkitErrorCodes.INVALID_PROJECT_TYPE;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}
		return true;
	}
}
