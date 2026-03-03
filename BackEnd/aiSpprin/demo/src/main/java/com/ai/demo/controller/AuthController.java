package com.ai.demo.controller;

import com.ai.demo.dto.AuthResponseDto;
import com.ai.demo.dto.LoginRequestDto;
import com.ai.demo.dto.RegisterRequestDto;
import com.ai.demo.service.AuthService;
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
}