package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

	Exercise findByName(String name);

	List<Exercise> findByUserId(Long id);

	void deleteByName(String name);

}
