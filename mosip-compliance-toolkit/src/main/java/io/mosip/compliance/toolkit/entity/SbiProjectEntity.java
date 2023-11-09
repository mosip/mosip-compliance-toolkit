package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity class defines the database table sbi_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "sbi_projects", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SbiProjectEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "project_type")
	private String projectType;

	@Column(name = "sbi_version")
	private String sbiVersion;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "device_type")
	private String deviceType;

	@Column(name = "device_sub_type")
	private String deviceSubType;

	@Column(name = "device_image1")
	private String deviceImage1;

	@Column(name = "device_image2")
	private String deviceImage2;

	@Column(name = "device_image3")
	private String deviceImage3;

	@Column(name = "device_image4")
	private String deviceImage4;

	@Column(name = "sbi_hash")
	private String sbiHash;

	@Column(name = "website_url")
	private String websiteUrl;

	@Column(name = "partner_id")
	private String partnerId;

	@Column(name = "org_name")
	private String orgName;

	@Column(name = "cr_by")
	private String crBy;

	@Column(name = "cr_dtimes")
	private LocalDateTime crDate;

	@Column(name = "upd_by")
	private String upBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDate;

	@Column(name = "is_deleted")
	private boolean isDeleted;

	@Column(name = "del_dtimes")
	private LocalDateTime delTime;

}
