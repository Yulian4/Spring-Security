package com.yuliana.demo.dto;

import com.yuliana.demo.model.Usuario;
import com.yuliana.demo.dto.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserRepository userRepository;
    public UserController(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}


   
    @PostMapping
    public Mono<ResponseEntity<Usuario>> createUser(@RequestBody Usuario user) {
        return userRepository.save(user)
                .map(savedUser -> ResponseEntity.ok(savedUser));
    }

 
    @GetMapping
    public Flux<Usuario> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Usuario>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Usuario>> updateUser(@PathVariable Long id, @RequestBody Usuario user) {
        return userRepository.findById(id)
                .flatMap(existingUser -> {
                    existingUser.setNombre(user.getNombre());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setPassword(user.getPassword());
                    return userRepository.save(existingUser);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .flatMap(existingUser ->
                        userRepository.delete(existingUser)
                                .then(Mono.just(ResponseEntity.noContent().<Void>build())) // 👈 Aquí forzamos el tipo
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

}
