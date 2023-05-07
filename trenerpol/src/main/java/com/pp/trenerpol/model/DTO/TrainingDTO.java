package com.pp.trenerpol.model.DTO;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
public class TrainingDTO {
	private Long id;
	private Long protegeId;
	private Long trainerId;
	@NotNull
	private LocalDateTime trainingStartDate;
	@NotNull
	private int mDuration;
	private LocalDateTime trainingEndDate;

}
