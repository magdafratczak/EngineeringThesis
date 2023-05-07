package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TrainingPlanDTO {
	@NotNull
	private Long trainingId;
	@NotNull
	private List<SetExerciseDTO> setExerciseList;
}
