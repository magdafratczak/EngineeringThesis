package com.pp.trenerpol.repository;

import com.pp.trenerpol.security.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	PasswordResetToken save(PasswordResetToken token);

	PasswordResetToken findByToken(String token);

}
