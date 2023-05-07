package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

	List<Training> findAllByTrainerId(Long id);

	List<Training> findAllByProtegeId(Long id);

	Training findByTrainerIdAndTrainingStartDate(Long id, Date date);

	Training findByProtegeIdAndTrainingStartDate(Long id, Date date);

	@Query(value = "SELECT * FROM training " +
			"WHERE trainer_id =?1 and CAST(training_start_date as date) = ?2", nativeQuery = true)
	List<Training> findAllByTrainerIdAndTrainingStartDateContaining(@Param("id") Long trainerId, @Param("date") Date date);


	List<Training> findAllByTrainerIdAndTrainingStartDateIsGreaterThanEqualAndTrainingStartDateIsLessThanEqual(Long id, Date from, Date to);

}
