package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.ProtegeeMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProtegeeMeasurementRepository extends JpaRepository<ProtegeeMeasurement, Long> {

	List<ProtegeeMeasurement> findAllByProtegeeId(Long id);

	ProtegeeMeasurement findByProtegeeIdAndCreationDate(Long id, Date date);

}
