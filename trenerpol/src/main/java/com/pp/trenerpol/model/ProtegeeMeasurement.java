package com.pp.trenerpol.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

@Data
@Entity
@Table(name = "protegee_measurement")
public class ProtegeeMeasurement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "protegee_id")
	private Long protegeeId;

	@Column(name = "training_id")
	private Long trainingId;

	@Column(name = "creation_date")
	@CreationTimestamp
	@Temporal(TemporalType.DATE)
	private java.util.Date creationDate;

	@Column(name = "weight")
	private double weight;

	@Column(name = "thigh")
	private double thigh;

	@Column(name = "hips")
	private double hips;

	@Column(name = "calve")
	private double calve;

	@Column(name = "waist")
	private double waist;

	@Column(name = "bicep")
	private double bicep;

	@Column(name = "chest")
	private double chest;

}
