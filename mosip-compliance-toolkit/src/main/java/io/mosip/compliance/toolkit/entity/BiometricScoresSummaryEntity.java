package io.mosip.compliance.toolkit.entity;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.annotations.NamedNativeQuery;

import groovy.transform.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * This entity class defines the many named native queries and their mappings.
 * 
 * @author Mayura Deshmukh
 * @since 1.4.0
 *
 */
@Entity
@Getter
@Setter
@SqlResultSetMapping(name = "Mapping.BiometricScoresSummaryEntity", classes = {
		@ConstructorResult(targetClass = BiometricScoresSummaryEntity.class, columns = {
				@ColumnResult(name = "id", type = String.class),
				@ColumnResult(name = "male_0_10", type = Integer.class),
				@ColumnResult(name = "male_11_20", type = Integer.class),
				@ColumnResult(name = "male_21_30", type = Integer.class),
				@ColumnResult(name = "male_31_40", type = Integer.class),
				@ColumnResult(name = "male_41_50", type = Integer.class),
				@ColumnResult(name = "male_51_60", type = Integer.class),
				@ColumnResult(name = "male_61_70", type = Integer.class),
				@ColumnResult(name = "male_71_80", type = Integer.class),
				@ColumnResult(name = "male_81_90", type = Integer.class),
				@ColumnResult(name = "male_91_100", type = Integer.class),
				@ColumnResult(name = "female_0_10", type = Integer.class),
				@ColumnResult(name = "female_11_20", type = Integer.class),
				@ColumnResult(name = "female_21_30", type = Integer.class),
				@ColumnResult(name = "female_31_40", type = Integer.class),
				@ColumnResult(name = "female_41_50", type = Integer.class),
				@ColumnResult(name = "female_51_60", type = Integer.class),
				@ColumnResult(name = "female_61_70", type = Integer.class),
				@ColumnResult(name = "female_71_80", type = Integer.class),
				@ColumnResult(name = "female_81_90", type = Integer.class),
				@ColumnResult(name = "female_91_100", type = Integer.class) }) })
@NamedNativeQuery(name = "BiometricScoresSummaryEntity.getBiometricScoresForFinger", resultClass = BiometricScoresSummaryEntity.class, query = "SELECT b.id AS id,  "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_91_100, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_91_100 "
		+ "FROM biometric_scores b " + "WHERE b.partner_id = :partnerId " + "AND b.project_id = :projectId "
		+ "AND b.testrun_id = :testRunId " + "AND CAST(b.scores_json AS jsonb) ->> 'name' = :name "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'biometricType' = :biometricType "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'ageGroup' = :ageGroup "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'occupation' = :occupation GROUP BY b.id")
@NamedNativeQuery(name = "BiometricScoresSummaryEntity.getBiometricScoresForChildFinger", resultClass = BiometricScoresSummaryEntity.class, query = "SELECT b.id AS id,  "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_91_100, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_91_100 "
		+ "FROM biometric_scores b " + "WHERE b.partner_id = :partnerId " + "AND b.project_id = :projectId "
		+ "AND b.testrun_id = :testRunId " + "AND CAST(b.scores_json AS jsonb) ->> 'name' = :name "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'biometricType' = :biometricType "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'ageGroup' = :ageGroup GROUP BY b.id")
@NamedNativeQuery(name = "BiometricScoresSummaryEntity.getBiometricScoresForFace", resultClass = BiometricScoresSummaryEntity.class, query = "SELECT b.id AS id,  "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'male' THEN 1 ELSE NULL END) AS male_91_100, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '0-10' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_0_10, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '11-20' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_11_20, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '21-30' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_21_30, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '31-40' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_31_40, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '41-50' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_41_50, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '51-60' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_51_60, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '61-70' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_61_70, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '71-80' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_71_80, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '81-90' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_81_90, "
		+ "COUNT(CASE WHEN CAST(b.scores_json AS jsonb) ->> 'biometricScoreRange' = '91-100' AND CAST(b.scores_json AS jsonb) ->> 'gender' = 'female' THEN 1 ELSE NULL END) AS female_91_100 "
		+ "FROM biometric_scores b " + "WHERE b.partner_id = :partnerId " + "AND b.project_id = :projectId "
		+ "AND b.testrun_id = :testRunId " + "AND CAST(b.scores_json AS jsonb) ->> 'name' = :name "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'biometricType' = :biometricType "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'ageGroup' = :ageGroup "
		+ "AND CAST(b.scores_json AS jsonb) ->> 'race' = :race GROUP BY b.id")
public class BiometricScoresSummaryEntity {

	@Override
	public String toString() {
		return "BiometricScoresSummaryEntity [id=" + id + ", male_0_10=" + male_0_10 + ", male_11_20=" + male_11_20
				+ ", male_21_30=" + male_21_30 + ", male_31_40=" + male_31_40 + ", male_41_50=" + male_41_50
				+ ", male_51_60=" + male_51_60 + ", male_61_70=" + male_61_70 + ", male_71_80=" + male_71_80
				+ ", male_81_90=" + male_81_90 + ", male_91_100=" + male_91_100 + ", female_0_10=" + female_0_10
				+ ", female_11_20=" + female_11_20 + ", female_21_30=" + female_21_30 + ", female_31_40=" + female_31_40
				+ ", female_41_50=" + female_41_50 + ", female_51_60=" + female_51_60 + ", female_61_70=" + female_61_70
				+ ", female_71_80=" + female_71_80 + ", female_81_90=" + female_81_90 + ", female_91_100="
				+ female_91_100 + "]";
	}

	public BiometricScoresSummaryEntity(String id, Integer male_0_10, Integer male_11_20, Integer male_21_30,
			Integer male_31_40, Integer male_41_50, Integer male_51_60, Integer male_61_70, Integer male_71_80,
			Integer male_81_90, Integer male_91_100, Integer female_0_10, Integer female_11_20, Integer female_21_30,
			Integer female_31_40, Integer female_41_50, Integer female_51_60, Integer female_61_70, Integer female_71_80,
			Integer female_81_90, Integer female_91_100) {
		super();
		this.id = id;
		this.male_0_10 = male_0_10;
		this.male_11_20 = male_11_20;
		this.male_21_30 = male_21_30;
		this.male_31_40 = male_31_40;
		this.male_41_50 = male_41_50;
		this.male_51_60 = male_51_60;
		this.male_61_70 = male_61_70;
		this.male_71_80 = male_71_80;
		this.male_81_90 = male_81_90;
		this.male_91_100 = male_91_100;
		this.female_0_10 = female_0_10;
		this.female_11_20 = female_11_20;
		this.female_21_30 = female_21_30;
		this.female_31_40 = female_31_40;
		this.female_41_50 = female_41_50;
		this.female_51_60 = female_51_60;
		this.female_61_70 = female_61_70;
		this.female_71_80 = female_71_80;
		this.female_81_90 = female_81_90;
		this.female_91_100 = female_91_100;
	}

	public BiometricScoresSummaryEntity() {
		super();
	}

	@Id
	private String id;
	private Integer male_0_10;
	private Integer male_11_20;
	private Integer male_21_30;
	private Integer male_31_40;
	private Integer male_41_50;
	private Integer male_51_60;
	private Integer male_61_70;
	private Integer male_71_80;
	private Integer male_81_90;
	private Integer male_91_100;
	private Integer female_0_10;
	private Integer female_11_20;
	private Integer female_21_30;
	private Integer female_31_40;
	private Integer female_41_50;
	private Integer female_51_60;
	private Integer female_61_70;
	private Integer female_71_80;
	private Integer female_81_90;
	private Integer female_91_100;
}
