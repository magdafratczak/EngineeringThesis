package com.pp.trenerpol.service;

import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.DTO.TrainingPlanDTO;
import com.pp.trenerpol.model.Training;
import com.pp.trenerpol.model.TrainingSetExercise;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;


public interface TrainingService {
	Training createTraining(Training training);

	Training updateTraining(Training training);

	void cancelTraining(Long trainingId);

	Training getTrainingById(Long trainingId);

	Training getTrainingInfoByTrainerIdAndDate(Long trainerId, Date date);

	Training getTrainingInfoByProtegeeIdAndDate(Long protegeeId, Date date);

	void createTrainingPlan(TrainingPlanDTO dto);

	List<TrainingSetExercise> getTrainingPlan(Long trainingId);

	List<TrainingSetExercise> getTrainingPlansByProtegeeId(Long protegeeId);

	List<TrainingSetExercise> getTrainingPlansByTrainerId(Long trainerId);

	void deleteTrainingPlan(Long trainingId);

	void updateTrainingPlan(Long trainingId, TrainingPlanDTO dto);

	List<Training> getTrainingsByTrainerId(Long id);

	List<Training> getTrainingsByTrainerIdBetweenDates(Long id, Date from, Date to);

	List<Training> getTrainingsByTrainerIdFromDay(Long id, Date date);

	List<Training> getTrainingsByProtegeeId(Long id);

	void saveProtegeeMetricsPerTraining(ProtegeeMeasurementDTO dto);

	ByteArrayInputStream generateTrainingPlanAsExcelFile(Long trainingId, String fileName) throws IOException;
}
