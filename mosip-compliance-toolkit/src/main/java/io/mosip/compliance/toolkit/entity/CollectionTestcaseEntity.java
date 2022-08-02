package io.mosip.compliance.toolkit.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Component
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "collection_testcase_mapping", schema = "toolkit")
public class CollectionTestcaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 480806380514910771L;

	@EmbeddedId
	private CollectionTestcaseKey ctm_id;

	@OneToOne(targetEntity = CollectionEntity.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "collection_id")
	private CollectionEntity collection;

	@OneToOne(targetEntity = TestCaseEntity.class, cascade = CascadeType.ALL)
	@JoinColumn(name = "testcase_id")
	private TestCaseEntity testcase;
}
