package com.ai.demo.controller;

import com.ai.demo.dto.AuthResponse;
import com.ai.demo.dto.LoginRequest;
import com.ai.demo.dto.RegisterRequest;
import com.ai.demo.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@RequestBody String refreshToken) {
        return authService.refreshToken(refreshToken)
                .map(ResponseEntity::ok);
    }
}
