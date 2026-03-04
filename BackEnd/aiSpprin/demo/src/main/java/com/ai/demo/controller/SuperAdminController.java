package com.ai.demo.controller;

import com.ai.demo.exception.AppException;
import com.ai.demo.model.User;
import com.ai.demo.repository.UserRepository;
import com.ai.demo.repository.ChatHistoryRepository;
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
@RequestMapping("/api/v1/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserRepository userRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    // ── Get all users ─────────────────────────────────────
    @GetMapping("/users")
    public Mono<List<User>> getAllUsers() {
        return Mono.fromCallable(userRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic());
    }

    // ── Assign role ───────────────────────────────────────
    @PutMapping("/users/{id}/role")
    public Mono<ResponseEntity<User>> assignRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return Mono.fromCallable(() -> {
            String newRole = body.get("role");
            if (newRole == null || (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_SUPER_ADMIN"))) {
                throw new AppException("Invalid role. Must be ROLE_USER or ROLE_SUPER_ADMIN", HttpStatus.BAD_REQUEST);
            }
            return userRepository.findById(id).map(u -> {
                u.setRole(newRole);
                return ResponseEntity.ok(userRepository.save(u));
            }).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Disable / Enable user ─────────────────────────────
    @PutMapping("/users/{id}/disable")
    public Mono<ResponseEntity<User>> disableUser(@PathVariable Long id) {
        return Mono.fromCallable(() ->
                userRepository.findById(id).map(u -> {
                    u.setIsActive(false);
                    return ResponseEntity.ok(userRepository.save(u));
                }).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/users/{id}/enable")
    public Mono<ResponseEntity<User>> enableUser(@PathVariable Long id) {
        return Mono.fromCallable(() ->
                userRepository.findById(id).map(u -> {
                    u.setIsActive(true);
                    return ResponseEntity.ok(userRepository.save(u));
                }).orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    // ── Delete user ───────────────────────────────────────
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

    // ── Analytics ─────────────────────────────────────────
    @GetMapping("/analytics")
    public Mono<Map<String, Object>> analytics() {
        return Mono.fromCallable(() -> Map.<String, Object>of(
                "totalUsers",    userRepository.count(),
                "activeUsers",   userRepository.findAll().stream().filter(User::isEnabled).count(),
                "disabledUsers", userRepository.findAll().stream().filter(u -> !u.isEnabled()).count(),
                "totalChats",    chatHistoryRepository.count()
        )).subscribeOn(Schedulers.boundedElastic());
    }
}