package io.mosip.compliance.toolkit.constants;

public enum ToolkitErrorCodes {
	
	SUCCESS("TOOLKIT_SUCCESS_000", "Success"),
	PROJECTS_NOT_AVAILABLE("TOOLKIT_PROJECTS_001", "No project found for the user"),
	SBI_PROJECT_NOT_AVAILABLE("TOOLKIT_PROJECTS_002", "No matching SBI project available for the given id"),
	
	COLLECTION_NOT_AVAILABLE("TOOLKIT_COLLECTIONS_001", "No matching Collection available for the given project"),
	COLLECTION_UNABLE_TO_ADD("TOOLKIT_COLLECTIONS_002", "Unable to add a Collection"),
	COLLECTION_TESTCASES_NOT_AVAILABLE("TOOLKIT_COLLECTIONS_002",
			"No matching Testcases available for the given collection id"),
	COLLECTION_TESTCASE_UNABLE_TO_ADD("TOOLKIT_COLLECTIONS_003", "Unable to add collection testcase mapping"),
	
	TESTRUN_UNABLE_TO_ADD("TOOLKIT_TESTRUN_001", "Unable to add testrun"),
	TESTRUN_NOT_AVAILABLE("TOOLKIT_TESTRUN_002", "No matching testrun available for the given id"),
	TESTRUN_UNABLE_TO_UPDATE("TOOLKIT_TESTRUN_003", "Unable to update testrun"),
	TESTRUN_DETAILS_UNABLE_TO_ADD("TOOLKIT_TESTRUN_004", "Unable to add testrun details"),
	TESTRUN_DETAILS_NOT_AVAILABLE("TOOLKIT_TESTRUN_005", "No matching TestRunDetails available for the given id"),
	TESTRUN_STATUS_NOT_AVAILABLE("TOOLKIT_TESTRUN_006", "TestRunStatus not available for the given id"),
	TESTCASE_NOT_AVAILABLE("TOOLKIT_TESTRUN_007", "No matching Testcase available for the given id"),
	BIOMETRIC_TESTDATA_NOT_AVAILABLE("TOOLKIT_BIOMETRIC_TESTDATA_001", "No matching biometric testdata available"),
	PAGE_NOT_FOUND("TOOLKIT_PAGE_ERR_001", "Page content not available"),
	INVALID_REQUEST_ID("TOOLKIT_REQ_ERR_001", "Request id is invalid"),
	INVALID_REQUEST_VERSION("TOOLKIT_REQ_ERR_002", "Request version is invalid"),
	INVALID_REQUEST_DATETIME("TOOLKIT_REQ_ERR_003", "Invalid request time"),
	INVALID_REQUEST_BODY("TOOLKIT_REQ_ERR_004", "Request body is invalid"),
	INVALID_REQUEST_DATETIME_NOT_CURRENT_DATE("TOOLKIT_REQ_ERR_005", "Request date should be current date"),
	INVALID_PROJECT_TYPE("TOOLKIT_REQ_ERR_006", "Project Type is invalid"),
	INVALID_TEST_CASE_ID("TOOLKIT_REQ_ERR_007", "Test Case Id is invalid"),
	INVALID_TEST_CASE_JSON("TOOLKIT_REQ_ERR_008", "Test Case Json is invalid"),
	INVALID_REQUEST_PARAM("TOOLKIT_REQ_ERR_009", "Invalid Request params"),
	SAVE_TEST_CASE_JSON_ERROR("TOOLKIT_REQ_ERR_010", "Save Test Case Json error"),
	INVALID_PARTNER_TYPE("TOOLKIT_REQ_ERR_011", "Invalid Partner Type"),
	SBI_PROJECT_UNABLE_TO_ADD("TOOLKIT_REQ_ERR_012", "SBI Project unable to add"),
	INVALID_SBI_SPEC_VERSION("TOOLKIT_REQ_ERR_013", "Invalid SBI Spec Version"),
	INVALID_SBI_SPEC_VERSION_FOR_PROJECT_TYPE("TOOLKIT_REQ_ERR_014", "Invalid SBI Spec Version for the Project Type"),
	INVALID_PURPOSE("TOOLKIT_REQ_ERR_015", "Invalid SBI Purpose"),
	INVALID_DEVICE_TYPE("TOOLKIT_REQ_ERR_016", "Invalid Device Type"),
	INVALID_DEVICE_SUB_TYPE("TOOLKIT_REQ_ERR_017", "Invalid Device Sub Type"),
	INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE("TOOLKIT_REQ_ERR_018", "Invalid Device Sub Type for the Device Type"),
	INVALID_SPEC_VERSION_FOR_PROJECT_TYPE("TOOLKIT_REQ_ERR_019", "Invalid Spec Version for the Project Type"),
	GET_TEST_CASE_ERROR("TOOLKIT_REQ_ERR_020", "Get Test Case error"),
	INVALID_CERTIFICATION_TYPE("TOOLKIT_REQ_ERR_021", "Invalid Certification Type"),
	INVALID_DEVICE_STATUS("TOOLKIT_REQ_ERR_022", "Invalid Device Status"),

