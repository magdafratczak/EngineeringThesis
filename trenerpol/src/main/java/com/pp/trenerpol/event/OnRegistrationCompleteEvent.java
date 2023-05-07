package com.pp.trenerpol.event;

import com.pp.trenerpol.model.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent {
	private String appUrl;
	private Locale locale;
	private User user;
	private boolean isInvite;

	public OnRegistrationCompleteEvent(User user, Locale locale, String appUrl, boolean isInvite) {
		super(user);
		this.user = user;
		this.locale = locale;
		this.appUrl = appUrl;
		this.isInvite = isInvite;
	}

}
