package com.pp.trenerpol.model.DTO;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetExerciseDTO {
	@NotNull
	private Long repetition;
	@NotNull
	private Long load;
	@NotNull
	private Long exerciseId;


}
