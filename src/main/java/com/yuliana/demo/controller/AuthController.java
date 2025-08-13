package com.yuliana.demo.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yuliana.demo.service.AuthService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService service;
	private final AuthenticationManager authManager;
	
	
	public AuthController(AuthService service) {
		super();
		this.service = service;
		this.authManager = null;
	}

	@PostMapping("/register")
	public Mono<ResponseEntity<TokenResponse>>
	register(@RequestBody final RegisterRequest request){
		  
	return service.register(request)
			.map(token -> ResponseEntity.ok(token));
	}
	
	@PostMapping("/login")
	public Mono<ResponseEntity<TokenResponse>>
	login(@RequestBody final LoginRequest request){
		return service.login(request)
				.map(token -> ResponseEntity.ok(token));
	}
	
	//si queremos tener un token nuevo
	//@RequestHeader(HttpHeaders.AUTHORIZATION) trae el token que se genero anteriormente
	@PostMapping("/refresh")
	public Mono<TokenResponse>
	refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) final String authHeader ){
		return service.refreshToken(authHeader);
	}
	
}
