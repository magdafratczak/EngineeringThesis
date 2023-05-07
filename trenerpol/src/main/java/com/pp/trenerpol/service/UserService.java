package com.pp.trenerpol.service;

import com.pp.trenerpol.model.DTO.ProtegeeDTO;
import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.DTO.SignUpDTO;
import com.pp.trenerpol.model.DTO.SignUpUserDTO;
import com.pp.trenerpol.model.Protegee;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.security.EmailVerificationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

public interface UserService {
	void saveUser(final User user);

	User findUserByEmail(final String email);

	boolean existsByEmail(final String email);

	void changeUserPassword(final User user, final String password);

	boolean checkIfValidOldPassword(final User user, final String oldPassword);

	Vector<String> validatePassword(final String password, final String repetition);

	User getUser(String verificationToken);

	void createVerificationTokenForUser(User user, String token);

	EmailVerificationToken getVerificationToken(String VerificationToken);

	EmailVerificationToken generateNewVerificationToken(final String existingVerificationToken);

	void createPasswordResetTokenForUser(User user, String token);

	String validatePasswordResetToken(String token);

	Optional<User> getUserByPasswordResetToken(String token);

	User updateUser2FA(boolean use2FA);

	void deleteVerificationToken(EmailVerificationToken verificationToken);

	void saveProtegeeMeasurements(Long id, ProtegeeMeasurementDTO dto);

	void editProtegeeData(Protegee protegee, ProtegeeDTO dto);

	User getLoggedInUser();

	List<User> getAllUsers();

	User getUserById(Long id);

	void deleteUserById(Long id);

	void inviteProtegee(SignUpUserDTO signUpUserDTO, HttpServletRequest request);

	void signUpUser(SignUpDTO signUpDto, HttpServletRequest request);

	boolean checkGACode(String code);
}
