package com.yuliana.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.yuliana.demo.dto.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {
	private final UserRepository userRepository;

	public AppConfig(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Bean
	public ReactiveUserDetailsService userDetailsService() {
		return username -> userRepository.findByEmail(username)
				.switchIfEmpty(Mono.error(new UsernameNotFoundException("user not found")))
				.map(user -> User.withUsername(user.getEmail()).password(user.getPassword()).roles("usuario").build());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
//				.authorizeExchange(exchanges -> exchanges.pathMatchers("/api/auth/login", "/api/auth/register")
//						.permitAll().pathMatchers("/users/**").authenticated().anyExchange().authenticated())
//				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).build();
//	}

//	@Bean
//	public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService,
//			PasswordEncoder passwordEncoder) {
//
//		var authManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
//		authManager.setPasswordEncoder(passwordEncoder);
//		return authManager;
//	}

}
