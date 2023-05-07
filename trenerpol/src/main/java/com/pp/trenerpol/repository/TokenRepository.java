package com.pp.trenerpol.repository;

import com.pp.trenerpol.security.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	EmailVerificationToken findByToken(String verificationToken);

}
