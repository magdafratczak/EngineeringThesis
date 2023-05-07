package com.pp.trenerpol.repository;

import com.pp.trenerpol.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByEmail(String email);

	Boolean existsByEmail(String email);

}
