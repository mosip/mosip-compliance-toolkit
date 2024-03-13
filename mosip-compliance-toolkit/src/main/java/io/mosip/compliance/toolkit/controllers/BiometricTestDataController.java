package io.mosip.compliance.toolkit.controllers;

import java.util.List;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.AddBioTestDataResponseDto;
import io.mosip.compliance.toolkit.dto.BiometricTestDataDto;
import io.mosip.compliance.toolkit.service.BiometricTestDataService;
import io.mosip.compliance.toolkit.util.CommonUtil;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
@Tag(name = "biometric-testdata-controller")
public class BiometricTestDataController {

	/** The Constant BIOMETRIC_TESTDATA_POST_ID application. */
	private static final String BIOMETRIC_TESTDATA_POST_ID = "biometric.testdata.post";

	@Autowired
	private BiometricTestDataService biometricTestDataService;

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	private Logger log = LoggerConfiguration.logConfig(BiometricTestDataController.class);

	@GetMapping(value = "/getListOfBiometricTestData")
	@Operation(summary = "Get list of biometric testdata", description = "Get list of biometric testdata uploaded by partners", tags = "biometric-testdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<BiometricTestDataDto>> getListOfBiometricTestData() {
		return biometricTestDataService.getListOfBiometricTestData();
	}

	@PostMapping(value = "/addBiometricTestData", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
			MediaType.APPLICATION_JSON_VALUE })
	@Operation(summary = "Add biometric testdata", description = "Partners can upload their own biometric testdata.", tags = "biometric-testdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<AddBioTestDataResponseDto> addBiometricTestData(@RequestParam("file") MultipartFile file,
																		   @RequestPart("biometricMetaData") String strRequestWrapper, Errors errors) {
		try {
			RequestWrapper<BiometricTestDataDto> requestWrapper = objectMapperConfig.objectMapper()
					.readValue(strRequestWrapper, new TypeReference<RequestWrapper<BiometricTestDataDto>>() {
					});
			requestValidator.validate(requestWrapper, errors);
			requestValidator.validateId(BIOMETRIC_TESTDATA_POST_ID, requestWrapper.getId(), errors);
			DataValidationUtil.validate(errors, BIOMETRIC_TESTDATA_POST_ID);
			return biometricTestDataService.addBiometricTestdata(requestWrapper.getRequest(), file);
		} catch (Exception ex) {
			log.debug("sessionId", "idType", "id", ex.getStackTrace());
			log.error("sessionId", "idType", "id",
					"In addBiometricTestData method of BiometricTestDataController - " + ex.getMessage());
			ResponseWrapper<AddBioTestDataResponseDto> responseWrapper = new ResponseWrapper<>();
			String errorCode = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode();
			String errorMessage = ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage();
			responseWrapper.setErrors(CommonUtil.getServiceErr(errorCode,errorMessage));
			return responseWrapper;
		}
	}

	@GetMapping(value = "/getBiometricTestDataFile/{id}")
	@Operation(summary = "Get biometric testdata file", description = "Get biometric testdata file by id", tags = "biometric-testdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Resource> getBiometricTestDataFile(@PathVariable String id) {
		return biometricTestDataService.getBiometricTestDataFile(id);
	}

	@GetMapping(value = "/getBioTestDataNames")
	@Operation(summary = "Get bio testdata names", description = "Get bio testdata names based on the purpose", tags = "biometric-testdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseWrapper<List<String>> getBioTestDataNames(@RequestParam(required = true) String purpose) {
		return biometricTestDataService.getBioTestDataNames(purpose);
	}

	@GetMapping(value = "/getSampleBioTestDataFile")
	@Operation(summary = "Get sample bio testdata file", description = "Download sample bio testdata file according to the purpose", tags = "biometric-testdata-controller")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden" ,content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found" ,content = @Content(schema = @Schema(hidden = true)))})
	public ResponseEntity<Resource> getSampleBioTestDataFile(@RequestParam(required = true) String purpose) {
		return biometricTestDataService.getSampleBioTestDataFile(purpose);
	}
}