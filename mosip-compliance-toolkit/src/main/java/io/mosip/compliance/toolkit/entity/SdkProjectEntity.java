package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This entity class defines the database table sdk_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "sdk_projects", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SdkProjectEntity implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "project_type")
	private String projectType;

	@Column(name = "sdk_version")
	private String sdkVersion;
	
	@Column(name = "url")
	private String url;

	@Column(name = "sdk_hash")
	private String sdkHash;

	@Column(name = "website_url")
	private String websiteUrl;

	@Column(name = "bio_test_data_file_name")
	private String bioTestDataFileName;
	
	@Column(name = "purpose")
	private String purpose;
	
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
