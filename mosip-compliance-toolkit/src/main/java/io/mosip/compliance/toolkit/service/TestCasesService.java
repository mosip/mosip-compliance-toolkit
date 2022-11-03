package io.mosip.compliance.toolkit.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.DeviceSubTypes;
import io.mosip.compliance.toolkit.constants.DeviceTypes;
import io.mosip.compliance.toolkit.constants.MethodName;
import io.mosip.compliance.toolkit.constants.ProjectTypes;
import io.mosip.compliance.toolkit.constants.Purposes;
import io.mosip.compliance.toolkit.constants.SbiSpecVersions;
import io.mosip.compliance.toolkit.constants.SdkPurpose;
import io.mosip.compliance.toolkit.constants.SdkSpecVersions;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.GenerateSdkRequestResponseDto;
import io.mosip.compliance.toolkit.dto.sdk.CheckQualityRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.ConvertFormatRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.ExtractTemplateRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.MatchRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.RequestDto;
import io.mosip.compliance.toolkit.dto.sdk.SegmentRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.SdkRequestDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidateRequestSchemaDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidatorDefDto;
import io.mosip.compliance.toolkit.entity.BiometricTestDataEntity;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.BiometricTestDataRepository;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;
import io.mosip.compliance.toolkit.util.CryptoUtil;
import io.mosip.compliance.toolkit.validators.BaseValidator;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class TestCasesService {

	@Value("${mosip.toolkit.api.id.projects.get}")
	private String getProjectsId;

	@Value("${mosip.toolkit.api.id.sdk.generate.request}")
	private String generateSdkRequest;

	@Value("${mosip.toolkit.api.id.validations.post}")
	private String validationsId;

	@Value("${mosip.toolkit.api.id.testcase.project.get}")
	private String getTestCasesId;

	@Value("$(mosip.toolkit.api.id.testcase.get)")
	private String getTestCaseId;

	@Value("${mosip.toolkit.max.allowed.gallery.files}")
	private String maxAllowedGalleryFiles;
	
	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	private ApplicationContext context;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TestCasesRepository testCasesRepository;
	
	@Autowired
	TestCaseCacheService testCaseCacheService;
	
	@Autowired
	BiometricTestDataRepository biometricTestDataRepository;
	
	@Value("${mosip.kernel.objectstore.account-name}")
	private String objectStoreAccountName;

	@Qualifier("S3Adapter")
	@Autowired
	private ObjectStoreAdapter objectStore;
	
	@Autowired
	ResourceCacheService resourceCacheService;

	private CbeffUtil cbeffReader = new CbeffImpl();

	Gson gson = new GsonBuilder().create();

	private static final String VERSION = "1.0";

	private Logger log = LoggerConfiguration.logConfig(ProjectsService.class);

	private AuthUserDetails authUserDetails() {
		return (AuthUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	private String getPartnerId() {
		String partnerId = authUserDetails().getUsername();
		return partnerId;
	}

	public ResponseWrapper<List<TestCaseDto>> getSbiTestCases(String specVersion, String purpose, String deviceType,
			String deviceSubType) {
		ResponseWrapper<List<TestCaseDto>> responseWrapper = new ResponseWrapper<>();
		List<TestCaseDto> testCases = new ArrayList<>();
		List<ServiceError> serviceErrorsList = new ArrayList<>();
		ServiceError serviceError = null;
		try {
			String testCaseSchemaJson = this.getSchemaJson(null, null,"testcase_schema.json");
			if (isValidSbiTestCase(specVersion, purpose, deviceType, deviceSubType)) {
				List<TestCaseEntity> testCaseEntities = testCaseCacheService.getSbiTestCases(AppConstants.SBI, specVersion);//testCasesRepository.findAllSbiTestCaseBySpecVersion(specVersion);
				for (final TestCaseEntity testCaseEntity : testCaseEntities) {
					String testcaseJson = testCaseEntity.getTestcaseJson();
					if (AppConstants.SUCCESS
							.equals(this.validateJsonWithSchema(testcaseJson, testCaseSchemaJson).getStatus())) {
						TestCaseDto testCaseDto = objectMapper.readValue(testcaseJson, TestCaseDto.class);
						if (!testCaseDto.isInactive() && testCaseDto.getSpecVersion() != null && testCaseDto.getSpecVersion().equals(specVersion)
								&& testCaseDto.getOtherAttributes().getPurpose().contains(purpose)
								&& testCaseDto.getOtherAttributes().getBiometricTypes().contains(deviceType)
								&& testCaseDto.getOtherAttributes().getDeviceSubTypes().contains(deviceSubType)) {
							testCases.add(testCaseDto);
						}
					}
				}
			}
		} catch (ToolkitException ex) {
			testCases = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiTestCases method of TestCasesService - " + ex.getMessage());
			serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			testCases = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSbiTestCases method of TestCasesService - " + ex.getMessage());
			serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestCasesId);
		responseWrapper.setResponse(testCases);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	/**
	 * Verifies test case is valid. validates specVersion, purpose, deviceType,
	 * deviceSubType values
	 *
	 * @param specVersion, purpose, deviceType, deviceSubType
	 * @return boolean
	 */
	private boolean isValidSbiTestCase(String specVersion, String purpose, String deviceType, String deviceSubType)
			throws ToolkitException {
		SbiSpecVersions.fromCode(specVersion);
		Purposes.fromCode(purpose);
		DeviceTypes.fromCode(deviceType);
		DeviceSubTypes.fromCode(deviceSubType);
		return true;
	}

	public ResponseWrapper<List<TestCaseDto>> getSdkTestCases(String specVersion, String sdkPurpose) {
		ResponseWrapper<List<TestCaseDto>> responseWrapper = new ResponseWrapper<>();
		List<TestCaseDto> testCases = new ArrayList<>();
		List<ServiceError> serviceErrorsList = new ArrayList<>();
		ServiceError serviceError = null;
		try {
			String testCaseSchemaJson = this.getSchemaJson(null, null, "testcase_schema.json");
			if (isValidSdkTestCase(specVersion, sdkPurpose)) {
				List<TestCaseEntity> testCaseEntities = testCaseCacheService.getSdkTestCases(AppConstants.SDK, specVersion);//testCasesRepository.findAllSdkTestCaseBySpecVersion(specVersion);
				for (final TestCaseEntity testCaseEntity : testCaseEntities) {
					String testcaseJson = testCaseEntity.getTestcaseJson();
					if (AppConstants.SUCCESS
							.equals(this.validateJsonWithSchema(testcaseJson, testCaseSchemaJson).getStatus())) {
						TestCaseDto testCaseDto = objectMapper.readValue(testcaseJson, TestCaseDto.class);
						if (!testCaseDto.isInactive() && testCaseDto.getSpecVersion() != null && testCaseDto.getSpecVersion().equals(specVersion)
								&& testCaseDto.getOtherAttributes().getSdkPurpose().contains(sdkPurpose)) {
							testCases.add(testCaseDto);
						}
					}
				}
			}
		} catch (ToolkitException ex) {
			testCases = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSdkTestCases method of TestCasesService - " + ex.getMessage());
			serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			testCases = null;
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getSdkTestCases method of TestCasesService - " + ex.getMessage());
			serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.GET_TEST_CASE_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestCasesId);
		responseWrapper.setResponse(testCases);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	/**
	 * Verifies test case is valid. validates specVersion, purpose, deviceType,
	 * deviceSubType values
	 *
	 * @param specVersion, purpose, deviceType, deviceSubType
	 * @return boolean
	 */
	private boolean isValidSdkTestCase(String specVersion, String sdkPurpose) throws ToolkitException {
		SdkSpecVersions.fromCode(specVersion);
		SdkPurpose.fromCode(sdkPurpose);
		return true;
	}

	/**
	 * Validates JSON against the schema.
	 *
	 * @param sourceJson
	 * @param schemaJson
	 * @return
	 * @throws Exception
	 */
	public ValidationResultDto validateJsonWithSchema(String sourceJson, String schemaJson) throws Exception {
		// create an instance of the JsonSchemaFactory using version flag
		JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
		// store the JSON data in InputStream
		try {
			// read data from the stream and store it into JsonNode
			JsonNode json = objectMapper.readTree(sourceJson);
			// get schema from the schemaStream and store it into JsonSchema
			JsonSchema schema = schemaFactory.getSchema(schemaJson);
			// create set of validation message and store result in it
			Set<ValidationMessage> validationResult = schema.validate(json);
			ValidationResultDto validationResultDto = new ValidationResultDto();
			// show the validation errors
			if (validationResult.isEmpty()) {
				// show custom message if there is no validation error
				log.info(
						"JSON is as expected. All mandatory values are available and they all have valid expected values.");
				validationResultDto.setDescription(
						"JSON is as expected. All mandatory values are available and they all have valid expected values.");
				validationResultDto.setStatus(AppConstants.SUCCESS);
				return validationResultDto;
			} else {
				List<String> errors = new ArrayList<>();
				// show all the validation error
				validationResult.forEach(vm -> errors.add(vm.getMessage()));
				log.debug("Schema validations failed.");
				validationResultDto.setDescription(errors.toString());
				validationResultDto.setStatus(AppConstants.FAILURE);
				return validationResultDto;
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public ResponseWrapper<TestCaseResponseDto> saveTestCases(List<TestCaseDto> values) throws Exception {
		ResponseWrapper<TestCaseResponseDto> responseWrapper = new ResponseWrapper<>();
		TestCaseResponseDto testCaseResponseDto = new TestCaseResponseDto();
		Map<String, String> savedValues = new HashMap<String, String>();

		try {
			String testCaseSchemaJson = this.getSchemaJson(null, null, "testcase_schema.json");
			for (TestCaseDto testCaseDto : values) {
				// Do JSON Schema Validation
				String jsonValue = objectMapper.writeValueAsString(testCaseDto);
				ValidationResultDto validationResultDto = this.validateJsonWithSchema(jsonValue, testCaseSchemaJson);
				if (AppConstants.SUCCESS.equals(validationResultDto.getStatus())) {
					// Get JSON Object
					// Do Validation on content of JSON
					if (isValidTestCaseId(testCaseDto) && validateArrayLengths(testCaseDto)) {
						String testCaseId = testCaseDto.getTestId();
						Optional<TestCaseEntity> checkTestCaseEntity = Optional.empty();
						checkTestCaseEntity = testCasesRepository.findById(testCaseId);

						// else if test case not present .. save
						if (checkTestCaseEntity.isEmpty() || !checkTestCaseEntity.isPresent()) {
							TestCaseEntity testCase = new TestCaseEntity();
							testCase.setId(testCaseId);
							testCase.setTestcaseJson(jsonValue);
							testCase.setTestcaseType(testCaseDto.getTestCaseType());
							testCase.setSpecVersion(testCaseDto.getSpecVersion());
							testCase = testCaseCacheService.saveTestCase(testCase);
						}
						// Check if test case present .. update
						else {
							TestCaseEntity testCase = checkTestCaseEntity.get();
							testCase.setTestcaseJson(jsonValue);
							testCase.setTestcaseType(testCaseDto.getTestCaseType());
							testCase.setSpecVersion(testCaseDto.getSpecVersion());
							testCase = testCaseCacheService.updateTestCase(testCase);
						}
						savedValues.put(testCaseId, jsonValue);
					}
				} else {
					ToolkitErrorCodes errorCode = null;
					errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
					throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage() + '-'
							+ testCaseDto.testId + " - " + validationResultDto.getDescription());
				}
			}
			testCaseResponseDto.setTestCases(savedValues);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In saveTestCases method of Test Cases Service - " + ex.getLocalizedMessage());

			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.SAVE_TEST_CASE_JSON_ERROR;
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(errorCode.getErrorCode());
			serviceError.setMessage(errorCode.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getProjectsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testCaseResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	public ResponseWrapper<ValidationResultDto> performRequestValidations(ValidateRequestSchemaDto requestDto) {
		ResponseWrapper<ValidationResultDto> responseWrapper = new ResponseWrapper<>();
		try {
			ValidationResultDto resultDto = null;
			String sourceJson = requestDto.getMethodRequest();
			String testCaseSchemaJson = null;
			if (requestDto.getTestCaseType().equalsIgnoreCase(AppConstants.SBI)) {
				testCaseSchemaJson = this.getSchemaJson(AppConstants.SBI.toLowerCase(), requestDto.getSpecVersion(),
						requestDto.getRequestSchema() + ".json");
			}
			if (requestDto.getTestCaseType().equalsIgnoreCase(AppConstants.SDK)) {
				testCaseSchemaJson = this.getSchemaJson(AppConstants.SDK.toLowerCase(), requestDto.getSpecVersion(),
						requestDto.getRequestSchema() + ".json");
			}
			resultDto = this.validateJsonWithSchema(sourceJson, testCaseSchemaJson);
			resultDto.setValidatorName("SchemaValidator");
			resultDto.setValidatorDescription("Validates the method request against the schema.");
			responseWrapper.setResponse(resultDto);
			return responseWrapper;
		} catch (Exception e) {
			ValidationResultDto validationResponseDto = new ValidationResultDto();
			validationResponseDto.setStatus(AppConstants.FAILURE);
			validationResponseDto.setDescription(e.getLocalizedMessage());
			responseWrapper.setResponse(validationResponseDto);
			return responseWrapper;
		}
	}

	public ResponseWrapper<ValidationResponseDto> performValidations(ValidationInputDto validationInputDto) {
		ResponseWrapper<ValidationResponseDto> responseWrapper = new ResponseWrapper<>();
		ValidationResponseDto validationResponseDto = new ValidationResponseDto();
		List<ValidationResultDto> validationResults = new ArrayList<ValidationResultDto>();
		try {
			List<ValidatorDefDto> validatorDefs = validationInputDto.getValidatorDefs();
			// first check all validator definitions
			// now perform validation for all
			validatorDefs.forEach(v -> {
				BaseValidator validator = null;
				ValidationResultDto resultDto = new ValidationResultDto();
				try {
					Class<?> className = Class.forName("io.mosip.compliance.toolkit.validators." + v.getName());
					log.debug("invloking validator: {}", className);
					validator = (BaseValidator) className.getDeclaredConstructor().newInstance();
					context.getAutowireCapableBeanFactory().autowireBean(validator);
					resultDto = validator.validateResponse(validationInputDto);
					resultDto.setValidatorName(v.getName());
					resultDto.setValidatorDescription(v.getDescription());
				} catch (Exception ex) {
					resultDto.setValidatorName(v.getName());
					resultDto.setValidatorDescription(v.getDescription());
					resultDto.setStatus(AppConstants.FAILURE);
					resultDto.setDescription(ToolkitErrorCodes.INVALID_VALIDATOR_DEF.getErrorCode() + " - "
							+ ToolkitErrorCodes.INVALID_VALIDATOR_DEF.getErrorMessage());
				}
				validationResults.add(resultDto);
			});
			validationResponseDto.setValidationsList(validationResults);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id",
					"In performValidations method of Test Cases Service - " + ex.getLocalizedMessage());

			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.TESTCASE_VALIDATION_ERR;
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(errorCode.getErrorCode());
			serviceError.setMessage(errorCode.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(validationsId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(validationResponseDto);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}


	/**
	 * Verifies test case is valid. validates testcaseid starts with typename
	 *
	 * @param TestCaseDto
	 * @return boolean
	 */
	private boolean isValidTestCaseId(TestCaseDto testCaseDto) throws ToolkitException {
		ToolkitErrorCodes errorCode = null;
		String type = testCaseDto.getTestCaseType();
		String testId = testCaseDto.getTestId();
		if (!testId.startsWith(type)) {
			errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_ID;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage());
		}

		return true;
	}

	private BiometricType getBiometricType(String type) {
		if (type.equalsIgnoreCase(AppConstants.FINGER)) {
			return BiometricType.FINGER;
		} else if (type.equalsIgnoreCase(AppConstants.FACE)) {
			return BiometricType.FACE;
		} else if (type.equalsIgnoreCase(AppConstants.IRIS)) {
			return BiometricType.IRIS;
		} else {
			throw new BaseUncheckedException("Invalid biometric type : " + type);
		}

	}

	private boolean validateArrayLengths(TestCaseDto testCaseDto) {
		if (testCaseDto.getMethodName().size() > 1
				&& !ProjectTypes.SDK.getCode().equals(testCaseDto.getTestCaseType())) {
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
			throw new ToolkitException(errorCode.getErrorCode(),
					errorCode.getErrorMessage() + " - the 'methodName' array length should be only 1.");
		}
		if (testCaseDto.getMethodName().size() > 2) {
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
			throw new ToolkitException(errorCode.getErrorCode(),
					errorCode.getErrorMessage() + " - the 'methodName' array length should not be more than 2.");
		}
		if (testCaseDto.getMethodName().size() != testCaseDto.getRequestSchema().size()) {
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage()
					+ " - the 'requestSchema' array length does not match the 'methodName'  array length.");
		}
		if (testCaseDto.getMethodName().size() != testCaseDto.getResponseSchema().size()) {
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage()
					+ " - the 'responseSchema' array length does not match the 'methodName'  array length.");
		}
		if (testCaseDto.getMethodName().size() != testCaseDto.getValidatorDefs().size()) {
			ToolkitErrorCodes errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
			throw new ToolkitException(errorCode.getErrorCode(), errorCode.getErrorMessage()
					+ " - the 'validatorDefs' array length does not match the 'methodName' array length.");
		}
		return true;
	}

	private String base64Encode(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

private String base64Decode(String data) {
		return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
	}
	
	public String getSchemaJson(String type, String version, String fileName) throws Exception {
		// Read File Content
		String schemaResponse = resourceCacheService.getSchema(type, version, fileName);
		if(Objects.nonNull(schemaResponse)) {
			return schemaResponse;
		}else {
			throw new ToolkitException(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode(),
					ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage());
		}
	}

	public ResponseWrapper<GenerateSdkRequestResponseDto> generateRequestForSDKTestcase(SdkRequestDto requestDto) throws Exception {
		ResponseWrapper<GenerateSdkRequestResponseDto> responseWrapper = new ResponseWrapper<>();
		try {
			GenerateSdkRequestResponseDto generateSdkRequestResponseDto = new GenerateSdkRequestResponseDto();
			String requestJson = null;
			InputStream objectStoreStream = null;
			byte[] probeFileBytes = null;
			if (Objects.nonNull(requestDto)) {
				if (requestDto.getMethodName().equalsIgnoreCase(MethodName.INIT.getCode())) {
					ObjectNode rootNode = objectMapper.createObjectNode();
					ObjectNode childNode = objectMapper.createObjectNode();
					rootNode.set("initParams", childNode);
					requestJson = gson.toJson(rootNode);
				} else {
					String partnerId = getPartnerId();
					SdkPurpose sdkPurpose = getSdkPurpose(requestDto.getMethodName());
					objectStoreStream = getPartnerTestDataStream(requestDto, partnerId, sdkPurpose);
					probeFileBytes = getProbeData(requestDto, objectStoreStream, sdkPurpose, requestDto.getTestcaseId());
					generateSdkRequestResponseDto.setTestDataSource(requestDto.bioTestDataName);
					if (Objects.isNull(objectStoreStream) || Objects.isNull(probeFileBytes)) {
						objectStoreStream = getDefaultTestDataStream(requestDto.getMethodName(), sdkPurpose);
						probeFileBytes = getProbeData(requestDto, objectStoreStream, sdkPurpose, requestDto.getTestcaseId());
						generateSdkRequestResponseDto.setTestDataSource(AppConstants.MOSIP_DEFAULT);
					}
					if (Objects.nonNull(objectStoreStream)) {
						if (Objects.nonNull(probeFileBytes)) {
							// get the BIRs from the XML
							List<io.mosip.kernel.biometrics.entities.BIR> birsForProbe = cbeffReader
									.getBIRDataFromXML(probeFileBytes);
							// convert BIRS to Biometric Record
							BiometricRecord biometricRecord = new BiometricRecord();
							biometricRecord.setSegments(birsForProbe);

							List<BiometricRecord> biometricRecordsArr = new ArrayList<BiometricRecord>();
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
								for (int i = 1; i <= Integer.parseInt(maxAllowedGalleryFiles); i++) {
									// TODO pass the orgname / partnerId
									byte[] galleryFileBytes = null;
									if (Objects.nonNull(objectStoreStream)) {
										objectStoreStream.reset();
										galleryFileBytes = this.getXmlDataFromZipFile(objectStoreStream,
												sdkPurpose.getCode(), requestDto.getTestcaseId(),
												"gallery" + i + ".xml");
									}
									if (galleryFileBytes != null) {
										// get the BIRs from the XML
										List<io.mosip.kernel.biometrics.entities.BIR> birsForGallery = cbeffReader
												.getBIRDataFromXML(galleryFileBytes);
										BiometricRecord biometricRecordGallery = new BiometricRecord();
										biometricRecordGallery.setSegments(birsForGallery);
										biometricRecordsArr.add(biometricRecordGallery);
									} else {
										break;
									}
								}
							}

							if (Objects.nonNull(objectStoreStream)) {
								objectStoreStream.close();
							}

							// get the Biometric types
							List<BiometricType> bioTypeList = requestDto.getModalities().stream()
									.map(bioType -> this.getBiometricType(bioType)).collect(Collectors.toList());

							// populate the request object based on the method name
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
								CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
								checkQualityRequestDto.setSample(biometricRecord);
								checkQualityRequestDto.setModalitiesToCheck(bioTypeList);
								// TODO: set flags
								checkQualityRequestDto.setFlags(null);
								requestJson = gson.toJson(checkQualityRequestDto);
							}
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.MATCH.getCode())) {
								MatchRequestDto matchRequestDto = new MatchRequestDto();
								matchRequestDto.setSample(biometricRecord);
								matchRequestDto.setGallery(
										(BiometricRecord[]) biometricRecordsArr.toArray(new BiometricRecord[0]));
								matchRequestDto.setModalitiesToMatch(bioTypeList);
								// TODO: set flags
								matchRequestDto.setFlags(null);
								requestJson = gson.toJson(matchRequestDto);
							}
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.EXTRACT_TEMPLATE.getCode())) {
								ExtractTemplateRequestDto extractTemplateRequestDto = new ExtractTemplateRequestDto();
								extractTemplateRequestDto.setSample(biometricRecord);
								extractTemplateRequestDto.setModalitiesToExtract(bioTypeList);
								// TODO: set flags
								extractTemplateRequestDto.setFlags(null);
								requestJson = gson.toJson(extractTemplateRequestDto);
							}
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.SEGMENT.getCode())) {
								SegmentRequestDto segmentRequestDto = new SegmentRequestDto();
								segmentRequestDto.setSample(biometricRecord);
								segmentRequestDto.setModalitiesToSegment(bioTypeList);
								// TODO: set flags
								segmentRequestDto.setFlags(null);
								requestJson = gson.toJson(segmentRequestDto);
							}
							if (requestDto.getMethodName().equalsIgnoreCase(MethodName.CONVERT_FORMAT.getCode())) {
								ConvertFormatRequestDto convertFormatRequestDto = new ConvertFormatRequestDto();
								convertFormatRequestDto.setSample(biometricRecord);
								convertFormatRequestDto.setSourceFormat(requestDto.getConvertSourceFormat());
								convertFormatRequestDto.setTargetFormat(requestDto.getConvertTargetFormat());
								convertFormatRequestDto.setSourceParams(null);
								convertFormatRequestDto.setTargetParams(null);
								convertFormatRequestDto.setModalitiesToConvert(bioTypeList);
								requestJson = gson.toJson(convertFormatRequestDto);
							}
						} else {
							List<ServiceError> serviceErrorsList = new ArrayList<>();
							ServiceError serviceError = new ServiceError();
							serviceError.setErrorCode(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorCode());
							serviceError.setMessage(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorMessage());
							serviceErrorsList.add(serviceError);
							responseWrapper.setErrors(serviceErrorsList);
						}
					} else {
						List<ServiceError> serviceErrorsList = new ArrayList<>();
						ServiceError serviceError = new ServiceError();
						serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode());
						serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage());
						serviceErrorsList.add(serviceError);
						responseWrapper.setErrors(serviceErrorsList);
					}

				}
				// convert the request json to base64encoded string
				if (requestJson != null) {
					RequestDto inputDto = new RequestDto();
					inputDto.setVersion(VERSION);
					inputDto.setRequest(this.base64Encode(requestJson));
					generateSdkRequestResponseDto.setGeneratedRequest(gson.toJson(inputDto));		
					responseWrapper.setResponse(generateSdkRequestResponseDto);
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (ToolkitException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In generateRequestForSDKTestcase method of TestCasesService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getErrorText());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In generateRequestForSDKTestcase method of TestCasesService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.GENERATE_SDK_REQUEST_ERROR.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.GENERATE_SDK_REQUEST_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(generateSdkRequest);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private byte[] getProbeData(SdkRequestDto requestDto, InputStream objectStoreStream, 
			SdkPurpose sdkPurpose, String testcaseId) throws IOException, Exception {
		byte[] probeFileBytes = null;
		if (Objects.nonNull(objectStoreStream)) {
			objectStoreStream.reset();
			probeFileBytes = this.getXmlDataFromZipFile(objectStoreStream, sdkPurpose.getCode(),
					testcaseId, "probe.xml");
		}
		return probeFileBytes;
	}

	private InputStream getPartnerTestDataStream(SdkRequestDto requestDto, String partnerId, SdkPurpose sdkPurpose)
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		InputStream objectStoreStream = null;
		if (Objects.nonNull(requestDto.getBioTestDataName())
				&& !requestDto.getBioTestDataName().equals(AppConstants.MOSIP_DEFAULT)) {
			BiometricTestDataEntity biometricTestData = biometricTestDataRepository
					.findByTestDataName(requestDto.getBioTestDataName(), partnerId);
			String zipFileName = biometricTestData.getFileId();
			String zipFileHash = biometricTestData.getFileHash();
			if (Objects.nonNull(zipFileName)) {
				String container = AppConstants.PARTNER_TESTDATA + "/" + partnerId + "/" + sdkPurpose.getCode();
				if (isObjectExistInObjectStore(container, zipFileName)) {
					objectStoreStream = getFromObjectStore(container, zipFileName);
					if (Objects.nonNull(objectStoreStream)) {
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						int nRead;
						byte[] data = new byte[16384];
						while ((nRead = objectStoreStream.read(data, 0, data.length)) != -1) {
							buffer.write(data, 0, nRead);
						}
						byte[] bytes = buffer.toByteArray();
						objectStoreStream.reset();
						String encodedHash = CryptoUtil.getEncodedHash(bytes);
						if (Objects.isNull(encodedHash) || !encodedHash.equals(zipFileHash)) {
							log.info("testdata " + zipFileName + " encoded file hash mismatch." + "\n"
									+ "stored hash : " + zipFileHash + "\n" + "calculated hash : " + encodedHash);
							objectStoreStream.close();
							objectStoreStream = null;
						}
					}
				}
			}
		}
		return objectStoreStream;
	}
	
	public ResponseWrapper<GenerateSdkRequestResponseDto> generateRequestForSDKFrmBirs(SdkRequestDto sdkRequestDto) throws Exception {
		ResponseWrapper<GenerateSdkRequestResponseDto> responseWrapper = new ResponseWrapper<>();
		try {
			GenerateSdkRequestResponseDto generateSdkRequestResponseDto = null;
			String[] methods = sdkRequestDto.getMethodName().split(",");
			String methodName1 = null;
			String methodName2 = null;
			if (methods.length == 2) {
				methodName1 = methods[0];
				methodName2 = methods[1];
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
			List<String> modalities = sdkRequestDto.getModalities();
			String decodedVal = this.base64Decode(sdkRequestDto.getBirsForProbe());
			io.mosip.kernel.biometrics.entities.BIR[] birsFromRequestDto = gson.fromJson(decodedVal,
					io.mosip.kernel.biometrics.entities.BIR[].class);
			// get the Biometric types
			List<BiometricType> bioTypeList = modalities.stream().map(bioType -> this.getBiometricType(bioType))
					.collect(Collectors.toList());
			String requestJson = null;
			generateSdkRequestResponseDto = new GenerateSdkRequestResponseDto();
			// populate the request object based on the method name
			if (methodName2.equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
				//the birs in sdkRequestDto are the extracted probe for the check quality method
				List<io.mosip.kernel.biometrics.entities.BIR> probeBirs = Arrays.asList(birsFromRequestDto);
				// convert BIRS to Biometric Record
				BiometricRecord checkQualityBiometricRecord = new BiometricRecord();
				checkQualityBiometricRecord.setSegments(probeBirs);
				CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
				checkQualityRequestDto.setSample(checkQualityBiometricRecord);
				checkQualityRequestDto.setModalitiesToCheck(bioTypeList);
				// TODO: set flags
				checkQualityRequestDto.setFlags(null);
				requestJson = gson.toJson(checkQualityRequestDto);
			}
			else if (methodName2.equalsIgnoreCase(MethodName.MATCH.getCode())) {
				//the birs in sdkRequestDto are the extracted gallery for the match method
				List<io.mosip.kernel.biometrics.entities.BIR> galleryBirs = Arrays.asList(birsFromRequestDto);
				List<BiometricRecord> galleryArr = new ArrayList<BiometricRecord>();
				BiometricRecord biometricRecordGallery = new BiometricRecord();
				biometricRecordGallery.setSegments(galleryBirs);
				galleryArr.add(biometricRecordGallery);
				
				//get the probe from /testcaseId/match folder
				String partnerId = getPartnerId();
				SdkPurpose sdkPurpose = getSdkPurpose(methodName1);
				InputStream objectStoreStream = getPartnerTestDataStream(sdkRequestDto, partnerId, sdkPurpose);
				
				//Here the probe is nested under "match" folder
				String testcaseFolder = sdkRequestDto.getTestcaseId()+"/" + MethodName.MATCH.toString().toLowerCase();
				byte[] probeFileBytes = getProbeData(sdkRequestDto, objectStoreStream, sdkPurpose, testcaseFolder);
				generateSdkRequestResponseDto.setTestDataSource(sdkRequestDto.getBioTestDataName());
				if (Objects.isNull(probeFileBytes)) {
					objectStoreStream = getDefaultTestDataStream(sdkRequestDto.getMethodName(), sdkPurpose);
					probeFileBytes = getProbeData(sdkRequestDto, objectStoreStream, sdkPurpose, testcaseFolder);
					generateSdkRequestResponseDto.setTestDataSource(AppConstants.MOSIP_DEFAULT);
				}
				if (Objects.nonNull(objectStoreStream) && Objects.nonNull(probeFileBytes)) {
					List<io.mosip.kernel.biometrics.entities.BIR> matchProbeBirs = cbeffReader
							.getBIRDataFromXML(probeFileBytes);
					// convert BIRS to Biometric Record
					BiometricRecord matchBiometricRecord = new BiometricRecord();
					matchBiometricRecord.setSegments(matchProbeBirs);
					//create match request DTO	
					MatchRequestDto matchRequestDto = new MatchRequestDto();
					matchRequestDto.setSample(matchBiometricRecord);
					matchRequestDto.setGallery(
							(BiometricRecord[]) galleryArr.toArray(new BiometricRecord[0]));
					matchRequestDto.setModalitiesToMatch(bioTypeList);
					// TODO: set flags
					matchRequestDto.setFlags(null);
					requestJson = gson.toJson(matchRequestDto);
				} else {
					List<ServiceError> serviceErrorsList = new ArrayList<>();
					ServiceError serviceError = new ServiceError();
					serviceError.setErrorCode(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorCode());
					serviceError.setMessage(ToolkitErrorCodes.OBJECT_STORE_FILE_NOT_AVAILABLE.getErrorMessage());
					serviceErrorsList.add(serviceError);
					responseWrapper.setErrors(serviceErrorsList);	
				}
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.INVALID_METHOD_NAME.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
			// convert the request json to base64encoded string
			if (requestJson != null) {
				RequestDto requestDto = new RequestDto();
				requestDto.setVersion(VERSION);
				requestDto.setRequest(this.base64Encode(requestJson));
				generateSdkRequestResponseDto.setGeneratedRequest(gson.toJson(requestDto));
				responseWrapper.setResponse(generateSdkRequestResponseDto);
			}
		} catch (ToolkitException ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In generateRequestForSDKTestcase method of TestCasesService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ex.getErrorCode());
			serviceError.setMessage(ex.getErrorText());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In generateRequestForSDKTestcase method of TestCasesService - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.GENERATE_SDK_REQUEST_ERROR.getErrorCode());
			serviceError
					.setMessage(ToolkitErrorCodes.GENERATE_SDK_REQUEST_ERROR.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(generateSdkRequest);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}

	private byte[] getXmlDataFromZipFile(InputStream zipFileIs, String purpose, String testcaseId, String name)
			throws Exception {
		byte[] bytes = null;
		try {
			ZipInputStream zis = new ZipInputStream(zipFileIs);
			ZipEntry zipEntry;
			String xmlFileName = purpose + "/";
			if (Objects.nonNull(testcaseId)) {
				xmlFileName += testcaseId + "/";
			}
			xmlFileName += name;
			while ((zipEntry = zis.getNextEntry()) != null) {
				if (xmlFileName.equals(zipEntry.getName())) {
					bytes = getZipEntryBytes(zis);
					break;
				}
			}
			zis.closeEntry();
			zis.close();
		} catch (Exception ex) {
			throw ex;
		}
		return bytes;
	}

	private byte[] getZipEntryBytes(ZipInputStream zis) throws IOException {
		byte[] b = new byte[1024];
		int len = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((len = zis.read(b)) > 0) {
			out.write(b, 0, len);
		}
		return out.toByteArray();
	}
	
	private InputStream getDefaultTestDataStream(String method, SdkPurpose sdkPurpose) {
		InputStream defaultTestDataStrem = null;
		String objectName = AppConstants.MOSIP_DEFAULT + "_" + sdkPurpose.toString() + ".zip";
		if (isObjectExistInObjectStore(AppConstants.TESTDATA, objectName)) {
			defaultTestDataStrem = getFromObjectStore(AppConstants.TESTDATA, objectName);
		}
		return defaultTestDataStrem;
	}
	
	private SdkPurpose getSdkPurpose(String methodName) {
		SdkPurpose purpose = null;
		switch (MethodName.fromCode(methodName)) {
		case MATCH:
			purpose = SdkPurpose.MATCHER;
			break;
		case EXTRACT_TEMPLATE:
			purpose = SdkPurpose.EXTRACT_TEMPLATE;
			break;
		case CHECK_QUALITY:
			purpose = SdkPurpose.CHECK_QUALITY;
			break;
		case SEGMENT:
			purpose = SdkPurpose.SEGMENT;
			break;
		case CONVERT_FORMAT:
			purpose = SdkPurpose.CONVERT_FORMAT;
			break;
		default:
		}
		return purpose;
	}

	private boolean isObjectExistInObjectStore(String container, String objectName) {
		return objectStore.exists(objectStoreAccountName, container, null, null, objectName);
	}

	private InputStream getFromObjectStore(String container, String objectName) {
		return objectStore.getObject(objectStoreAccountName, container, null, null, objectName);
	}
	

	public ResponseWrapper<TestCaseDto> getTestCaseById(String testCaseId) {
		ResponseWrapper<TestCaseDto> responseWrapper = new ResponseWrapper<>();
		TestCaseDto testcase = null;
		try {
			String testCaseJson = testCasesRepository.getTestCasesById(testCaseId);

			if (Objects.nonNull(testCaseJson)) {
				testcase = objectMapper.readValue(testCaseJson, TestCaseDto.class);
			} else {
				List<ServiceError> serviceErrorsList = new ArrayList<>();
				ServiceError serviceError = new ServiceError();
				serviceError.setErrorCode(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorCode());
				serviceError.setMessage(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorMessage());
				serviceErrorsList.add(serviceError);
				responseWrapper.setErrors(serviceErrorsList);
			}
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In getTestCaseById method of TestCasesService Service - " + ex.getMessage());
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.TESTCASE_NOT_AVAILABLE.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
		}
		responseWrapper.setId(getTestCaseId);
		responseWrapper.setVersion(AppConstants.VERSION);
		responseWrapper.setResponse(testcase);
		responseWrapper.setResponsetime(LocalDateTime.now());
		return responseWrapper;
	}
}
