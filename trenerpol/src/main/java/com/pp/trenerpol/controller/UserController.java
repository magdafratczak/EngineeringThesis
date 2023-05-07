package com.pp.trenerpol.controller;

import com.pp.trenerpol.model.DTO.PasswordDTO;
import com.pp.trenerpol.model.DTO.SignInDTO;
import com.pp.trenerpol.model.DTO.SignInWithTokenDTO;
import com.pp.trenerpol.model.DTO.SignUpDTO;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.security.EmailVerificationToken;
import com.pp.trenerpol.security.JWTUtil;
import com.pp.trenerpol.service.TrainerService;
import com.pp.trenerpol.service.UserService;
import com.pp.trenerpol.util.Util;
import org.jboss.aerogear.security.otp.Totp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/user")
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Autowired
	private UserService userService;

	@Autowired
	private TrainerService trainerService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JWTUtil jwtUtil;

	@Autowired
	private JavaMailSender javaMailSender;

	@GetMapping("")
	public List<User> retrieveAllUsers() {
		return userService.getAllUsers();
	}

	@GetMapping("/{id}")
	public User retrieveUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@GetMapping("/in")
	public User retrieveLoggedInUser() {
		return userService.getLoggedInUser();
	}

	@DeleteMapping("/{id}")
	public void deleteUser(@PathVariable Long id) {
		userService.deleteUserById(id);
	}

	/***
	 * registration and sending email for confirmation via OnRegistrationCompleteEvent
	 * @param signUpDto
	 * @param request
	 * @return
	 */
	@PostMapping("auth/signup")
	public ResponseEntity<?> registerUser(@RequestBody @Valid SignUpDTO signUpDto, HttpServletRequest request) {

		if (userService.existsByEmail(signUpDto.getEmail())) {
			return ResponseEntity.badRequest()
					.body("Email is already taken!");
		}

		Vector<String> errors = userService.validatePassword(signUpDto.getPassword(), signUpDto.getRepeatPassword());
		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(errors.get(0));
		}
		userService.signUpUser(signUpDto, request);
		return ResponseEntity.ok()
				.body("User registered successfully");
	}

	/***
	 * confirm user registration
	 * @param token
	 * @return
	 */
	@GetMapping("auth/registrationConfirm")
	@ResponseBody
	public ResponseEntity<?> confirmRegistration(@RequestParam("token") String token) {
		EmailVerificationToken verificationToken = userService.getVerificationToken(token);
		if (verificationToken == null) {
			return ResponseEntity.badRequest()
					.body("Bad user - invalid token");
		}
		User user = verificationToken.getUser();
		Calendar cal = Calendar.getInstance();
		if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
			return ResponseEntity.badRequest()
					.body("Token has expired");
		}
		user.setEnabled(true);
		userService.saveUser(user);
		userService.deleteVerificationToken(verificationToken);
		if (user.getType().equalsIgnoreCase("T")) {
			trainerService.initDefaultSchedule(user.getId());
		}
		return ResponseEntity.ok()
				.body("Registration has been confirmed, user enabled");
	}

	/***
	 * resend email confirmation after registration and expiring previous token
	 * @param request
	 * @param existingToken
	 * @return
	 */
	@GetMapping("auth/resendRegistrationToken")
	public ResponseEntity<?> resendRegistrationToken(
			HttpServletRequest request, @RequestParam("token") String existingToken) {
		EmailVerificationToken newToken = userService.generateNewVerificationToken(existingToken);

		User user = userService.getUser(newToken.getToken());
		String appUrl =
				"http://" + request.getServerName() +
						":" + request.getServerPort() +
						request.getContextPath();
		SimpleMailMessage email = constructResendVerificationTokenEmail(appUrl, newToken, user);
		javaMailSender.send(email);

		return ResponseEntity.ok()
				.body("Registration token has been resent");

	}

	/***
	 * set up new user after trainer's invite
	 * @param token
	 * @return
	 */
	@PostMapping("auth/setUpNewUser")
	public ResponseEntity<?> setUpNewUser(@RequestParam("token") String token) {
		EmailVerificationToken verificationToken = userService.getVerificationToken(token);
		if (verificationToken == null) {
			return ResponseEntity.badRequest()
					.body("invalid token");
		}
		User user = verificationToken.getUser();
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Couldn't find user based on token");
		}
		user.setEnabled(true);
		userService.saveUser(user);
		userService.deleteVerificationToken(verificationToken);
		String passwordToken = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, passwordToken);
		return ResponseEntity.ok()
				.body("user has been enabled. redirect me to password page with token: " + passwordToken);
	}

	/***
	 * login
	 * @param signInDTO
	 * @return
	 */
	@PostMapping("auth/signin")
	public Object checkCredentials(@RequestBody @Valid SignInDTO signInDTO) {
		UsernamePasswordAuthenticationToken loginCredentials =
				new UsernamePasswordAuthenticationToken(
						signInDTO.getEmail(), signInDTO.getPassword());

		Authentication authentication = authenticationManager.authenticate(loginCredentials); //jak user enabled==false to spring rzuca wyjatkiem
		User user = (User) authentication.getPrincipal();
		Map json = new HashMap();

		if (user != null) {
			if (user.isEnabled2fa()) {
				json.put("token", null);
				json.put("twoFaEnabled", true);
			} else {
				String jwtToken = jwtUtil.createJWT(user);
				json.put("token", jwtToken);
				json.put("twoFaEnabled", false);
			}
			return json;
		}
		return ResponseEntity.badRequest()
				.body("Failed sign-in");
	}

	@PostMapping("auth/checkPwd")
	public Object checkPassword(@RequestParam("pwd") String password) {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Cannot retrieve user");
		}
		if (userService.checkIfValidOldPassword(user, password)) {
			return ResponseEntity.ok().body("Given password is correct");
		}
		return ResponseEntity.badRequest()
				.body("Given password is incorrect");
	}


	/***
	 * pass GA code for users with enabled 2f
	 * @param signInDTO
	 * @return
	 */
	@PostMapping("auth/gacode")
	public ResponseEntity<?> authenticateUser(@RequestBody @Valid SignInWithTokenDTO signInDTO) {
		User user = userService.findUserByEmail(signInDTO.getEmail());

		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Failed sign-in. User not found");
		}
		Totp totp = new Totp(user.getSecret());
		if (!Util.isValidLong(signInDTO.getCode()) || !totp.verify(signInDTO.getCode())) {
			logger.error("Invalid verification code" + signInDTO.getCode());
			return ResponseEntity.badRequest()
					.body("Invalid verification code");
		}
		String jwtToken = jwtUtil.createJWT(user);
		return ResponseEntity.ok()
				.body(jwtToken);

	}

	/***
	 * reset password from login page
	 * @param request
	 * @param userEmail
	 * @return
	 */
	@PostMapping("auth/user/resetPassword")
	public ResponseEntity<?> resetPassword(HttpServletRequest request,
	                                       @RequestParam("email") String userEmail) {
		User user = userService.findUserByEmail(userEmail);
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("User not found");
		}
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(user, token);
		String appUrl =
				"http://" + request.getServerName() +
						":" + request.getServerPort() +
						request.getContextPath();
		SimpleMailMessage email = constructResetPasswordEmail(appUrl, token, user);
		javaMailSender.send(email);
		return ResponseEntity.ok()
				.body("Email with link to resetting password has been sent.");
	}

	/***
	 * change password after clicking on reset password option
	 * @param token
	 * @return
	 */
	@GetMapping("auth/changePassword")
	public ResponseEntity<?> showChangePasswordPage(@RequestParam("token") String token) {
		String result = userService.validatePasswordResetToken(token);
		if (result != null) {
			return ResponseEntity.badRequest()
					.body("Invalid token. Go back to login page");
		}
		return ResponseEntity.ok()
				.body("Redirect me to resetting password page with this token: " + token);
	}

	/***
	 * saving new password after resetting, token is needed and new password
	 * @param passwordDto
	 * @return
	 */
	@PostMapping("auth/user/saveResetPassword")
	public ResponseEntity<?> savePassword(@RequestBody PasswordDTO passwordDto) {

		String result = userService.validatePasswordResetToken(passwordDto.getToken());

		if (result != null) {
			return ResponseEntity.badRequest()
					.body("invalid token");
		}

		Optional user = userService.getUserByPasswordResetToken(passwordDto.getToken());
		if (user.isPresent()) {
			userService.changeUserPassword((User) user.get(), passwordDto.getNewPassword());
			return ResponseEntity.ok()
					.body("Password has been reset");

		} else {
			return ResponseEntity.badRequest()
					.body("user not found by pass token");
		}
	}

	@GetMapping(value = "/logout")
	public ResponseEntity<String> logoutPage(HttpServletRequest request, HttpServletResponse response) {
		//usuniecie jwt z local storage na froncie
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return ResponseEntity.ok()
				.body("Successfull logout");
	}

	/***
	 * construct email for registration verification
	 * @param contextPath
	 * @param newToken
	 * @param user
	 * @return
	 */
	private SimpleMailMessage constructResendVerificationTokenEmail
	(String contextPath, EmailVerificationToken newToken, User user) {
		String confirmationUrl = "http://localhost:3000/confirm-email?token=" + newToken.getToken();
		SimpleMailMessage email = new SimpleMailMessage();
		String message = String.format("Hey %s,\n" +
				"Click below to verify your email address:", user.getName());
		email.setSubject("Resending Registration Token");
		email.setText(message + "\r\n" + confirmationUrl);
		email.setTo(user.getEmail());
		return email;
	}

	/***
	 * construct email for resetting password
	 * @param contextPath
	 * @param newToken
	 * @param user
	 * @return
	 */
	private SimpleMailMessage constructResetPasswordEmail
	(String contextPath, String newToken, User user) {
		String confirmationUrl = "http://localhost:3000/reset-password?token=" + newToken;
		SimpleMailMessage email = new SimpleMailMessage();
		String message = String.format("Hey %s,\n" +
				"Click below to reset your password:", user.getName());
		email.setSubject("Resetting your password");
		email.setText(message + "\r\n" + confirmationUrl);
		email.setTo(user.getEmail());
		email.setFrom("trener.pol@outlook.com");
		return email;
	}

}
