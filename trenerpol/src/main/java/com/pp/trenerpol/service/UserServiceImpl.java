package com.pp.trenerpol.service;

import com.pp.trenerpol.event.OnRegistrationCompleteEvent;
import com.pp.trenerpol.exception.EntityNotFoundException;
import com.pp.trenerpol.model.DTO.ProtegeeDTO;
import com.pp.trenerpol.model.DTO.ProtegeeMeasurementDTO;
import com.pp.trenerpol.model.DTO.SignUpDTO;
import com.pp.trenerpol.model.DTO.SignUpUserDTO;
import com.pp.trenerpol.model.*;
import com.pp.trenerpol.model.enums.Sex;
import com.pp.trenerpol.model.enums.UserType;
import com.pp.trenerpol.repository.*;
import com.pp.trenerpol.security.EmailVerificationToken;
import com.pp.trenerpol.security.GoogleTokenManager;
import com.pp.trenerpol.security.PasswordResetToken;
import com.pp.trenerpol.util.Util;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TokenRepository tokenRepository;

	@Autowired
	private PasswordTokenRepository passwordTokenRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ProtegeeMeasurementRepository protegeeMeasurementRepository;

	@Autowired
	private ProtegeeRepository protegeeRepository;

	@Autowired
	private TrainerRepository trainerRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private GoogleTokenManager googleTokenManager;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	/**
	 * Must have at least one numeric character
	 * Must have at least one lowercase character
	 * Must have at least one uppercase character
	 * Must have at least one special symbol among @#$%
	 * Password length should be between 8 and 20
	 */
	public static boolean isValidPassword(String password) {
		String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*#?&]).{8,20}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);
		return matcher.matches();
	}

	@Override
	public void saveUser(final User user) {
		userRepository.save(user);
	}

	@Override
	public User findUserByEmail(final String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public boolean checkIfValidOldPassword(final User user, final String oldPassword) {
		return passwordEncoder.matches(oldPassword, user.getPassword());
	}

	@Override
	public Vector<String> validatePassword(String password, String repetition) {
		Vector<String> errors = new Vector<>();
		if (!isValidPassword(password)) {
			errors.add("Given password does not meet the requirements!");
		}
		if (!password.equals(repetition)) {
			errors.add("Passwords do not match");
		}
		return errors;
	}

	@Override
	public void changeUserPassword(final User user, final String password) {
		user.setPassword(passwordEncoder.encode(password));
		userRepository.save(user);
	}

	@Override
	public User getUser(String verificationToken) {
		return tokenRepository.findByToken(verificationToken).getUser();
	}

	@Override
	public void createVerificationTokenForUser(User user, String token) {
		EmailVerificationToken myToken = new EmailVerificationToken(token, user);
		tokenRepository.save(myToken);
	}

	@Override
	public EmailVerificationToken getVerificationToken(final String verificationToken) {
		return tokenRepository.findByToken(verificationToken);
	}

	@Override
	public void deleteVerificationToken(final EmailVerificationToken verificationToken) {
		tokenRepository.delete(verificationToken);
	}

	@Override
	public void saveProtegeeMeasurements(Long id, ProtegeeMeasurementDTO dto) {
		ProtegeeMeasurement pm = protegeeMeasurementRepository.findByProtegeeIdAndCreationDate(id, new Date());

		if (pm == null) {
			pm = new ProtegeeMeasurement();
			pm.setProtegeeId(id);
		}
		//default values in table = 0.0, so get only greater
		if (dto.getBicep() > 0.0) {
			pm.setBicep(dto.getBicep());
		}
		if (dto.getCalve() > 0.0) {
			pm.setCalve(dto.getCalve());
		}
		if (dto.getChest() > 0.0) {
			pm.setChest(dto.getChest());
		}
		if (dto.getHips() > 0.0) {
			pm.setHips(dto.getHips());
		}
		if (dto.getThigh() > 0.0) {
			pm.setThigh(dto.getThigh());
		}
		if (dto.getWeight() > 0.0) {
			pm.setWeight(dto.getWeight());
		}
		if (dto.getWaist() > 0.0) {
			pm.setWaist(dto.getWaist());
		}
		if (dto.getTrainingId() != null && dto.getTrainingId() > 0.0) {
			pm.setTrainingId(dto.getTrainingId());
		}

		protegeeMeasurementRepository.save(pm);
	}

	@Override
	public void editProtegeeData(Protegee protegee, ProtegeeDTO dto) {

		if (!dto.getName().isEmpty()) {
			protegee.setName(dto.getName());
		}

		if (!dto.getSurname().isEmpty()) {
			protegee.setSurname(dto.getSurname());
		}

		if (!dto.getPhoneNumber().isEmpty()) {
			String phoneNumber = dto.getPhoneNumber().trim().replace(" ", "").replace("-", "");
			protegee.setPhoneNum(phoneNumber);
		}

		if (!dto.getSex().isEmpty()) {
			String sex = Sex.NONE.name();
			if (dto.getSex().equalsIgnoreCase("woman") || dto.getSex().equalsIgnoreCase("w")) {
				sex = Sex.WOMAN.name();
			}
			if (dto.getSex().equalsIgnoreCase("man") || dto.getSex().equalsIgnoreCase("m")) {
				sex = Sex.MAN.name();
			}
			protegee.setSex(sex);
		}

		if (dto.getHeight() > 0) {
			protegee.setHeight(dto.getHeight());
		}
		if (dto.getBirthDay() != null && dto.getBirthDay().before(new Date())) {
			protegee.setBirthDate(dto.getBirthDay());
		}

		protegeeRepository.save(protegee);
	}

	@Override
	public User getLoggedInUser() {
		Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
		return (User) curAuth.getPrincipal();
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public User getUserById(Long id) {
		Optional<User> user = userRepository.findById(id);

		if (user.isEmpty()) {
			logger.error("User not found: id-{}", id);
			throw new EntityNotFoundException("User not found: id-" + id);
		}

		return user.get();
	}

	@Override
	public void deleteUserById(Long id) {
		userRepository.deleteById(id);
		logger.info("User deleted: id-{}", id);
	}

	@Override
	public void inviteProtegee(SignUpUserDTO signUpUserDTO, HttpServletRequest request) {
		Protegee protegee = new Protegee();
		protegee.setName(signUpUserDTO.getName());
		protegee.setSurname(signUpUserDTO.getSurname());
		protegee.setEmail(signUpUserDTO.getEmail());
		protegee.setType(UserType.PROTEGEE.toString());
		Optional<Role> roles = roleRepository.findByName("user");
		if (roles.isPresent()) {
			protegee.setRoles(Collections.singleton(roles.get()));
		}
		protegee.setSecret(googleTokenManager.generateSecretKey());
		protegeeRepository.save(protegee);

		try {
			String appUrl = request.getContextPath();
			eventPublisher.publishEvent(new OnRegistrationCompleteEvent(protegee,
					request.getLocale(), appUrl, true));
		} catch (Exception ex) {
			logger.error(String.valueOf(ex));
		}
	}

	@Override
	public void signUpUser(SignUpDTO signUpDto, HttpServletRequest request) {
		Role roles = roleRepository.findByName("user").get();
		User user = null;
		if (signUpDto.getType().equalsIgnoreCase("T") || signUpDto.getType().equalsIgnoreCase("Trainer")) {
			Trainer trainer = new Trainer();
			trainer.setName(signUpDto.getName());
			trainer.setSurname(signUpDto.getSurname());
			trainer.setEmail(signUpDto.getEmail());
			trainer.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
			trainer.setType(UserType.TRAINER.toString());
			trainer.setRoles(Collections.singleton(roles));
			trainer.setSecret(googleTokenManager.generateSecretKey());
			trainerRepository.save(trainer);
			user = trainer;
		} else {
			Protegee protegee = new Protegee();
			protegee.setName(signUpDto.getName());
			protegee.setSurname(signUpDto.getSurname());
			protegee.setEmail(signUpDto.getEmail());
			protegee.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
			protegee.setType(UserType.PROTEGEE.toString());
			protegee.setRoles(Collections.singleton(roles));
			protegee.setSecret(googleTokenManager.generateSecretKey());
			protegeeRepository.save(protegee);
			user = protegee;
		}

		try {
			String appUrl = request.getContextPath();
			eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user,
					request.getLocale(), appUrl, false));
		} catch (Exception ex) {
			logger.error(String.valueOf(ex));
		}
	}

	@Override
	public boolean checkGACode(String code) {
		Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) curAuth.getPrincipal();
		Totp totp = new Totp(currentUser.getSecret());
		if (!Util.isValidLong(code) || !totp.verify(code)) {
			return false;
		}
		return true;
	}

	@Override
	public EmailVerificationToken generateNewVerificationToken(final String existingVerificationToken) {
		EmailVerificationToken vToken = tokenRepository.findByToken(existingVerificationToken);
		vToken.updateToken(UUID.randomUUID()
				.toString());
		vToken = tokenRepository.save(vToken);
		return vToken;
	}

	@Override
	public void createPasswordResetTokenForUser(User user, String token) {
		PasswordResetToken myToken = new PasswordResetToken(token, user);
		passwordTokenRepository.save(myToken);
	}

	@Override
	public String validatePasswordResetToken(String token) {
		final PasswordResetToken passToken = passwordTokenRepository.findByToken(token);

		return !isTokenFound(passToken) ? "invalidToken"
				: isTokenExpired(passToken) ? "expired"
				: null;
	}

	@Override
	public Optional<User> getUserByPasswordResetToken(final String token) {
		return Optional.ofNullable(passwordTokenRepository.findByToken(token).getUser());
	}

	private boolean isTokenFound(PasswordResetToken passToken) {
		return passToken != null;
	}

	private boolean isTokenExpired(PasswordResetToken passToken) {
		final Calendar cal = Calendar.getInstance();
		return passToken.getExpiryDate().before(cal.getTime());
	}

	@Override
	public User updateUser2FA(boolean use2FA) {
		Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) curAuth.getPrincipal();
		currentUser.setEnabled2fa(use2FA);
		currentUser = userRepository.save(currentUser);

		Authentication auth = new UsernamePasswordAuthenticationToken(
				currentUser, currentUser.getPassword(), curAuth.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		return currentUser;
	}

}
