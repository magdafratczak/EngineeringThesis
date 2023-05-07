package com.pp.trenerpol.security;

import com.pp.trenerpol.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTTokenFilter extends OncePerRequestFilter {
	public static final String _BEARER = "Bearer ";
	private static final Logger logger = LoggerFactory.getLogger(JWTTokenFilter.class);
	@Autowired
	private UserService userService;

	@Autowired
	private JWTUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException, IOException {

		try {
			String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);


			if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(_BEARER)) {
				String jwtToken = headerAuth.substring(7);

				String email = jwtUtil.parseJWT(jwtToken).getSubject();

				UserDetails userDetails = userService.findUserByEmail(email);
				UsernamePasswordAuthenticationToken authentication =
						new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}

		} catch (Exception ex) {
			logger.error("Error authenticating user request : {}", ex.getMessage());
		}

		filterChain.doFilter(request, response);
	}
}
