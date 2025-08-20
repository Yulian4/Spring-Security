package com.yuliana.demo.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository extends ReactiveCrudRepository<Token, Long> {
	 Flux<Token> findAllByUserIdAndExpiredFalseAndRevokedFalse(Long userId);
	 Mono<Token> findByToken(String token);
}
