package com.yuliana.demo.service;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yuliana.demo.controller.LoginRequest;
import com.yuliana.demo.controller.RegisterRequest;
import com.yuliana.demo.controller.TokenResponse;
import com.yuliana.demo.dto.UserRepository;
import com.yuliana.demo.model.Usuario;
import com.yuliana.demo.repository.Token;
import com.yuliana.demo.repository.TokenRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepo;
	private final TokenRepository tokenRepo;
	private final PasswordEncoder passwordEncoder;
	private final JWTService jwtService;
	private final ReactiveAuthenticationManager reactiveAuthManager;

	public AuthService(UserRepository userRepo, TokenRepository tokenRepo, PasswordEncoder passwordEncoder,
			JWTService jwtService, ReactiveAuthenticationManager reactiveAuthManager) {
		this.userRepo = userRepo;
		this.tokenRepo = tokenRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.reactiveAuthManager = reactiveAuthManager;
	}

	public Mono<TokenResponse> register(RegisterRequest request) {
		Usuario usuario = Usuario.builder().nombre(request.nombre()).email(request.email())
				.pasword(passwordEncoder.encode(request.password())).build();

		return userRepo.save(usuario).flatMap(savedUser -> {
			String jwtToken = jwtService.generateToken(savedUser);
			String refreshToken = jwtService.generateRefreshToken(savedUser);
			Token token = Token.builder().token(jwtToken).tokenType(Token.TokenType.BEARER).revoked(false)
					.expired(false).userId(savedUser.getId()).build();

			return tokenRepo.save(token).thenReturn(new TokenResponse(jwtToken, refreshToken));
		});

	}

	public Mono<TokenResponse> login(LoginRequest request) {
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(request.email(),
				request.password());

		return reactiveAuthManager.authenticate(authToken) // Mono<Authentication>
				.flatMap(authentication -> {
					Mono<Usuario> userMono = userRepo.findByEmail(request.email())
							.switchIfEmpty(Mono.error(new UsernameNotFoundException("Usuario no encontrado")));
					return userMono;
				}).flatMap(user -> {
					String jwtToken = jwtService.generateToken(user);
					String refreshToken = jwtService.generateRefreshToken(user);

					return revokeAllUserTokens(user).then(saveUserToken(user, jwtToken))
							.thenReturn(new TokenResponse(jwtToken, refreshToken));
				});
	}
	

	private Mono<Void> saveUserToken(Usuario user, String jwtToken) {
		Token token = Token.builder().token(jwtToken).tokenType(Token.TokenType.BEARER).expired(false).revoked(false)
				.build();

		return tokenRepo.save(token).then();
	}

	private Mono<Void> revokeAllUserTokens(Usuario user) {
		return tokenRepo.findAllByUserIdAndExpiredFalseAndRevokedFalse(user.getId()).collectList().flatMap(tokens -> {
			if (tokens.isEmpty()) {
				return Mono.empty();
			}

			tokens.forEach(token -> {
				token.setExpired(true);
				token.setRevoked(true);
			});

			return tokenRepo.saveAll(tokens).then();
		});
	}
	public Mono<TokenResponse> refreshToken(final String authHeader){
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        return Mono.error(new IllegalArgumentException("Invalid Bearer Token"));
    }

    final String refreshToken = authHeader.substring(7);
    final String userEmail;
    try {
        userEmail = jwtService.extractUsername(refreshToken);
    } catch (Exception e) {
        return Mono.error(new IllegalArgumentException("Invalid Refresh Token"));
    }

    return userRepo.findByEmail(userEmail)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException(userEmail)))
            .flatMap(user -> {
                if (!jwtService.isTokenValid(refreshToken, user)) {
                    return Mono.error(new IllegalArgumentException("Invalid Refresh Token"));
                }
                final String accessToken = jwtService.generateToken(user);
                return revokeAllUserTokens(user)
                        .then(saveUserToken(user, accessToken))
                        .thenReturn(new TokenResponse(accessToken, refreshToken));
            });
}


}
