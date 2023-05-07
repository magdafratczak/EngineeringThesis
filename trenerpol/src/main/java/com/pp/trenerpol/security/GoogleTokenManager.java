package com.pp.trenerpol.security;

import com.pp.trenerpol.model.User;

public interface GoogleTokenManager {
	String generateSecretKey();

	String generateQRUrl(User user);

	boolean verifyTotp(final String code, final String secret);
}
