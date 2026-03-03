package com.ai.demo.controller;

import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/enable")
    public Mono<ResponseEntity<User>> enableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setEnabled(true);
                    return userRepository.save(user);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/disable")
    public Mono<ResponseEntity<User>> disableUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setEnabled(false);
                    return userRepository.save(user);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/role")
    public Mono<ResponseEntity<User>> updateUserRole(@PathVariable Long id, @RequestBody String role) {
        return userRepository.findById(id)
                .flatMap(user -> {
                    user.setRole(role);
                    return userRepository.save(user);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .flatMap(user -> userRepository.deleteById(id).thenReturn(ResponseEntity.noContent().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/analytics")
    public Mono<ResponseEntity<AnalyticsResponse>> getAnalytics() {
        return userRepository.count()
                .map(count -> ResponseEntity.ok(new AnalyticsResponse(count, "Chat analytics placeholder")));
    }

    public record AnalyticsResponse(long totalUsers, String message) {}
}
