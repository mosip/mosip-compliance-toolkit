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
	TESTRUN_DELETE_ERROR("TOOLKIT_TESTRUN_007", "TestRun delete error"),
	TESTCASE_NOT_AVAILABLE("TOOLKIT_TESTCASE_001", "No matching Testcase available for the given id"),
	BIOMETRIC_TESTDATA_NOT_AVAILABLE("TOOLKIT_BIOMETRIC_TESTDATA_001", "No matching biometric testdata available"),
	BIOMETRIC_SCORES_DATA_NOT_AVAILABLE("TOOLKIT_BIOMETRIC_SCORESDATA_001", "No matching biometric scores data available"),
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
	SAVE_TEST_CASE_JSON_ERROR("TOOLKIT_REQ_ERR_010", "Cannot save the testcase."),
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

	INVALID_BIO_SUB_TYPE("TOOLKIT_REQ_ERR_023", "Invalid Bio Sub Type"),
	SOURCE_NOT_VALID_FINGER_ISO_FORMAT_EXCEPTION("TOOLKIT_REQ_ERR_024", "Source not valid ISO ISO19794_4_2011"),
	SOURCE_NOT_VALID_FACE_ISO_FORMAT_EXCEPTION("TOOLKIT_REQ_ERR_025", "Source not valid ISO ISO19794_5_2011"),
	SOURCE_NOT_VALID_IRIS_ISO_FORMAT_EXCEPTION("TOOLKIT_REQ_ERR_026", "Source not valid ISO ISO19794_6_2011"),
	SOURCE_NOT_VALID_BASE64URLENCODED_EXCEPTION("TOOLKIT_REQ_ERR_027", "Source not valid base64urlencoded"),

	// SDK
	INVALID_SDK_PURPOSE("TOOLKIT_REQ_ERR_028", "Invalid SDK Purpose"),
	INVALID_SDK_URL("TOOLKIT_REQ_ERR_029", "Invalid SDK url"),
	SDK_PROJECT_NOT_AVAILABLE("TOOLKIT_PROJECTS_030", "No matching SDK project available for the given id"),
	SDK_PROJECT_UNABLE_TO_ADD("TOOLKIT_REQ_ERR_031", "SDK Project unable to add"),
	INVALID_SDK_SPEC_VERSION("TOOLKIT_REQ_ERR_032", "Invalid SDK Spec Version"),
	INVALID_MODALITY("TOOLKIT_REQ_ERR_033", "Invalid Modality"),
	GENERATE_SDK_REQUEST_ERROR("TOOLKIT_REQ_ERR_034", "Unable to generate request for SDK"),
	INVALID_FINGER_COMPRESSION_TYPE("TOOLKIT_REQ_ERR_035",
			"Invalid Image Compression Type for Finger Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS, WSQ]"),
	INVALID_IRIS_COMPRESSION_TYPE("TOOLKIT_REQ_ERR_036",
			"Invalid Image Compression Type for Iris Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS]"),
	INVALID_FACE_COMPRESSION_TYPE("TOOLKIT_REQ_ERR_037",
			"Invalid Image Compression Type for Face Modality, allowed values[JPEG_2000_LOSSY, JPEG_2000_LOSS_LESS]"),
	AUTH_BIO_VALUE_DECRYPT_ERROR("TOOLKIT_REQ_ERR_038", "Auth Bio value decryption failed"),
	ENCRYPTION_KEY_ERROR("TOOLKIT_REQ_ERR_039",
			"Unable to get the encryption certificate from KeyManager for the appId"),

	INVALID_USER_DETAILS("TOOLKIT_REQ_ERR_040", "Authorities of Logged in user is null"),

	RCAPTURE_DATA_ENCRYPT_ERROR("TOOLKIT_REQ_ERR_041", "Encryption of rcapture data failed"),

	RCAPTURE_DATA_DECRYPT_ERROR("TOOLKIT_REQ_ERR_042", "Decryption of rcapture data failed"),
	FILE_WITH_MULTIPLE_EXTENSIONS("TOOLKIT_REQ_ERR_043", "File name should not contain multiple extensions"),
	FILE_WITHOUT_EXTENSIONS("TOOLKIT_REQ_ERR_043", "Unable to upload a file without extension"),
	PROJECT_NAME_EXISTS("TOOLKIT_DB_ERR_001", "You have previously created a project with name: "),
	BIO_TEST_DATA_FILE_EXISTS("TOOLKIT_DB_ERR_002", "You have previously added biometric test data with name: "),
	COLLECTION_NAME_EXISTS("TOOLKIT_DB_ERR_003", "You have previously created a collection with name: "),

	RESOURCE_UPLOAD_ERROR("TOOLKIT_RESOURCE_UPLOAD_ERR_001", "Unable to upload resource"),

	OBJECT_STORE_ERROR("TOOLKIT_OBJECT_STORE_ERR_001", "Object store error"),
	OBJECT_STORE_FILE_EXISTS("TOOLKIT_OBJECT_STORE_ERR_002", "You have previously uploaded a file with same name: "),
	OBJECT_STORE_UNABLE_TO_ADD_FILE("TOOLKIT_OBJECT_STORE_ERR_003", "Unable to add file"),
	OBJECT_STORE_UNABLE_TO_GET_FILE("TOOLKIT_OBJECT_STORE_ERR_003", "Unable to get file"),
	OBJECT_STORE_FILE_NOT_AVAILABLE("TOOLKIT_OBJECT_STORE_ERR_004", "Object store file not available"),
	OBJECT_STORE_INVALID_FILE_ID("TOOLKIT_OBJECT_STORE_ERR_005",
			"This file id is invalid for the logged in partner id."),
	OBJECT_STORE_SCHEMA_NOT_AVAILABLE("TOOLKIT_OBJECT_STORE_ERR_006", "Schema file not available"),

	INVALID_VALIDATOR_DEF("TOOLKIT_VALIDATION_ERR_001", "Invalid validator definition in testcase."),
	TESTCASE_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_002", "Unable to perform validations."),
	PARTNERID_VALIDATION_ERR("TOOLKIT_VALIDATION_ERR_003", "Partner Id validation failed"),
	INVALID_METHOD_NAME("TOOLKIT_VALIDATION_ERR_004", "Invalid MethodName"),

	TESTDATA_WRONG_PURPOSE("TOOLKIT_TESTDATA_ERR_001", "Testdata has wrong purpose"),
	TESTDATA_INVALID_GALLERY("TOOLKIT_TESTDATA_ERR_002", "Testdata has invalid gallery file "),
	TESTDATA_STRUCTURE_ERROR("TOOLKIT_TESTDATA_ERR_003", "Testdata folder structure error"),
	TESTDATA_INVALID_FOLDER("TOOLKIT_TESTDATA_ERR_004", "Testdata has invalid folder"),
	TESTDATA_INVALID_FILE("TOOLKIT_TESTDATA_ERR_005", "Testdata file is invalid"),
	TESTDATA_VALIDATION_UNSUCCESSFULL("TOOLKIT_TESTDATA_ERR_006", "Testdata validation failed."),

	ZIP_HIGH_COMPRESSION_RATIO_ERROR("TOOLKIT_ZIP_FILE_ERR_001",
			"Ratio between compressed and uncompressed data is highly suspicious, looks like a Zip Bomb Attack."),
	ZIP_SIZE_TOO_LARGE_ERROR("TOOLKIT_ZIP_FILE_ERR_002",
			"The uncompressed data size is too much for the application resource capacity."),
	ZIP_ENTRIES_TOO_MANY_ERROR("TOOLKIT_ZIP_FILE_ERR_003",
			"Too much entries in this archive, can lead to inodes exhaustion of the system."),

	TECHNICAL_ERROR_EXCEPTION("TOOLKIT_REQ_ERR_500", "Technical Error"),

	ABIS_DATA_SHARE_URL_EXCEPTION("ABIS_DATA_SHARE_URL_EXCEPTION_001", "Unable to generate data share url"),
	ABIS_EXPIRE_DATA_SHARE_URL_EXCEPTION("ABIS_DATA_SHARE_URL_EXCEPTION_002", "Unable to expire data share url"),
	ABIS_PROJECT_NOT_AVAILABLE("TOOLKIT_ABIS_PROJECTS_001", "No matching ABIS project available for the given id"),
	INVALID_ABIS_SPEC_VERSION("TOOLKIT_ABIS_PROJECTS_002", "Invalid ABIS Spec Version"),
	ABIS_PROJECT_UNABLE_TO_ADD("TOOLKIT_ABIS_PROJECTS_003", "ABIS Project unable to add"),
	INVALID_ABIS_URL("TOOLKIT_ABIS_PROJECTS_004", "Invalid ABIS url"),
	TOOLKIT_REPORT_ERR("TOOLKIT_REPORT_001", "Error during report generation"),
	TOOLKIT_REPORT_NOT_AVAILABLE_ERR("TOOLKIT_REPORT_002",
			"Report Data is not available. Try with correct values for partner id, project type, project id, collection id and test run id."),
	TOOLKIT_REPORT_STATUS_INVALID_ERR("TOOLKIT_REPORT_003",
			"Report status cannot be updated to new value since currently it is :"),
	TOOLKIT_REPORT_STATUS_UPDATE_ERR("TOOLKIT_REPORT_004", "Error while changing report status"),
	TOOLKIT_REPORT_GET_ERR("TOOLKIT_REPORT_005", "Error while fetching list of reports"),
	TOOLKIT_INVALID_REPORT_STATUS_ERR("TOOLKIT_REPORT_006", "Error while fetching list of reports. Invalid report status: "),
	TOOLKIT_CERTIFICATE_PARSING_ERR("TOOLKIT_CERTIFICATE_PARSING_001", "Error while parsing certificate : "),
	PARTNER_CONSENT_TEMPLATE_ERR("TOOLKIT_PARTNER_CONSENT_ERR_001", "Error fetching biometric consent template."),
	PARTNER_CONSENT_UNABLE_TO_ADD("TOOLKIT_PARTNER_CONSENT_ERR_002", "Error saving partner consent data."),
	PARTNER_CONSENT_STATUS_ERR("TOOLKIT_PARTNER_CONSENT_ERR_003", "Error fetching partner consent status.");

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