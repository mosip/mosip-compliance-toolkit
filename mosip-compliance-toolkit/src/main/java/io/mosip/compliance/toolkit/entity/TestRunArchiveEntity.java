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

@Component
@Entity
@Table(name = "test_run_archive", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TestRunArchiveEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7523623699534779126L;

	@Id
	private String id;

	@Column(name = "collection_id")
	private String collectionId;

	@Column(name = "run_dtimes")
	private LocalDateTime runDtimes;

	@Column(name = "execution_dtimes")
	private LocalDateTime executionDtimes;

	@Column(name = "run_configuration_json")
	private String runConfigurationJson;

	@Column(name = "partner_id")
	private String partnerId;

	@Column(name = "cr_by")
	private String crBy;

	@Column(name = "cr_dtimes")
	private LocalDateTime crDtimes;

	@Column(name = "upd_by")
	private String updBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDtimes;

	@Column(name = "is_deleted")
	private boolean isDeleted;

	@Column(name = "del_dtimes")
	private LocalDateTime delTime;
}
