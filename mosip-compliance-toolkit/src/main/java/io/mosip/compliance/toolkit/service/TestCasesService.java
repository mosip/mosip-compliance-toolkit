package io.mosip.compliance.toolkit.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.compliance.toolkit.config.TestCasesConfig;
import io.mosip.compliance.toolkit.constants.AppConstants;
import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.ProjectsResponseDto;
import io.mosip.compliance.toolkit.dto.sdk.CheckQualityRequestDto;
import io.mosip.compliance.toolkit.dto.sdk.RequestDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseDto;
import io.mosip.compliance.toolkit.dto.testcases.TestCaseResponseDto;
import io.mosip.compliance.toolkit.dto.testcases.ValidationResponseDto;
import io.mosip.compliance.toolkit.entity.TestCaseProjectEntity;
import io.mosip.compliance.toolkit.exceptions.TestCaseException;
import io.mosip.compliance.toolkit.repository.TestCasesProjectRepository;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
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

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TestCasesProjectRepository testCasesProjectRepository;

	private CbeffUtil cbeffReader = new CbeffImpl();
	
	//Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz").create();
	
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

	public List<String> generateTestCase(String testCaseType, String testCaseSchemaJson,
			List<TestCasesConfig.TestCaseConfig> testCases) throws Exception {
		List<String> testcases = new ArrayList<>();
		System.out.println("Testcases configured: " + testCases);
		testCases.forEach(s -> {
			try {
				ObjectNode rootNode = objectMapper.createObjectNode();
				rootNode.put("testCaseType", testCaseType);
				rootNode.put("testName", s.getName());
				rootNode.put("testId", s.getId());
				rootNode.put("testDescription", s.getDescription());
				rootNode.put("testOrderSequence", s.getTestOrderSequence());
				rootNode.put("methodName", s.getMethodName());
				rootNode.put("requestSchema", s.getRequestSchema());
				rootNode.put("responseSchema", s.getResponseSchema());
				ArrayNode validatorNode = objectMapper.createArrayNode();
				List<TestCasesConfig.TestCaseConfig.Validators> validators = s.getValidators();
				if (validators != null) {
					validators.forEach(v -> {
						ObjectNode childNode = objectMapper.createObjectNode();
						childNode.put("name", v.getName());
						childNode.put("description", v.getDescription());
						validatorNode.add(childNode);
					});
				}
				rootNode.set("validatorDefs", validatorNode);
				if (s.getOtherAttributes() != null && AppConstants.SBI.equalsIgnoreCase(testCaseType)) {
					TestCasesConfig.TestCaseConfig.OtherAttributes otherAttributesConfig = s.getOtherAttributes();
					ObjectNode otherAttributesNode = objectMapper.createObjectNode();
					otherAttributesNode.put("runtimeInput", otherAttributesConfig.getRuntimeInput());
					ArrayNode purposeNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getPurpose() != null) {
						otherAttributesConfig.getPurpose().forEach(p -> purposeNode.add(p));
					}
					otherAttributesNode.set("purpose", purposeNode);
					ArrayNode biometricTypesNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getBiometricTypes() != null) {
						otherAttributesConfig.getBiometricTypes().forEach(b -> biometricTypesNode.add(b));
					}
					otherAttributesNode.set("biometricTypes", biometricTypesNode);
					ArrayNode deviceSubTypesNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getDeviceSubTypes() != null) {
						otherAttributesConfig.getDeviceSubTypes().forEach(b -> deviceSubTypesNode.add(b));
					}
					otherAttributesNode.set("deviceSubTypes", deviceSubTypesNode);
					ArrayNode segmentsNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getSegments() != null) {
						otherAttributesConfig.getSegments().forEach(b -> segmentsNode.add(b));
					}
					otherAttributesNode.set("segments", segmentsNode);
					ArrayNode exceptionsNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getExceptions() != null) {
						otherAttributesConfig.getExceptions().forEach(b -> exceptionsNode.add(b));
					}
					otherAttributesNode.set("exceptions", exceptionsNode);
					ArrayNode sbiSpecVersionsNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getSbiSpecVersions() != null) {
						otherAttributesConfig.getSbiSpecVersions().forEach(b -> sbiSpecVersionsNode.add(b));
					}
					otherAttributesNode.set("sbiSpecVersions", sbiSpecVersionsNode);
					otherAttributesNode.put("requestedScore", otherAttributesConfig.getRequestedScore());
					otherAttributesNode.put("bioCount", otherAttributesConfig.getBioCount());
					otherAttributesNode.put("deviceSubId", otherAttributesConfig.getDeviceSubId());
					rootNode.set("otherAttributes", otherAttributesNode);
				}
				if (s.getOtherAttributes() != null && AppConstants.SDK.equalsIgnoreCase(testCaseType)) {
					TestCasesConfig.TestCaseConfig.OtherAttributes otherAttributesConfig = s.getOtherAttributes();
					ObjectNode otherAttributesNode = objectMapper.createObjectNode();
					ArrayNode modalitiesNode = objectMapper.createArrayNode();
					if (otherAttributesConfig.getModalities() != null) {
						otherAttributesConfig.getModalities().forEach(m -> modalitiesNode.add(m));
					}
					otherAttributesNode.set("modalities", modalitiesNode);
					rootNode.set("otherAttributes", otherAttributesNode);
				}
				String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
				// System.out.println(jsonString);
				// now validate it against the schema
				if (AppConstants.SUCCESS.equals(this.validateJsonWithSchema(jsonString, testCaseSchemaJson).getStatus())) {
					testcases.add(jsonString);
				}
			} catch (Exception ex) {
				System.out.println("exception occured: " + ex.getLocalizedMessage());
				throw new RuntimeException(ex);
				// TODO: handle exception
			}
		});
		System.out.println("generated number of testcases: " + testcases.size());
		return testcases;
	}

	public ValidationResponseDto validateJsonWithSchema(String sourceJson, String schemaJson) throws Exception {
		// create instance of the ObjectMapper class
		// ObjectMapper objectMapper = new ObjectMapper();
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
			ValidationResponseDto validationResponseDto = new ValidationResponseDto();
			// show the validation errors
			if (validationResult.isEmpty()) {
				// show custom message if there is no validation error
				System.out.println("There are no validation errors. Json matches the schema.");
				validationResponseDto.setDescription("There are no validation errors. Json matches the schema.");
				validationResponseDto.setStatus(AppConstants.SUCCESS);
				return validationResponseDto;
			} else {
				List<String> errors = new ArrayList<>();
				// show all the validation error
				validationResult.forEach(vm -> errors.add(vm.getMessage()));
				System.out.println(errors.toString());
				validationResponseDto.setDescription(errors.toString());
				validationResponseDto.setStatus(AppConstants.FAILURE);
				return validationResponseDto;
			}
		} catch (Exception e) {
			throw e;
			// TODO: handle exception
		}
	}

