package com.yuliana.demo.config;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

import com.yuliana.demo.model.Usuario;
import com.yuliana.demo.service.AuthService;
import com.yuliana.demo.service.JWTService;

import reactor.core.publisher.Mono;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private final JWTService jwtService;

	private final ReactiveUserDetailsService userDetailsService;

	public JwtReactiveAuthenticationManager(JWTService jwtService, ReactiveUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {

			return Mono.error(new BadCredentialsException("Unsupported authentication type"));
		}

		Object credentials = authentication.getCredentials();

		if (credentials == null) {

			return Mono.error(new BadCredentialsException("No JWT token found"));
		}

		String token = credentials.toString();

		if (token.split("\\.").length != 3) {

			return Mono.error(new BadCredentialsException("Invalid JWT token format"));
		}

		String username = jwtService.extractUsername(token);
		if (username == null) {

			return Mono.error(new BadCredentialsException("Cannot extract username from token"));
		}

		return userDetailsService.findByUsername(username).flatMap(userDetails -> {
			if (jwtService.isTokenValid(token, userDetails)) {
				return Mono.just(
						new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities()));
			} else {
				return Mono.error(new BadCredentialsException("Invalid JWT token"));
			}
		});

	}

}