	PROJECT_NAME_EXISTS("TOOLKIT_DB_ERR_001", "You have previously created a project with name: "),
	BIO_TEST_DATA_FILE_EXISTS("TOOLKIT_DB_ERR_002", "You have previously added biometric test data with name: "),
	
	SCHEMA_UNABLE_TO_ADD("TOOLKIT_SCHEMA_ERR_001", "Unable to add schema"),
	
	SCHEMA_UNABLE_TO_ADD("TOOLKIT_SCHEMA_ERR_001", "Unable to add schema"),
	
	OBJECT_STORE_ERROR("TOOLKIT_OBJECT_STORE_ERR_001", "Object store error"),
	OBJECT_STORE_FILE_EXISTS("TOOLKIT_OBJECT_STORE_ERR_002", "You have previously uploaded a file with same name: "),
	OBJECT_STORE_UNABLE_TO_ADD_FILE("TOOLKIT_OBJECT_STORE_ERR_003", "Unable to add file"),
	OBJECT_STORE_UNABLE_TO_GET_FILE("TOOLKIT_OBJECT_STORE_ERR_003", "Unable to get file"),
	OBJECT_STORE_FILE_NOT_AVAILABLE("TOOLKIT_OBJECT_STORE_ERR_004", "Testdata file not available"),
	OBJECT_STORE_INVALID_FILE_ID("TOOLKIT_OBJECT_STORE_ERR_005", "This file id is invalid for the logged in partner id."),
	OBJECT_STORE_SCHEMA_NOT_AVAILABLE("TOOLKIT_OBJECT_STORE_ERR_006", "Schema file not available"),
	
	//	SDK
	INVALID_SDK_PURPOSE("TOOLKIT_REQ_ERR_023", "Invalid SDK Purpose"),
	INVALID_SDK_URL("TOOLKIT_REQ_ERR_024", "Invalid SDK url"),
	SDK_PROJECT_NOT_AVAILABLE("TOOLKIT_PROJECTS_025", "No matching SDK project available for the given id"),
	SDK_PROJECT_UNABLE_TO_ADD("TOOLKIT_REQ_ERR_026", "SDK Project unable to add"),
	INVALID_SDK_SPEC_VERSION("TOOLKIT_REQ_ERR_027", "Invalid SDK Spec Version"),
	INVALID_MODALITY("TOOLKIT_REQ_ERR_028", "Invalid Modality"),
	GENERATE_SDK_REQUEST_ERROR("TOOLKIT_REQ_ERR_029", "Unable to generate request for SDK"),

	INVALID_VALIDATOR_DEF("TOOLKIT_VALIDATION_ERR_001", "Invalid validator definition in testcase."),
	TESTCASE_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_002", "Unable to perform validations."),
	PARTNERID_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_003", "Partner Id validation failed"),
	INVALID_METHOD_NAME("TOOLKIT_VALIDATION_ERR_004", "Invalid MethodName"),

	TECHNICAL_ERROR_EXCEPTION("TOOLKIT_REQ_ERR_500", "Technical Error");

	private final String errorCode;
	private final String errorMessage;

	private ToolkitErrorCodes(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public static ToolkitErrorCodes fromErrorCode(String errorCode) {
		for (ToolkitErrorCodes paramCode : ToolkitErrorCodes.values()) {
			if (paramCode.getErrorCode().equalsIgnoreCase(errorCode)) {
				return paramCode;
			}
		}
		return TECHNICAL_ERROR_EXCEPTION;
	}
}
