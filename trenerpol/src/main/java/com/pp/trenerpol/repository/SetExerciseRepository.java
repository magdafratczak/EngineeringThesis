package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.SetExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetExerciseRepository extends JpaRepository<SetExercise, Long> {
}
