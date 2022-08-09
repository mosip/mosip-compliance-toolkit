package io.mosip.compliance.toolkit.constants;

/**
 * ToolkitErrorCodes Enum for the services errors.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 */
public enum ToolkitErrorCodes {
	SUCCESS("TOOLKIT_SUCCESS_000", "Success"),
	PROJECTS_NOT_AVAILABLE("TOOLKIT_PROJECTS_001", "No project found for the user"),
	SBI_PROJECT_NOT_AVAILABLE("TOOLKIT_PROJECTS_002", "No matching SBI project available for the given id"),
	COLLECTION_NOT_AVAILABLE("TOOLKIT_COLLECTIONS_001", "No matching Collection available for the given project"),
	COLLECTION_TESTCASES_NOT_AVAILABLE("TOOLKIT_COLLECTIONS_002",
			"No matching Testcases available for the given collection id"),
	COLLECTION_TESTCASE_UNABLE_TO_ADD("TOOLKIT_COLLECTIONS_002", "Unable to add collection testcase mapping"),
	TESTRUN_UNABLE_TO_ADD("TOOLKIT_TESTRUN_001", "Unable to add testrun"),
	TESTRUN_NOT_AVAILABLE("TOOLKIT_TESTRUN_002", "No matching testrun available for the given id"),
	TESTRUN_UNABLE_TO_UPDATE("TOOLKIT_TESTRUN_003", "Unable to update testrun"),
	TESTRUN_DETAILS_UNABLE_TO_ADD("TOOLKIT_TESTRUN_004", "Unable to add testrun details"),
	TESTRUN_DETAILS_NOT_AVAILABLE("TOOLKIT_TESTRUN_005", "No matching TestRunDetails available for the given id"),
	COLLECTION_UNABLE_TO_ADD("TOOLKIT_COLLECTIONS_002", "Unable to add a Collection"),
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
	TESTCASE_NOT_AVAILABLE("TOOLKIT_TESTRUN_005", "No matching Testcase available for the given id"),

	SBI_PROJECT_UNABLE_TO_ADD("TOOLKIT_REQ_ERR_011", "SBI Project unable to add"),
	INVALID_SBI_SPEC_VERSION("TOOLKIT_REQ_ERR_012", "Invalid Sbi Spec Version"),
	INVALID_SBI_SPEC_VERSION_FOR_PROJECT_TYPE("TOOLKIT_REQ_ERR_013", "Invalid Sbi Spec Version for the Project Type"),
	INVALID_PURPOSE("TOOLKIT_REQ_ERR_014", "Invalid Purpose"),
	INVALID_DEVICE_TYPE("TOOLKIT_REQ_ERR_015", "Invalid Device Type"),
	INVALID_DEVICE_SUB_TYPE("TOOLKIT_REQ_ERR_016", "Invalid Device Sub Type"),
	INVALID_DEVICE_SUB_TYPE_FOR_DEVICE_TYPE("TOOLKIT_REQ_ERR_017", "Invalid Device Sub Type for the Device Type"),
	INVALID_SPEC_VERSION_FOR_PROJECT_TYPE("TOOLKIT_REQ_ERR_018", "Invalid Spec Version for the Project Type"),
	GET_TEST_CASE_ERROR("TOOLKIT_REQ_ERR_019", "Get Test Case error"),
	INVALID_CERTIFICATION_TYPE("TOOLKIT_REQ_ERR_020", "Invalid Certification Type"),
	INVALID_DEVICE_STATUS("TOOLKIT_REQ_ERR_021", "Invalid Device Status"),

	INVALID_VALIDATOR_DEF("TOOLKIT_VALIDATION_ERR_001", "Invalid validator definition in testcase."),
	TESTCASE_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_002", "Unable to perform validations."),
	PARTNERID_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_003", "Partner Id validation failed"),

	INVALID_METHOD_NAME("TOOLKIT_VALIDATION_ERR_003", "Invalid MethodName"),

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
