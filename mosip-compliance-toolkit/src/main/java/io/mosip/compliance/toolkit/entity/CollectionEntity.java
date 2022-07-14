package io.mosip.compliance.toolkit.entity;

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
 * This entity class defines the database table collections.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "collections", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class CollectionEntity {

	@Id
	private String id;

	@Column(name = "sbi_project_id")
	private String sbiProjectId;

	@Column(name = "sdk_project_id")
	private String sdkProjectId;

	@Column(name = "abis_project_id")
	private String abisProjectId;

	@Column(name = "name")
	private String name;

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
