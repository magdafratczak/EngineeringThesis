package com.pp.trenerpol.service;

import com.pp.trenerpol.model.User;
import com.pp.trenerpol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService, Serializable {

	@Autowired
	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userByEmail = Optional.ofNullable(userRepository.findByEmail(username));
		return userByEmail.orElseThrow(() -> new UsernameNotFoundException("User not found."));
	}

}
