package io.mosip.compliance.toolkit.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.mosip.compliance.toolkit.config.LoggerConfiguration;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.OtherKey;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.core.cbeffutil.constant.CbeffConstant;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class BIRBuilder {

	private static final String UNKNOWN = "UNKNOWN";

	private static final String UNKNOWN_SmallCase = "unknown";

	private Logger log = LoggerConfiguration.logConfig(BIRBuilder.class);

	private static final String CBEFF_DEFAULT_FORMAT_ORG = "Mosip";
	private static final String CBEFF_DEFAULT_ALG_ORG = "HMAC";
	private static final String CBEFF_DEFAULT_ALG_TYPE = "SHA-256";
	private static final String EMPTY = "";

	public BIR buildBIR(byte[] bdb, String bioType, String bioSubType, long qualityScore, boolean forAuth,
			String specVersion) {

		log.debug("sessionId", "idType", "id", "started building BIR for for bioAttribute : {}", bioType);
		BiometricType biometricType = BiometricType.fromValue(bioType);

		// Format
		RegistryIDType birFormat = new RegistryIDType();
		birFormat.setOrganization(CBEFF_DEFAULT_FORMAT_ORG);
		birFormat.setType(String.valueOf(getFormatType(biometricType)));

		log.debug("sessionId", "idType", "id", "started building BIR algorithm for for bioAttribute : {}", bioType);

		// Algorithm
		RegistryIDType birAlgorithm = new RegistryIDType();
		birAlgorithm.setOrganization(CBEFF_DEFAULT_ALG_ORG);
		birAlgorithm.setType(CBEFF_DEFAULT_ALG_TYPE);

		log.debug("sessionId", "idType", "id", "started building Quality type for for bioAttribute : {}", bioType);

		// Quality Type
		QualityType qualityType = new QualityType();
		qualityType.setAlgorithm(birAlgorithm);
		qualityType.setScore(qualityScore);

		VersionType versionType = new VersionType(1, 1);

		if (UNKNOWN_SmallCase.equalsIgnoreCase(bioSubType) || "".equals(bioSubType)) {
			bioSubType = UNKNOWN;
		}

		BIR probeBir = new BIR.BIRBuilder().withBdb(bdb).withVersion(versionType).withCbeffversion(versionType)
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(birFormat).withQuality(qualityType)
						.withType(Arrays.asList(biometricType)).withSubtype(Arrays.asList(bioSubType))
						.withPurpose(forAuth ? PurposeType.VERIFY : PurposeType.ENROLL)
						.withLevel(ProcessedLevelType.RAW).withCreationDate(LocalDateTime.now(ZoneId.of("UTC")))
						.withIndex(UUID.randomUUID().toString()).build())
				.withSb(new byte[0]).withOthers(OtherKey.EXCEPTION, "false").withOthers(OtherKey.RETRIES, EMPTY)
				.withOthers(OtherKey.SDK_SCORE, EMPTY).withOthers(OtherKey.FORCE_CAPTURED, EMPTY)
				.withOthers(OtherKey.PAYLOAD, EMPTY).withOthers(OtherKey.SPEC_VERSION, specVersion).build();

		log.debug("sessionId", "idType", "id", "probe created (without bdb) : {}", probeBir);

		probeBir.setBdb(bdb);

		return probeBir;
	}

	public static long getFormatType(BiometricType biometricType) {
		long format = 0;
		switch (biometricType) {
		case FINGER:
			format = CbeffConstant.FORMAT_TYPE_FINGER;
			break;

		case EXCEPTION_PHOTO:
		case FACE:
			format = CbeffConstant.FORMAT_TYPE_FACE;
			break;
		case IRIS:
			format = CbeffConstant.FORMAT_TYPE_IRIS;
			break;
		}
		return format;
	}
}
