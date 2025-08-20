package com.yuliana.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.yuliana.demo.service.JWTService;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

	ServerWebExchangeMatcher loginMatcher = ServerWebExchangeMatchers.pathMatchers("/api/auth/login");
	ServerWebExchangeMatcher registerMatcher = ServerWebExchangeMatchers.pathMatchers("/api/auth/register");

	OrServerWebExchangeMatcher orMatcher = new OrServerWebExchangeMatcher(loginMatcher, registerMatcher);

	NegatedServerWebExchangeMatcher negatedMatcher = new NegatedServerWebExchangeMatcher(orMatcher);

	private final JWTAuthConfig jwtAuthConverter;
	private final ReactiveAuthenticationManager authenticationManager;

	public SecurityConfig(JWTAuthConfig jwtAuthConverter, @Lazy ReactiveAuthenticationManager authenticationManager) {
		this.jwtAuthConverter = jwtAuthConverter;
		this.authenticationManager = authenticationManager;
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http.csrf(ServerHttpSecurity.CsrfSpec::disable).authorizeExchange(
				exchanges -> exchanges.pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
//	            .pathMatchers("/users/**").hasAuthority("ROLE_ADMIN")
						.pathMatchers("/users/**").authenticated()

						.anyExchange().authenticated())
				.addFilterAt(jwtAuthWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
				.securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // <- ESTA LÍNEA
				.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
				.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable).build();
	}

	@Bean
	public AuthenticationWebFilter jwtAuthWebFilter() {
		System.out.println(">>> Creating JWT Authentication Filter");

		AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);

		ServerWebExchangeMatcher loginMatcher = ServerWebExchangeMatchers.pathMatchers("/api/auth/login");
		ServerWebExchangeMatcher registerMatcher = ServerWebExchangeMatchers.pathMatchers("/api/auth/register");
		OrServerWebExchangeMatcher orMatcher = new OrServerWebExchangeMatcher(loginMatcher, registerMatcher);
		NegatedServerWebExchangeMatcher negatedMatcher = new NegatedServerWebExchangeMatcher(orMatcher);

		filter.setRequiresAuthenticationMatcher(negatedMatcher);

		filter.setServerAuthenticationConverter(jwtAuthConverter);

		filter.setAuthenticationSuccessHandler((webFilterExchange, authentication) -> {

			return webFilterExchange.getChain().filter(webFilterExchange.getExchange())
					.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
		});

		filter.setAuthenticationFailureHandler((webFilterExchange, exception) -> {

			return webFilterExchange.getExchange().getResponse().setComplete();
		});

		return filter;
	}

	@Bean
	public ReactiveAuthenticationManager reactiveAuthenticationManager(JWTService jwtService,
			ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		JwtReactiveAuthenticationManager jwtManager = new JwtReactiveAuthenticationManager(jwtService,
				userDetailsService);
		UserDetailsRepositoryReactiveAuthenticationManager userPassManager = new UserDetailsRepositoryReactiveAuthenticationManager(
				userDetailsService);
		userPassManager.setPasswordEncoder(passwordEncoder);

		return authentication -> {
			if (authentication instanceof UsernamePasswordAuthenticationToken && authentication.getCredentials() != null
					&& !(authentication.getCredentials().toString().contains("."))) {
				return userPassManager.authenticate(authentication);
			} else {
				return jwtManager.authenticate(authentication);
			}
		};
	}

}
