/**
 * 
 */
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
 * This entity class defines the database table master_data.
 * 
 * @author srinivas
 *
 */
@Component
@Entity
@Table(name = "master_data", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MasterDataEntity implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	@Id
	private String id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "value_json")
	private String valueJson;
	
	@Column(name = "cr_by")
	private String crBy;

	@Column(name = "cr_dtimes")
	private LocalDateTime crDate;

	@Column(name = "upd_by")
	private String upBy;

	@Column(name = "upd_dtimes")
	private LocalDateTime updDate;
	
}
