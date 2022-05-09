package io.mosip.compliance.toolkit.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(prefix = "mosip.compliance-toolkit.test-cases")
@Configuration
@Getter
@Setter
@ToString
public class TestCasesConfig {

	public List<TestCaseConfig> sbiTestCases;

	public List<TestCaseConfig> abisTestCases;

	public List<TestCaseConfig> sdkTestCases;

	@Getter
	@Setter
	public static class TestCaseConfig {
		private String id;
		private String name;
		private String description;
		private int testOrderSequence;
		private String methodName;
		private String requestSchema;
		private String responseSchema;
		private List<Validators> validators;
		private OtherAttributes otherAttributes;

		@Getter
		@Setter
		public static class Validators {
			private String name;
			private String description;
		}

		@Getter
		@Setter
		public static class OtherAttributes {
			private String runtimeInput; //SBI related
			private List<String> purpose; //SBI related
			private List<String> biometricTypes; //SBI related
			private List<String> deviceSubTypes; //SBI related
			private List<String> segments; //SBI related
			private List<String> exceptions; //SBI related
			private List<String> sbiSpecVersions; //SBI related
			private String requestedScore; //SBI related
			private String deviceSubId; //SBI related
			private String bioCount; //SBI related
			private List<String> modalities; //SDK related
		}
	}

}