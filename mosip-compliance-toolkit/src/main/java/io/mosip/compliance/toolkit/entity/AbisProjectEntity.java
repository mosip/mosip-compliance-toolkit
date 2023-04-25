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
 * This entity class defines the database table abis_projects.
 * 
 * @author Mayura Deshmukh
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "abis_projects", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AbisProjectEntity implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "project_type")
	private String projectType;
	
	@Column(name = "url")
	private String url;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "inbound_queue_name")
	private String inboundQueueName;
	
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
	
	@Column(name = "outbound_queue_name")
	private String outboundQueueName;
	
	@Column(name = "bio_test_data_file_name")
	private String bioTestDataFileName;
	
	@Column(name = "abis_version")
	private String abisVersion;

}
