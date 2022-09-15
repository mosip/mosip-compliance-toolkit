package io.mosip.compliance.toolkit.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;

import io.mosip.compliance.toolkit.constants.ToolkitErrorCodes;
import io.mosip.compliance.toolkit.dto.SchemaDataDto;
import io.mosip.compliance.toolkit.service.SchemaService;
import io.mosip.compliance.toolkit.util.DataValidationUtil;
import io.mosip.compliance.toolkit.util.ObjectMapperConfig;
import io.mosip.compliance.toolkit.util.RequestValidator;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@RestController
public class SchemaController {

	/** The Constant SCHEMA_POST_ID application. */
	private static final String SCHEMA_POST_ID = "schema.post";

	@Autowired
	private RequestValidator requestValidator;

	@Autowired
	private SchemaService schemaService;

	@Autowired
	private ObjectMapperConfig objectMapperConfig;

	@PostMapping(value = "addSchema")
	public ResponseWrapper<Boolean> addSchemaToObjectStore(@RequestPart("SchemaData") String strRequestWrapper,
			@RequestParam("file") MultipartFile file, Errors errors) {
		try {
			RequestWrapper<SchemaDataDto> requestWrapper = objectMapperConfig.objectMapper()
					.readValue(strRequestWrapper, new TypeReference<RequestWrapper<SchemaDataDto>>() {
					});
			requestValidator.validate(requestWrapper, errors);
			requestValidator.validateId(SCHEMA_POST_ID, requestWrapper.getId(), errors);
			DataValidationUtil.validate(errors, SCHEMA_POST_ID);
			return schemaService.addSchemaToObjectStore(requestWrapper.getRequest(), file);
		} catch (Exception ex) {
			ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
			List<ServiceError> serviceErrorsList = new ArrayList<>();
			ServiceError serviceError = new ServiceError();
			serviceError.setErrorCode(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorCode());
			serviceError.setMessage(ToolkitErrorCodes.INVALID_REQUEST_BODY.getErrorMessage() + " " + ex.getMessage());
			serviceErrorsList.add(serviceError);
			responseWrapper.setErrors(serviceErrorsList);
			return responseWrapper;
		}
	}
}
