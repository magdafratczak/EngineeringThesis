package com.pp.trenerpol.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "set_exercise")
public class SetExercise {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "repetition")
	private Long repetition;

	@Column(name = "load")
	private Long load;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "exercise_id", referencedColumnName = "id")
	private Exercise exercise;
}
