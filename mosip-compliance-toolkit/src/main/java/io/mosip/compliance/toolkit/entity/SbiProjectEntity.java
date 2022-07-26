package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
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
	@NotNull
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

	@Column(name = "partner_id")
	private String partnerId;

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
