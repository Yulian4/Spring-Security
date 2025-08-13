package com.yuliana.demo.dto;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.yuliana.demo.model.Usuario;

import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<Usuario, Long>{


    Mono<Usuario> findByEmail(String email);
}
