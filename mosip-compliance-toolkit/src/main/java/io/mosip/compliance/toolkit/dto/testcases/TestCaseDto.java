package io.mosip.compliance.toolkit.dto.testcases;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
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
		public ArrayList<Object> purpose;
		public ArrayList<Object> biometricTypes;
		public ArrayList<Object> deviceSubTypes;
		public ArrayList<Object> segments;
		public ArrayList<Object> exceptions;
		public Object requestedScore;
		public String bioCount;
		public String deviceSubId;
		public ArrayList<String> modalities;
		public ArrayList<String> sdkPurpose;
		public String convertSourceFormat;
		public String convertTargetFormat;
		public String timeout;
		@JsonProperty("resumeBtn")
	    public boolean resumeBtn;
		@JsonProperty("resumeAgainBtn")
	    public boolean resumeAgainBtn;
		@JsonProperty("keyRotationTestCase")
	    public boolean keyRotationTestCase;
		public String transactionId;
		public String invalidRequestAttribute;
		public ArrayList<String> abisPurpose;
	}
}