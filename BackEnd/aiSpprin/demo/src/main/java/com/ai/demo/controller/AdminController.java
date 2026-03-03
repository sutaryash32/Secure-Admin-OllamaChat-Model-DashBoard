package com.ai.demo.controller;

import com.ai.demo.exception.AppException;
import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public Mono<List<User>> getAllUsers() {
        return Mono.fromCallable(userRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/users/{id}/disable")
    public Mono<ResponseEntity<User>> disableUser(@PathVariable Long id) {
        return Mono.fromCallable(() ->
                userRepository.findById(id).map(u -> {
                    u.setIsActive(false);
                    return ResponseEntity.ok(userRepository.save(u));
                }).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/users/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return Mono.<ResponseEntity<Void>>fromCallable(() -> {
            if (!userRepository.existsById(id)) {
                throw new AppException("User not found", HttpStatus.NOT_FOUND);
            }
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/analytics")
    public Mono<Map<String, Object>> analytics() {
        return Mono.fromCallable(() ->
                Map.<String, Object>of("totalUsers", userRepository.count())
        ).subscribeOn(Schedulers.boundedElastic());
    }
}