package com.pp.trenerpol.security;

import com.pp.trenerpol.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtil {
	private static final String CLAIM_EMAIL_KEY = "email";
	private static final String CLAIM_NAME_KEY = "name";
	private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
	@Value("${auth.jwt.issuer}")
	private String issuer;
	@Value("${auth.jwt.secret}")
	private String secret;
	@Value("${auth.jwt.audience}")
	private String audience;
	@Value("${auth.jwt.ttl-in-seconds}")
	private long timeToLiveInSeconds;
	private SecretKey secretKey;

	@PostConstruct
	public void setUpSecretKey() {
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createJWT(User user) {

		return Jwts.builder()
				.setId(UUID.randomUUID().toString())
				.setSubject(user.getEmail())
				.setIssuer(issuer)
				.setIssuedAt(Date.from(Instant.now()))
				.setExpiration(Date.from(Instant.now().plus(
						Duration.ofSeconds(timeToLiveInSeconds))))
				.claim(CLAIM_EMAIL_KEY, user.getEmail())
				.claim(CLAIM_NAME_KEY, user.getName())
				.signWith(secretKey)
				.compact();
	}

	public Claims parseJWT(String jwtString) {

		Jws<Claims> headerClaimsJwt =
				Jwts.parserBuilder()
						.setSigningKey(secretKey)
						.build()
						.parseClaimsJws(jwtString);

		Claims claims = headerClaimsJwt.getBody();

		return claims;
	}

}
