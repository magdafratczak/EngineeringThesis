package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.TrainerSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerScheduleRepository extends JpaRepository<TrainerSchedule, Long> {

	TrainerSchedule findByTrainerId(Long id);

}
