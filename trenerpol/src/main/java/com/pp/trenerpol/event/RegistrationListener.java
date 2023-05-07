package com.pp.trenerpol.event;

import com.pp.trenerpol.model.User;
import com.pp.trenerpol.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

	@Autowired
	private UserService service;

	@Autowired
	private JavaMailSender mailSender;

	@Override
	public void onApplicationEvent(OnRegistrationCompleteEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnRegistrationCompleteEvent event) {
		User user = event.getUser();
		String token = UUID.randomUUID().toString();
		service.createVerificationTokenForUser(user, token);
		String confirmationUrl = StringUtils.EMPTY;
		String message = StringUtils.EMPTY;
		String subject = StringUtils.EMPTY;

		if (event.isInvite()) {
			confirmationUrl = "http://trenerpol.s3-website-eu-west-1.amazonaws.com/set-up-User?token=" + token;
			message = String.format("Hey %s,\n" +
					"You've been invited by your new Trainer to join our platform TrenerPol." +
					" Click below to finish setting up your account:", user.getName());
			subject = "TrenerPol Invite";
		} else {
			confirmationUrl = "http://trenerpol.s3-website-eu-west-1.amazonaws.com/confirm-email?token=" + token;
			message = String.format("Hey %s,\n" +
					"Thanks for registering for an account on TrenerPol!" +
					" Before we get started, we just need to confirm that this is you." +
					" Click below to verify your email address:", user.getName());
			subject = "TrenerPol Confirm Registration";
		}

		SimpleMailMessage email = new SimpleMailMessage();
		email.setFrom("trener.pol@outlook.com");
		email.setTo(user.getEmail());
		email.setSubject(subject);
		email.setText(message + "\r\n" + confirmationUrl);
		mailSender.send(email);
	}

}
