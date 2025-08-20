package com.yuliana.demo.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.yuliana.demo.dto.UserRepository;
import com.yuliana.demo.repository.TokenRepository;
import com.yuliana.demo.service.JWTService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JWTAuthConfig implements ServerAuthenticationConverter {

	private final JWTService jwtService;
	private final UserRepository userRepository;
	private final TokenRepository tokenRepository;

	public JWTAuthConfig(JWTService jwtService, UserRepository userRepository, TokenRepository tokenRepository) {
		super();
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.tokenRepository = tokenRepository;
	}

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {

		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			return Mono.empty();
		}

		String jwtToken = authHeader.substring(7);

		String userEmail;
		try {
			userEmail = jwtService.extractUsername(jwtToken);

		} catch (Exception e) {

			return Mono.empty();
		}

		return userRepository.findByEmail(userEmail)
				.doOnNext(user -> System.out.println(">>> User found in DB: " + user.getEmail()))
				.flatMap(user -> tokenRepository.findByToken(jwtToken)
						.doOnNext(token -> System.out.println(
								">>> Token status - expired: " + token.isExpired() + ", revoked: " + token.isRevoked()))
						.filter(token -> !token.isExpired() && !token.isRevoked()).flatMap(token -> {
							boolean isValid = jwtService.isTokenValid(jwtToken, user);

							if (!isValid) {
								System.out.println(">>> Token is invalid");
								return Mono.empty();
							}

							UserDetails userDetails = org.springframework.security.core.userdetails.User
									.withUsername(user.getEmail()).password(user.getPassword())

									.roles(user.getRole().toUpperCase()).build();

							Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
									jwtToken, userDetails.getAuthorities());

							return Mono.just(authentication);
						}).switchIfEmpty(Mono.defer(() -> {

							return Mono.empty();
						})))
				.switchIfEmpty(Mono.defer(() -> {

					return Mono.empty();
				}));
	}

}