//	public String generateRequest(String schemaName) throws Exception {
//		try {
//			JCodeModel codeModel = new JCodeModel();
//			File schemaJsonFile = ResourceUtils.getFile("classpath:schemas/sbi/" + schemaName + ".json");
//			String sourceJson = new String(Files.readAllBytes(schemaJsonFile.toPath()));
//			GenerationConfig config = new DefaultGenerationConfig() {
//				@Override
//				public boolean isGenerateBuilders() { // set config option by overriding method
//					return false;
//				}
//
//				@Override
//				public InclusionLevel getInclusionLevel() {
//					return InclusionLevel.USE_DEFAULTS;
//				}
//
//				/**
//				 * @return <code>false</code>
//				 */
//				@Override
//				public boolean isUseInnerClassBuilders() {
//					return true;
//				}
//			};
//			SchemaMapper mapper = new SchemaMapper(
//					new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
//			mapper.generate(codeModel, schemaName, "io.mosip.compliance.toolkit.genarated", sourceJson);
//			codeModel.build(new File("src/main/java/"));
//
//			Class<?> className = Class.forName("io.mosip.compliance.toolkit.genarated." + schemaName);
//			System.out.print("Class represented by c1: " + className);
//			Object obj = className.getDeclaredConstructor().newInstance();
//			objectMapper.setSerializationInclusion(Include.USE_DEFAULTS);
//			objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//			return objectMapper.writeValueAsString(obj);
//		} catch (Exception ex) {
//			throw ex;
//		}
//	}

	public String generateRequestForSDKTestcase(String methodName, String testcaseId, List<String> modalities)
			throws Exception {
		try {
			String requestJson = null;

			if (methodName.equalsIgnoreCase(AppConstants.SDK_METHOD_INIT)) {
				ObjectNode rootNode = objectMapper.createObjectNode();
				ObjectNode childNode = objectMapper.createObjectNode();
				rootNode.set("initParams", childNode);
				requestJson = gson.toJson(rootNode);
			} else {
				//read the testdata given as probe xml
				byte[] inputFileBytes = this.getCbeffTestData(null, testcaseId);
				//get the birs from the xml
				List<BIR> birs = cbeffReader.getBIRDataFromXML(inputFileBytes);
				//convert birs to Biometric Record
				BiometricRecord biometricRecord = new BiometricRecord();
				biometricRecord.setSegments(birs);
				//get the biometric types
				List<BiometricType> bioTypeList = modalities.stream().map(bioType -> this.getBiometricType(bioType))
						.collect(Collectors.toList());
				//populate the request object based on the method name
				if (methodName.equalsIgnoreCase(AppConstants.SDK_METHOD_CHECK_QUALITY)) {
					CheckQualityRequestDto checkQualityRequestDto = new CheckQualityRequestDto();
					checkQualityRequestDto.setSample(biometricRecord);
					checkQualityRequestDto.setModalitiesToCheck(bioTypeList);
					// TODO: set flags
					checkQualityRequestDto.setFlags(null);
					requestJson = gson.toJson(checkQualityRequestDto);
				}
			}
			System.out.println(requestJson);
			//convert the request json to base64encoded string
			if (requestJson != null) {
				RequestDto requestDto = new RequestDto();
				requestDto.setVersion(VERSION);
				requestDto.setRequest(this.base64Encode(requestJson));
				return gson.toJson(requestDto);
			}
			// TODO
			// thorw exception;
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public ResponseWrapper<TestCaseResponseDto> saveTestCases(List<TestCaseDto> values, String testCaseSchemaJson) throws Exception {
		ResponseWrapper<TestCaseResponseDto> responseWrapper = new ResponseWrapper<>();
		TestCaseResponseDto testCaseResponseDto = new TestCaseResponseDto();
		Map<String, String> savedValues = new HashMap<String, String> ();			
		
		try
		{
			for (TestCaseDto testCaseDto : values)
			{
				//Do JSON Schema Validation
				String jsonValue = objectMapper.writeValueAsString (testCaseDto);
				if (AppConstants.SUCCESS.equals(this.validateJsonWithSchema(jsonValue, testCaseSchemaJson).getStatus())) {
					//Get JSON Object
					//Do Validation on content of JSON
					if (isValidTestCases(testCaseDto))
					{
						String testCaseId = testCaseDto.getTestId();
						Optional<TestCaseProjectEntity> checkTestCaseProjectEntity = Optional.empty();
						checkTestCaseProjectEntity = testCasesProjectRepository.findById(testCaseId);
						
						//else if test case not present .. save
						if (checkTestCaseProjectEntity.isEmpty() || !checkTestCaseProjectEntity.isPresent()) {
							TestCaseProjectEntity testCaseProjectEntity = new TestCaseProjectEntity ();
							testCaseProjectEntity.setId(testCaseId);
							testCaseProjectEntity.setTestcaseJson(jsonValue);
							testCaseProjectEntity.setTestcaseType(testCaseDto.getTestCaseType());
							testCaseProjectEntity.setSpecVersion(testCaseDto.getSpecVersion());
							testCaseProjectEntity = testCasesProjectRepository.save(testCaseProjectEntity);
						}
						//Check if test case present .. update
						else {
							TestCaseProjectEntity testCaseProjectEntity = checkTestCaseProjectEntity.get();
							testCaseProjectEntity.setTestcaseJson(jsonValue);
							testCaseProjectEntity.setTestcaseType(testCaseDto.getTestCaseType());
							testCaseProjectEntity.setSpecVersion(testCaseDto.getSpecVersion());
							testCaseProjectEntity = testCasesProjectRepository.update(testCaseProjectEntity);
						}
						savedValues.put(testCaseId, jsonValue);
					}
				}
				else
				{
					ToolkitErrorCodes errorCode = null; 
					errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_JSON;
					throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
				}
			}
			testCaseResponseDto.setTestCases(savedValues);
		}
		catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ExceptionUtils.getStackTrace(ex));
			log.error("sessionId", "idType", "id", "In saveTestCases method of Test Cases Service - " + ex.getMessage());

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
	
	/**
	* Verifies test case is valid.
	* validates testcaseid starts with typename
	*
	* @param TestCaseDto
	* @return boolean
	*/
	private boolean isValidTestCases(TestCaseDto testCaseDto) throws TestCaseException {
		ToolkitErrorCodes errorCode = null; 
		String type = testCaseDto.getTestCaseType();
		String testId = testCaseDto.getTestId();
		if (!testId.startsWith(type)){
			errorCode = ToolkitErrorCodes.INVALID_TEST_CASE_ID;
			throw new TestCaseException (errorCode.getErrorCode(), errorCode.getErrorMessage ());
		}		

		return true;
	}
	
	private byte[] getCbeffTestData(String orgName, String testcaseId) throws IOException {

		if (orgName == null) {
			orgName = AppConstants.MOSIP_DEFAULT;
		}
		String key = orgName + "/" + testcaseId;
		if (inputFiles.containsKey(key)) {
			return inputFiles.get(key);
		} else {
			File probeFile = ResourceUtils.getFile("classpath:testdata/SDK/" + key + "/probe.xml");
			String fileContents = new String(Files.readAllBytes(probeFile.toPath()));
			inputFiles.put(key, fileContents.getBytes());
			return inputFiles.get(key);
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
}
