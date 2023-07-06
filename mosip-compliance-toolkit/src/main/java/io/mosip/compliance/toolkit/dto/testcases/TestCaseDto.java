package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class TestCaseDto implements Serializable {
	private static final long serialVersionUID = -679461384553195804L;
	public String testCaseType;
	public String testId;
	public String specVersion;
	public String testName;
	public String testDescription;
	public String androidTestDescription;
	@JsonProperty("isNegativeTestcase")
	public boolean isNegativeTestcase;
	@JsonProperty("inactive")
	public boolean inactive;
	public String inactiveForAndroid;
	public List<String> methodName;
	public List<String> requestSchema;
	public List<String> responseSchema;
	public List<List<ValidatorDef>> validatorDefs;
	public OtherAttributes otherAttributes;

	@Getter
	@Setter
	public static class ValidatorDef {
		public String name;
		public String description;
	}

	@Getter
	@Setter
	@JsonInclude(Include.NON_NULL)
	public static class OtherAttributes {
		public List<String> purpose;
		public List<String> biometricTypes;
		public List<String> deviceSubTypes;
		public List<String> segments;
		public List<String> exceptions;
		public String requestedScore;
		public String bioCount;
		public String deviceSubId;
		public String timeout;
		@JsonProperty("resumeBtn")
		public boolean resumeBtn;
		@JsonProperty("resumeAgainBtn")
		public boolean resumeAgainBtn;
		@JsonProperty("keyRotationTestCase")
		public boolean keyRotationTestCase;
		@JsonProperty("hashValidationTestCase")
		public boolean hashValidationTestCase;
		public String transactionId;
		public String invalidRequestAttribute;
		public List<String> modalities;
		public List<String> sdkPurpose;
		public String convertSourceFormat;
		public String convertTargetFormat;
		@JsonProperty("bulkInsert")
		public boolean bulkInsert;
		public String insertCount;
		public String insertReferenceId;
		public String identifyReferenceId;
		public List<String> identifyGalleryIds;
		public String expectedDuplicateCount;
		public String expectedFailureReason;
	}
}