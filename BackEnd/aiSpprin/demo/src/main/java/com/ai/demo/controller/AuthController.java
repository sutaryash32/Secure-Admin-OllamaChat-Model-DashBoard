package com.ai.demo.controller;

import com.ai.demo.dto.AuthResponseDto;
import com.ai.demo.dto.LoginRequestDto;
import com.ai.demo.dto.RegisterRequestDto;
import com.ai.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto req) {
        return authService.register(req).map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto req) {
        return authService.login(req).map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponseDto>> refresh(@RequestBody String refreshToken) {
        return authService.refreshToken(refreshToken.trim()).map(ResponseEntity::ok);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logout() {
        return ReactiveSecurityContextHolder.getContext()
                .doOnNext(ctx -> {
                    log.info("User logging out: {}", ctx.getAuthentication() != null
                            ? ctx.getAuthentication().getName() : "unknown");
                    ctx.setAuthentication(null);
                })
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Logged out successfully"))))
                .defaultIfEmpty(ResponseEntity.ok(Map.of("message", "Logged out successfully")));
    }
}