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

@Component
@Entity
@Table(name = "biometric_testdata", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class BiometricTestDataEntity implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -1163852475507127582L;

	@Id
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

	@Column(name = "purpose")
	private String purpose;

	@Column(name = "partner_id")
	private String partnerId;

	@Column(name = "org_name")
	private String orgName;

	@Column(name = "file_id")
	private String fileId;
	
	@Column(name = "file_hash")
	private String fileHash;

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
