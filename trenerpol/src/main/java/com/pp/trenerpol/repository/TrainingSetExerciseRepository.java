package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.TrainingSetExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrainingSetExerciseRepository extends JpaRepository<TrainingSetExercise, Long> {
	List<TrainingSetExercise> findAllByTrainingId(Long id);

	@Query(value = "SELECT * FROM training_set_exercise " +
			"WHERE training_id in (SELECT id from training where protegee_id = ?1)", nativeQuery = true)
	List<TrainingSetExercise> findAllByTrainingIdForProtegee(@Param("id") Long id);

	@Query(value = "SELECT * FROM training_set_exercise " +
			"WHERE training_id in (SELECT id from training where trainer_id = ?1)", nativeQuery = true)
	List<TrainingSetExercise> findAllByTrainingIdForTrainer(@Param("id") Long id);

}
