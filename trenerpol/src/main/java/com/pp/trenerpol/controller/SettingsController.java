package com.pp.trenerpol.controller;

import com.pp.trenerpol.model.DTO.PasswordDTO;
import com.pp.trenerpol.model.User;
import com.pp.trenerpol.security.EmailVerificationToken;
import com.pp.trenerpol.security.GoogleTokenManager;
import com.pp.trenerpol.service.UserServiceImpl;
import com.pp.trenerpol.util.DateTimeFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://trenerpol.s3-website-eu-west-1.amazonaws.com/"})
@RestController
@RequestMapping("/settings")
public class SettingsController {
	private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

	@Autowired
	private UserServiceImpl userService;

	@Autowired
	private GoogleTokenManager googleTokenManager;

	/***
	 * update password by giving old password, new password and repetition of new password
	 * @param passwordDTO
	 * @return
	 */
	@PutMapping("/updatePassword")
	public ResponseEntity<?> changeUserPassword(@RequestBody PasswordDTO passwordDTO) {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Cannot retrieve user");
		}
		if (!userService.checkIfValidOldPassword(user, passwordDTO.getOldPassword())) {
			return ResponseEntity.badRequest()
					.body("Invalid old password");
		}
		Vector<String> errors = userService.validatePassword(passwordDTO.getNewPassword(), passwordDTO.getRepetition());
		if (!errors.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(errors.get(0));
		}
		userService.changeUserPassword(user, passwordDTO.getNewPassword());
		return ResponseEntity.ok()
				.body("User password successfully updated");
	}

	/***
	 * get GA qr code
	 * @return
	 */
	@GetMapping("/getGa")
	@ResponseBody
	public ResponseEntity<?> modifyUser2FA() {
		Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) curAuth.getPrincipal();
		Map<String, Object> body = new HashMap<>();
		String qr = googleTokenManager.generateQRUrl(currentUser);
		body.put("qr", qr);
		body.put("code", currentUser.getSecret());
		return ResponseEntity.ok().body(body);
	}

	@PutMapping("/checkCode")
	@ResponseBody
	public ResponseEntity<?> checkGaCode(@RequestParam("gaCode") String code, @RequestParam("isModeOn") boolean isModeOn) {
		Authentication curAuth = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) curAuth.getPrincipal();
		if (!userService.checkGACode(code)) {
			return ResponseEntity.badRequest()
					.body("Invalid verification code");
		}
		userService.updateUser2FA(isModeOn);
		if (!isModeOn) {
			return ResponseEntity.ok().body("2fa disabled");
		}
		return ResponseEntity.ok().body("2fa turned on");
	}

	/***
	 * delete user account, send email for confirmation
	 * @return
	 */
	@PostMapping("/delete")
	public ResponseEntity<?> deleteUser() {
		User user = userService.findUserByEmail(
				SecurityContextHolder.getContext().getAuthentication().getName());
		if (user == null) {
			return ResponseEntity.badRequest()
					.body("Cannot retrieve user");
		}
		String token = UUID.randomUUID().toString();
		userService.createVerificationTokenForUser(user, token);
		constructConfirmDeletionEmail(token, user);
		return ResponseEntity.ok()
				.body("Email for confirming account deletion has been sent");
	}

	/***
	 * delete user account after confirmation =  delete token from verification_token table
	 * set deletion date in table users - add +72 h and then CRON job will delete user from table
	 * @return
	 */
	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteUserAfterConfirmation(@RequestParam("token") String token) {
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
		userService.deleteVerificationToken(verificationToken);
		Date deletionDate = DateTimeFormatUtil.getDateFromLocalDateTime(DateTimeFormatUtil.getLocalDateTimeNow().plusHours(72));
		user.setDeletionDate(deletionDate);
		userService.saveUser(user);
		return ResponseEntity.ok()
				.body("User account will be deleted after 72 hours by CRON job");
	}

	/***
	 * construct email for confirmation of account deletion
	 * @param token
	 * @param user
	 * @return
	 */
	private SimpleMailMessage constructConfirmDeletionEmail(String token, User user) {
		String confirmationUrl = "http://localhost:3000/confirm-delete?token=" + token;
		SimpleMailMessage email = new SimpleMailMessage();
		String message = String.format("Hey %s,\n" +
				"Click below to confirm that you want to delete your account on TrenerPol service:", user.getName());
		email.setSubject("Deleting your account");
		email.setText(message + "\r\n" + confirmationUrl);
		email.setTo(user.getEmail());
		email.setFrom("trener.pol@outlook.com");
		logger.info(email.getText());
		return email;
	}
}
