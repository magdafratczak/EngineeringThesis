package com.pp.trenerpol.security;

import com.pp.trenerpol.model.User;
import dev.samstevens.totp.code.CodeVerifier;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service("googleTokenManager")
public class DefaultGoogleTokenManager implements GoogleTokenManager {
	private static final Object APP_NAME = "trenerpol";
	public static String QR_PREFIX =
			"https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";

	@Resource
	private CodeVerifier codeVerifier;

	@Override
	public String generateSecretKey() {
		return Base32.random();
	}

	@Override
	public String generateQRUrl(User user) {
		return QR_PREFIX + URLEncoder.encode(String.format(
				"otpauth://totp/%s:%s?secret=%s&issuer=%s",
				APP_NAME, user.getEmail(), user.getSecret(), APP_NAME),
				StandardCharsets.UTF_8);
	}

	@Override
	public boolean verifyTotp(String code, String secret) {
		return codeVerifier.isValidCode(secret, code);
	}
}
