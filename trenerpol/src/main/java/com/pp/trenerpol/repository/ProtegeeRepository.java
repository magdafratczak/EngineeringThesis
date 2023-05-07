package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.Protegee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProtegeeRepository extends JpaRepository<Protegee, Long> {
}
