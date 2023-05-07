package com.pp.trenerpol.service;

import com.pp.trenerpol.model.DTO.TrainerDTO;
import com.pp.trenerpol.model.Trainer;
import com.pp.trenerpol.model.TrainerSchedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public interface TrainerService {

	void initDefaultSchedule(Long id);

	boolean createTrainerSchedule(Long id, String[][] schedule);

	boolean createTrainerScheduleException(Long id, String[][] exception);

	Trainer getTrainer(Long id);

	ArrayList<String[]> getTrainerSchedule(Long id, Date from, Date to);

	boolean isTrainerScheduleCreated(Long id);

	TrainerSchedule getTrainerSchedule(Long id);

	boolean updateTrainerData(Trainer trainer, TrainerDTO dto);

	void updateTrainerSchedule(TrainerSchedule trainerSchedule);

	List<Trainer> getAllTrainers();
}
