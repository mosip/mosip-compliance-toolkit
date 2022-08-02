package io.mosip.compliance.toolkit.dto;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class TestcaseJsonDto {
	private String testId;
	private String testName;
	private String testDescription;
	private String testCaseType;
	private int testOrderSequence;
	private String methodName;
	private String requestSchema;
	private String responseSchema;
	private List<ValidatorDefs> validators;
	private OtherAttributes otherAttributes;

	@Getter
	@Setter
	public static class ValidatorDefs {
		private String name;
		private String description;
	}

	@Getter
	@Setter
	public static class OtherAttributes {
		private String runtimeInput; // SBI related
		private List<String> purpose; // SBI related
		private List<String> biometricTypes; // SBI related
		private List<String> deviceSubTypes; // SBI related
		private List<String> segments; // SBI related
		private List<String> exceptions; // SBI related
		private List<String> sbiSpecVersions; // SBI related
		private String requestedScore; // SBI related
		private String deviceSubId; // SBI related
		private String bioCount; // SBI related
		private List<String> modalities; // SDK related
	}
}
