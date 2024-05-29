package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

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
 * This entity class defines the database table testcase.
 * 
 * @author Janardhan B S
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "testcase", schema = "toolkit")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class TestCaseEntity implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;
	
	@Column(name = "testcase_json")
	private String testcaseJson;
	
	@Column(name = "testcase_type")
	private String testcaseType;
	
	@Column(name = "spec_version")
	private String specVersion;
	
}
