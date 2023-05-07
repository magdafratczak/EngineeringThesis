package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.TrainerScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TrainerScheduleExceptionRepository extends JpaRepository<TrainerScheduleException, Long> {

	List<TrainerScheduleException> findAllByTrainerIdAndDateIsGreaterThanEqualAndModifiedDateIsLessThanEqual(Long id, Date from, Date to);

}
