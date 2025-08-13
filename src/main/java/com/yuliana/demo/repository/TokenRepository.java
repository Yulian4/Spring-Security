package com.yuliana.demo.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;

public interface TokenRepository extends ReactiveCrudRepository<Token, Long> {
	 Flux<Token> findAllByUserIdAndExpiredFalseAndRevokedFalse(Long userId);
}
