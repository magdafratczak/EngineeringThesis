package com.pp.trenerpol.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;

@JsonIgnoreProperties({"hibernateLazyInitializer"})
@Entity
@Data
@Table(name = "training")
public class Training implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "training_start_date")
	private java.util.Date trainingStartDate;

	@Column(name = "modified_on")
	@CreationTimestamp
	private java.util.Date modifiedOn;

	@Column(name = "trainer_id")
	private Long trainerId;

	@Column(name = "protegee_id")
	private Long protegeId;

	@Column(name = "m_duration")
	private int mDuration;

	@Column(name = "training_end_date")
	private java.util.Date trainingEndDate;
}
