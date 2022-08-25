package io.mosip.compliance.toolkit.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.mosip.compliance.toolkit.constants.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.dto.sdk.CheckQualityRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.MatchRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.RequestDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidateRequestSchemaDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationInputDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResultDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidatorDefDto;
import io.mosip.compliance.toolkit.entity.TestCaseEntity;
import io.mosip.compliance.toolkit.exceptions.ToolkitException;
import io.mosip.compliance.toolkit.repository.TestCasesRepository;
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

	@Autowired
	ResourceLoader resourceLoader;

	@Autowired
	private ApplicationContext context;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TestCasesRepository testCasesRepository;

	private CbeffUtil cbeffReader = new CbeffImpl();

	Gson gson = new GsonBuilder().create();

	private static Map<String, byte[]> inputFiles = new HashMap<>();

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
			String testCaseSchemaJson = this.getSchemaJson("schemas/testcase_schema.json");
			if (isValidSbiTestCase(specVersion, purpose, deviceType, deviceSubType)) {
				List<TestCaseEntity> testCaseEntities = testCasesRepository
						.findAllSbiTestCaseBySpecVersion(specVersion);
				for (final TestCaseEntity testCaseEntity : testCaseEntities) {
					String testcaseJson = testCaseEntity.getTestcaseJson();
					if (AppConstants.SUCCESS
							.equals(this.validateJsonWithSchema(testcaseJson, testCaseSchemaJson).getStatus())) {
						TestCaseDto testCaseDto = objectMapper.readValue(testcaseJson, TestCaseDto.class);
						if (testCaseDto.getSpecVersion() != null && testCaseDto.getSpecVersion().equals(specVersion)
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
			String testCaseSchemaJson = this.getSchemaJson("schemas/testcase_schema.json");
			if (isValidSdkTestCase(specVersion, sdkPurpose)) {
				List<TestCaseEntity> testCaseEntities = testCasesRepository
						.findAllSdkTestCaseBySpecVersion(specVersion);
				for (final TestCaseEntity testCaseEntity : testCaseEntities) {
					String testcaseJson = testCaseEntity.getTestcaseJson();
					if (AppConstants.SUCCESS
							.equals(this.validateJsonWithSchema(testcaseJson, testCaseSchemaJson).getStatus())) {
						TestCaseDto testCaseDto = objectMapper.readValue(testcaseJson, TestCaseDto.class);
						if (testCaseDto.getSpecVersion() != null && testCaseDto.getSpecVersion().equals(specVersion)
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

	public ResponseWrapper<String> generateRequestForSDKTestcase(String methodName, String testcaseId,
			List<String> modalities) throws Exception {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		try {
			String requestJson = null;

			if (methodName.equalsIgnoreCase(MethodName.INIT.getCode())) {
				ObjectNode rootNode = objectMapper.createObjectNode();
				ObjectNode childNode = objectMapper.createObjectNode();
				rootNode.set("initParams", childNode);
				requestJson = gson.toJson(rootNode);
			} else {
				// read the testdata given as probe xml
				// TODO pass the orgname / partnerId
				byte[] probeFileBytes = this.getXmlData(null, testcaseId, "probe");

				// get the BIRs from the XML
				List<io.mosip.kernel.biometrics.entities.BIR> birsForProbe = cbeffReader
						.getBIRDataFromXML(probeFileBytes);
				// convert BIRS to Biometric Record
				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(birsForProbe);

				List<BiometricRecord> biometricRecordsArr = new ArrayList<BiometricRecord>();
				if (methodName.equalsIgnoreCase(MethodName.MATCH.getCode())) {
					for (int i = 1; i <= 5; i++) {
						// TODO pass the orgname / partnerId
						byte[] galleryFileBytes = this.getXmlData(null, testcaseId, "gallery" + i);
						if (galleryFileBytes != null) {
							// get the BIRs from the XML
							List<io.mosip.kernel.biometrics.entities.BIR> birsForGallery = cbeffReader
									.getBIRDataFromXML(galleryFileBytes);
							BiometricRecord biometricRecordGallery = new BiometricRecord();
							biometricRecordGallery.setSegments(birsForGallery);
							biometricRecordsArr.add(biometricRecordGallery);
						} else {
							i = 5;
						}
					}
				}

				// get the Biometric types
				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> this.getBiometricType(bioType))
						.collect(Collectors.toList());

				// populate the request object based on the method name
				if (methodName.equalsIgnoreCase(MethodName.CHECK_QUALITY.getCode())) {
					CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
					checkQualityRequestDto.setSample(biometricRecord);
					checkQualityRequestDto.setModalitiesToCheck(bioTypeList);
					// TODO: set flags
					checkQualityRequestDto.setFlags(null);
					System.out.println(checkQualityRequestDto);
					requestJson = gson.toJson(checkQualityRequestDto);
				}
				if (methodName.equalsIgnoreCase(MethodName.MATCH.getCode())) {
					MatchRequestDto matchRequestDto = new MatchRequestDto();
					matchRequestDto.setSample(biometricRecord);
					matchRequestDto.setGallery((BiometricRecord[]) biometricRecordsArr.toArray(new BiometricRecord[0]));
					matchRequestDto.setModalitiesToMatch(bioTypeList);
					// TODO: set flags
					matchRequestDto.setFlags(null);
					requestJson = gson.toJson(matchRequestDto);
				}
			}
			System.out.println(requestJson);
			// convert the request json to base64encoded string
			if (requestJson != null) {
				RequestDto requestDto = new RequestDto();
				requestDto.setVersion(VERSION);
				requestDto.setRequest(this.base64Encode(requestJson));
				responseWrapper.setResponse(gson.toJson(requestDto));
			}
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

	public ResponseWrapper<TestCaseResponseDto> saveTestCases(List<TestCaseDto> values) throws Exception {
		ResponseWrapper<TestCaseResponseDto> responseWrapper = new ResponseWrapper<>();
		TestCaseResponseDto testCaseResponseDto = new TestCaseResponseDto();
		Map<String, String> savedValues = new HashMap<String, String>();

		try {
			String testCaseSchemaJson = this.getSchemaJson("schemas/testcase_schema.json");
			for (TestCaseDto testCaseDto : values) {
				// Do JSON Schema Validation
				String jsonValue = objectMapper.writeValueAsString(testCaseDto);
				ValidationResultDto validationResultDto = this.validateJsonWithSchema(jsonValue, testCaseSchemaJson);
				if (AppConstants.SUCCESS.equals(validationResultDto.getStatus())) {
					// Get JSON Object
					// Do Validation on content of JSON
					if (isValidTestCaseId(testCaseDto)) {
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
							testCase = testCasesRepository.save(testCase);
						}
						// Check if test case present .. update
						else {
							TestCaseEntity testCase = checkTestCaseEntity.get();
							testCase.setTestcaseJson(jsonValue);
							testCase.setTestcaseType(testCaseDto.getTestCaseType());
							testCase.setSpecVersion(testCaseDto.getSpecVersion());
							testCase = testCasesRepository.update(testCase);
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
				testCaseSchemaJson = this.getSchemaJson("schemas/sbi/" + requestDto.getRequestSchema() + ".json");
			}
			if (requestDto.getTestCaseType().equalsIgnoreCase(AppConstants.SDK)) {
				testCaseSchemaJson = this.getSchemaJson("schemas/sdk/" + requestDto.getRequestSchema() + ".json");
			}
			// System.out.println(schemaJson);
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

	private void checkValidatorDefs(List<ValidatorDefDto> validatorDefs) {
		validatorDefs.forEach(v -> {
			try {
				Class<?> className = Class.forName("io.mosip.compliance.toolkit.validators." + v.getName());
				BaseValidator validator = (BaseValidator) className.getDeclaredConstructor().newInstance();
				log.debug("invloking validator: {}", validator);
			} catch (Exception ex) {
				log.debug("invalid validator: {}", ex.getMessage());
				throw new ToolkitException(ToolkitErrorCodes.INVALID_VALIDATOR_DEF.getErrorCode(),
						ToolkitErrorCodes.INVALID_VALIDATOR_DEF.getErrorMessage() + " - " + v.getName());
			}
		});
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

	private byte[] getXmlData(String orgName, String testcaseId, String name) {
		try {
			if (orgName == null) {
				orgName = AppConstants.MOSIP_DEFAULT;
			}
			String key = orgName + "/" + testcaseId + "/" + name;
			if (inputFiles.containsKey(key)) {
				return inputFiles.get(key);
			} else {
				// Read File Content
				String filePathName = "classpath:testdata/SDK/" + key + ".xml";
				log.info(filePathName);
				Resource resource = resourceLoader.getResource(filePathName);
				InputStream inputStream = resource.getInputStream();
				byte[] bytes = StreamUtils.copyToByteArray(inputStream);
				inputFiles.put(key, bytes);
				return bytes;
			}
		} catch (IOException ioe) {
			return null;
		}

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

	private String base64Encode(String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	public String getSchemaJson(String fileName) throws Exception {
		// Read File Content
		Resource resource = resourceLoader.getResource("classpath:" + fileName);
		InputStream inputStream = resource.getInputStream();
		try (Reader reader = new InputStreamReader(inputStream, UTF_8)) {
			return FileCopyUtils.copyToString(reader);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
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
