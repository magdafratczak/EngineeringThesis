package com.pp.trenerpol.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "training_set_exercise")
public class TrainingSetExercise {
	@Column(name = "training_id")
	private Long trainingId;

	@Id
	@Column(name = "set_exercise_id")
	private Long setExerciseId;

	@Column(name = "ex_order")
	private int order;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "set_exercise_id", referencedColumnName = "id")
	private SetExercise exercise;
}
