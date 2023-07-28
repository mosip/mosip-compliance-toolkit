package io.mosip.compliance.toolkit.dto.report;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PartnerDetailsDto {

	String id;
	Object metadata;
	// Partners response;
	Partner response;
	String responsetime;
	String version;
	List<ErrorDto> errors;

	@Getter
	@Setter
	public static class Partner {
		private String partnerID;
		private String status;
		private String organizationName;
		private String contactNumber;
		private String emailId;
		private String address;
		private String partnerType;
	}

//	@Getter
//	@Setter
//	public static class Partners {
//		private List<Partner> partners;
//		
//	}

	@Getter
	@Setter
	public static class ErrorDto {
		private String errorCode;
		private String message;
	}
}
